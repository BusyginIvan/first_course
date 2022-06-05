package root.remote_database;

import java.time.LocalDate;
import java.util.Map;

import root.User;
import root.product.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static root.Closer.close;

public class RemoteDatabase implements IRemoteUsersManager, IRemoteProductManager {
    private Connection connection;

    private PreparedStatement SELECT_USERS_BY_LOGIN;
    private PreparedStatement INSERT_USERS;
    private PreparedStatement SELECT_PRODUCTS;
    private PreparedStatement SELECT_PRODUCTS_BY_OWNER;
    private PreparedStatement SELECT_PRODUCTS_BY_ID;
    private PreparedStatement SELECT_OWNERS_BY_ID;
    private PreparedStatement SELECT_OWNERS;
    private PreparedStatement SELECT_LOCATIONS_BY_ID;
    private PreparedStatement LOCATION_INSERTER;
    private PreparedStatement DELETE_LOCATION;
    private PreparedStatement DELETE_OWNER;
    private PreparedStatement DELETE_PRODUCTS_BY_LOGIN;
    private PreparedStatement CLEAR_OWNERS;
    private PreparedStatement CLEAR_LOCATIONS;

    public RemoteDatabase(String URL, String login, String password) {
        try {
            connection = DriverManager.getConnection(URL, login, password);

            SELECT_USERS_BY_LOGIN = connection.prepareStatement("SELECT * FROM USERS WHERE login = ?");
            INSERT_USERS = connection.prepareStatement("INSERT INTO USERS VALUES (?, ?)");
            SELECT_PRODUCTS = connection.prepareStatement("SELECT * FROM products_table",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            SELECT_PRODUCTS_BY_ID = connection.prepareStatement("SELECT * FROM products_table WHERE id = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            SELECT_OWNERS_BY_ID = connection.prepareStatement("SELECT * FROM owners_table WHERE passport_id = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            SELECT_OWNERS = connection.prepareStatement("SELECT * FROM owners_table", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            SELECT_LOCATIONS_BY_ID = connection.prepareStatement("SELECT * FROM locations_table WHERE id = ?");
            LOCATION_INSERTER = connection.prepareStatement("SELECT * FROM locations_table",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            DELETE_LOCATION = connection.prepareStatement("DELETE FROM locations_table WHERE id = ?");
            SELECT_PRODUCTS_BY_OWNER = connection.prepareStatement("SELECT * FROM products_table WHERE owner_id = ?");
            DELETE_OWNER = connection.prepareStatement("DELETE FROM owners_table WHERE passport_id = ?");
            DELETE_PRODUCTS_BY_LOGIN = connection.prepareStatement("DELETE FROM products_table WHERE login = ?");
            CLEAR_OWNERS = connection.prepareStatement("DELETE FROM owners_table WHERE EXISTS(SELECT * FROM products_table WHERE owner_id = owners_table.passport_id)");
            CLEAR_LOCATIONS = connection.prepareStatement("DELETE FROM locations_table WHERE EXISTS(SELECT * FROM owners_table WHERE location_id = locations_table.id)");

            System.out.println("Подключение к базе данных установлено.");
        } catch (SQLException e) {
            //e.printStackTrace();
            System.err.println("Не удалось подключиться к базе данных.");
            System.exit(0);
        }
    }

    private boolean action(AutoCloseable runnable) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            return false;
        }
        try {
            try {
                Savepoint savepoint = connection.setSavepoint();
                try {
                    runnable.close();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    connection.rollback(savepoint);
                }
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ignored) { }
        return false;
    }

    @Override
    public boolean clear(String login) {
        return action(() -> {
            DELETE_PRODUCTS_BY_LOGIN.setString(1, login);
            DELETE_PRODUCTS_BY_LOGIN.executeUpdate();
            CLEAR_OWNERS.executeUpdate();
            CLEAR_LOCATIONS.executeUpdate();
        });
    }

    @Override
    public boolean add(Product product) {
        return action(() -> {
            addOwner(product.getOwner());
            ResultSet result = SELECT_PRODUCTS.executeQuery();
            result.moveToInsertRow();
            updateProductInResult(result, product);
            result.insertRow();
            product.setID(result.getLong("id"));
            close(result);
        });
    }

    @Override
    public boolean update(Product product) {
        return action(() -> {
            String passportID = product.getOwner() == null ? null : product.getOwner().getPassportID();
            addOwner(product.getOwner());
            SELECT_PRODUCTS_BY_ID.setLong(1, product.getID());
            ResultSet result = SELECT_PRODUCTS_BY_ID.executeQuery();
            if (result.next()) {
                updateProductInResult(result, product);
                result.updateRow();
            } else {
                result.moveToInsertRow();
                result.updateLong("id", product.getID());
                updateProductInResult(result, product);
                result.insertRow();
            }
            close(result);
            deleteOwner(passportID);
        });
    }

    @Override
    public boolean remove(long id) {
        return action(() -> {
            SELECT_PRODUCTS_BY_ID.setLong(1, id);
            ResultSet result = SELECT_PRODUCTS_BY_ID.executeQuery();
            if (result.next()) {
                String passportID = result.getString("owner_id");
                result.deleteRow();
                deleteOwner(passportID);
            }
        });
    }

    private void addOwner(Person owner) throws SQLException {
        if (owner != null) {
            SELECT_OWNERS_BY_ID.setString(1, owner.getPassportID());
            ResultSet result = SELECT_OWNERS_BY_ID.executeQuery();
            if (result.next()) {
                Person oldOwner = extractOwnerFromResult(result);
                if (!owner.equals(oldOwner)) {
                    DELETE_LOCATION.setInt(1, result.getInt("location_id"));
                    if (!result.wasNull())
                        DELETE_LOCATION.executeUpdate();
                    updateOwnerInResult(result, owner);
                    result.updateRow();
                }
            } else {
                result.moveToInsertRow();
                result.updateString("passport_id", owner.getPassportID());
                updateOwnerInResult(result, owner);
                result.insertRow();
            }
        }
    }
    
    private void updateProductInResult(ResultSet result, Product product) throws SQLException {
        result.updateString("name", product.getName());
        result.updateDouble("price", product.getPrice());
        result.updateFloat("x", product.getCoordinates().getX());
        result.updateDouble("y", product.getCoordinates().getY());
        result.updateObject("creation_date", Timestamp.valueOf(product.getCreationDate()));
        if (product.getUnitOfMeasure() == null) result.updateNull("unit_of_measure");
        else result.updateString("unit_of_measure", product.getUnitOfMeasure().toString());
        if (product.getOwner() == null) result.updateNull("owner_id");
        else result.updateString("owner_id", product.getOwner().getPassportID());
        result.updateString("login", product.getLogin());
    }

    private void updateOwnerInResult(ResultSet result, Person owner) throws SQLException {
        result.updateString("name", owner.getName());
        if (owner.getHeight() == null) result.updateNull("height");
        else result.updateFloat("height", owner.getHeight());
        result.updateFloat("weight", owner.getWeight());

        if (owner.getLocation() == null)
            result.updateNull("location_id");
        else {
            ResultSet resultFromLocations = LOCATION_INSERTER.executeQuery();
            resultFromLocations.moveToInsertRow();
            resultFromLocations.updateString("name", owner.getLocation().getName());
            resultFromLocations.updateFloat("x", owner.getLocation().getX());
            resultFromLocations.updateInt("y", owner.getLocation().getY());
            resultFromLocations.updateLong("z", owner.getLocation().getZ());
            resultFromLocations.insertRow();
            result.updateInt("location_id", resultFromLocations.getInt("id"));
        }
    }

    private void deleteOwner(String id) throws SQLException {
        if (id != null) {
            SELECT_PRODUCTS_BY_OWNER.setString(1, id);
            if (!SELECT_PRODUCTS_BY_OWNER.executeQuery().next()) {
                DELETE_OWNER.setString(1, id);
                DELETE_OWNER.executeUpdate();
            }
        }
    }

    @Override
    public void put(User user) throws SQLException {
        INSERT_USERS.setString(1, user.login);
        INSERT_USERS.setString(2, user.password);
        INSERT_USERS.executeUpdate();
    }

    private boolean userExists(String login) throws SQLException {
        SELECT_USERS_BY_LOGIN.setString(1, login);
        ResultSet result = SELECT_USERS_BY_LOGIN.executeQuery();
        try {
            return result.next();
        } finally {
            close(result);
        }
    }

    public void loadCollection(Predicate<Product> receiver) {
        try {
            ResultSet result = SELECT_PRODUCTS.executeQuery();
            int loaded = 0, requestExceptions = 0, contentExceptions = 0;

            while (result.next()) {
                try {
                    if (receiver.test(extractProductFromResult(result))) {
                        loaded++;
                        continue;
                    } contentExceptions++;
                } catch (SQLException e) {
                    requestExceptions++;
                } catch (IllegalArgumentException e) {
                    contentExceptions++;
                } result.deleteRow();
            }

            close(result);

            System.out.println("Коллекция загружена из базы данных. Количество товаров: " + loaded + ".");
            if (requestExceptions > 0)
                System.out.println("Из-за ошибок при запрорсах к базе данных было пропущено товаров: " + requestExceptions + ".");
            if (contentExceptions > 0)
                System.out.println("Из-за ошибок в содержании базы данных было пропущено товаров: " + contentExceptions + ".");
        } catch (SQLException e) {
            System.out.println("Ошибка при загрузке коллекции из базы данных.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private Location extractLocationFromResult(ResultSet result) throws SQLException {
        Location location = new Location();

        location.setName(result.getString("name"));
        location.setX(result.getFloat("x"));
        location.setY(result.getInt("y"));
        location.setZ(result.getLong("z"));

        return location;
    }

    private Person extractOwnerFromResult(ResultSet result) throws SQLException {
        Person person = new Person();

        person.setPassportID(result.getString("passport_id"));
        person.setName(result.getString("name"));
        person.setHeight(result.getObject("height", Float.class));
        person.setWeight(result.getFloat("weight"));

        int locationID = result.getInt("location_id");
        if (locationID != 0) {
            SELECT_LOCATIONS_BY_ID.setInt(1, locationID);
            try (ResultSet resultFromLocations = SELECT_LOCATIONS_BY_ID.executeQuery()) {
                if (resultFromLocations.next()) {
                    person.setLocation(extractLocationFromResult(resultFromLocations));
                    return person;
                }
                throw new IllegalArgumentException("Ошибка! Несоответствие локаций с владельцем.");
            }
        }

        return person;
    }

    private Product extractProductFromResult(ResultSet result) throws SQLException {
        Product product = new Product();

        if (!userExists(result.getString("login")))
            throw new IllegalArgumentException("Ошибка! Не существует пользователя, указанного в товаре.");

        product.setLogin(result.getString("login"));
        product.setID(result.getLong("id"));
        product.setName(result.getString("name"));
        product.setCreationDate(result.getObject("creation_date", LocalDateTime.class));
        product.setPrice(result.getDouble("price"));
        product.setUnitOfMeasure(result.getString("unit_of_measure"));

        Coordinates coordinates = new Coordinates();
        coordinates.setX(result.getFloat("x"));
        coordinates.setY(result.getDouble("y"));
        product.setCoordinates(coordinates);

        String passportID = result.getString("owner_id");
        if (!(passportID == null)) {
            SELECT_OWNERS_BY_ID.setString(1, passportID);
            try (ResultSet resultFromOwners = SELECT_OWNERS_BY_ID.executeQuery()) {
                if (resultFromOwners.next()) {
                    product.setOwner(extractOwnerFromResult(resultFromOwners));
                    return product;
                }
                throw new IllegalArgumentException("Ошибка! Несоответствие владельцев с товаром.");
            }
        }

        return product;
    }

    public LocalDate getInitializationDate() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM info",
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet result = statement.executeQuery();

        if (result.next()) {
            if (result.next()) {
                statement.executeUpdate("TRUNCATE TABLE info");
                result.moveToInsertRow();
                result.insertRow();
            } else
                result.previous();
        } else {
            result.moveToInsertRow();
            result.insertRow();
        }

        try {
            return result.getObject("init_date", LocalDate.class);
        } finally {
            close(statement);
        }
    }

    public void loadUsers(BiPredicate<String, String> receiver) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM users");
            int loaded = 0, requestExceptions = 0, contentExceptions = 0;

            while (result.next()) {
                try {
                    if (receiver.test(result.getString("login"), result.getString("password"))) {
                        loaded++;
                        continue;
                    } contentExceptions++;
                } catch (SQLException e) {
                    requestExceptions++;
                } catch (IllegalArgumentException e) {
                    contentExceptions++;
                } result.deleteRow();
            }

            close(statement);
            System.out.println("Информация о пользователях загружена из базы данных. Число регистраций: " + loaded + ".");
            if (requestExceptions > 0)
                System.out.println("Из-за ошибок при запрорсах к базе данных было пропущено аккаунтов: " + requestExceptions + ".");
            if (contentExceptions > 0)
                System.out.println("Из-за ошибок в содержании базы данных было пропущено аккаунтов: " + contentExceptions + ".");
        } catch (SQLException e) {
            System.out.println("Ошибка при загрузке пользователей из базы данных.");
            System.exit(-1);
        }
    }

    public void clearOwners(Map<String, Person> owners) throws SQLException {
        ResultSet result = SELECT_OWNERS.executeQuery();
        PreparedStatement deleteLocationStatement = connection.prepareStatement(
                "DELETE FROM locations_table WHERE id = ?");
        Savepoint savepoint;
        connection.setAutoCommit(false);

        while (result.next()) {
            if (!owners.containsKey(result.getString("passport_id"))) {
                savepoint = connection.setSavepoint();
                try {
                    int locationID = result.getInt("location_id");
                    if (!result.wasNull()) {
                        deleteLocationStatement.setInt(1, locationID);
                        deleteLocationStatement.executeUpdate();
                    }
                    result.deleteRow();
                } catch (SQLException e) {
                    connection.rollback(savepoint);
                    throw e;
                }
            }
        }

        connection.setAutoCommit(true);
        close(deleteLocationStatement);
        close(result);
    }

    public void destroy() {
        close(SELECT_OWNERS_BY_ID);
        close(SELECT_OWNERS);
        close(INSERT_USERS);
        close(SELECT_USERS_BY_LOGIN);
        close(SELECT_LOCATIONS_BY_ID);
        close(SELECT_PRODUCTS);
        close(LOCATION_INSERTER);
        close(DELETE_LOCATION);
        close(SELECT_PRODUCTS_BY_OWNER);
        close(DELETE_OWNER);
        close(DELETE_PRODUCTS_BY_LOGIN);
        close(CLEAR_OWNERS);
        close(CLEAR_LOCATIONS);
        close(connection);
    }
}