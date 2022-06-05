package root;

import root.client.responses.*;
import root.local_users_manager.IUsersValidator;
import root.product.Product;
import root.product_manager.IUpdatebleProductManager;
import root.remote_database.IRemoteProductManager;
import root.server.IServerSkills;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

public class ServerSkills implements IServerSkills {
    private final IUpdatebleProductManager localDatabase;
    private final IRemoteProductManager remoteDatabase;
    private final Consumer<Serializable> responseSender;
    private final IUsersValidator usersValidator;

    public ServerSkills(IUpdatebleProductManager localDatabase, IRemoteProductManager remoteDatabase,
                        IUsersValidator usersValidator, Consumer<Serializable> responseSender) {
        this.localDatabase = localDatabase;
        this.remoteDatabase = remoteDatabase;
        this.responseSender = responseSender;
        this.usersValidator = usersValidator;
    }

    @Override
    public String[] removeFirst(User user) {
        Optional<Product> optional = localDatabase.stream().max(Product::compareTo);
        if (!optional.isPresent()) return fail("remove_first");
        return removeByID(optional.get().getID(), user);
    }

    @Override
    public String[] clear(User user) {
        currentCommand = "clear";
        if (!usersValidator.contains(user))
            return biFail("identity");
        if (!remoteDatabase.clear(user.login))
            return biFail("connection");
        localDatabase.clear(user.login);
        responseSender.accept(new ClearResponse(user.login));
        return null;
    }

    @Override
    public String[] removeByID(long id, User user) {
        currentCommand = "remove";
        if (!usersValidator.contains(user))
            return biFail("identity");
        if (localDatabase.get(id) == null)
            return biFail("no_exists");
        if (!localDatabase.get(id).getLogin().equals(user.login))
            return biFail("foreign_product");
        if (!remoteDatabase.remove(id))
            return biFail("connection");
        localDatabase.remove(id);
        responseSender.accept(new RemoveResponse(id));
        return null;
    }

    @Override
    public String[] add(Product product, String password) {
        currentCommand = "add";
        if (!usersValidator.contains(product.getLogin(), password))
            return biFail("identity");
        if (localDatabase.invalidOwner(product.getOwner()))
            return biFail("passport_exists");
        if (!remoteDatabase.add(product))
            return biFail("connection");
        localDatabase.put(product);
        responseSender.accept(new PutResponse(product));
        return null;
    }

    @Override
    public String[] addIfMax(Product product, String password) {
        Optional<Product> optional = localDatabase.stream().max(Product::compareTo);
        if (optional.isPresent() && product.compareTo(optional.get()) <= 0)
            return fail("add_if_max");
        return add(product, password);
    }

    @Override
    public String[] update(Product product, String password) {
        currentCommand = "update";
        if (localDatabase.get(product.getID()) == null)
            return biFail("no_exists");
        if (!usersValidator.contains(product.getLogin(), password))
            return biFail("identity");
        if (!localDatabase.get(product.getID()).getLogin().equals(product.getLogin()))
            return biFail("foreign_product");
        if (localDatabase.invalidOwner(product.getOwner()))
            return biFail("passport_exists");
        if (!remoteDatabase.update(product))
            return biFail("connection");
        localDatabase.put(product);
        responseSender.accept(new PutResponse(product));
        return null;
    }

    private String currentCommand;
    private String[] fail(String cause) {
        return new String[]{cause};
    }
    private String[] biFail(String cause) {
        return new String[]{currentCommand, cause};
    }
}