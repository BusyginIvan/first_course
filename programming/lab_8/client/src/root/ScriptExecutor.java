package root;

import root.gui.Printer;
import root.product.*;
import root.server.requests.IRequest;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScriptExecutor {
    private Iterator<String> iterator;
    private final String EXECUTE = "execute_script";

    private final Map<String, IRequest> requests;
    private final Map<String, Supplier<String>> commands;

    private final Consumer<Serializable> sender;
    private final Consumer<Runnable> commandExecutor;
    private final Printer printer;

    public ScriptExecutor(Consumer<Serializable> sender, Consumer<Runnable> commandExecutor, Printer printer) {
        requests = new HashMap<>();
        commands = new HashMap<>();
        this.sender = sender;
        this.commandExecutor = commandExecutor;
        this.printer = printer;

        addCommand("help", this::getHelpText);
    }

    public String getHelpText() {
        StringBuilder stringBuilder = new StringBuilder(UIManager.getString("execute_script.commands"))
                .append(":\n ").append(EXECUTE).append(" - ")
                .append(UIManager.getString("description.execute_script"));

        commands.keySet().forEach(name -> addLine(stringBuilder, name));
        requests.keySet().forEach(name -> addLine(stringBuilder, name));

        return stringBuilder.toString() + ".";
    }

    private void addLine(StringBuilder stringBuilder, String name) {
        stringBuilder.append(";\n ").append(name).append(" - ")
                .append(UIManager.getString("description." + name));
    }

    public void addCommand(String name, Supplier<String> action) {
        commands.put(name, action);
    }

    public void addRequest(String name, IRequest request) {
        requests.put(name, request);
    }

    /*public void setUser(User user) {
        requests.values().forEach(request -> request.setUser(user));
    }*/

    public void executeScript(File file, int nestingLevel) {
        printer.setVisible(true);
        if (nestingLevel > 100)
            printRuntimeError("max_nesting_level");
        else if (!file.exists() || file.isDirectory())
            printRuntimeError("invalid_file");
        else if (!file.canRead())
            printRuntimeError("no_read_rights");
        else {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                iterator = bufferedReader.lines().iterator();

                int x = 0;
                while (iterator.hasNext()) {
                    String[] line = iterator.next().split("\\s+");
                    if (line[0].equals("")) continue;
                    if (line[0].equals(EXECUTE)) {
                        if (line.length < 2)
                            printCommandError(EXECUTE, "miss_file");
                        commandExecutor.accept(() -> executeScript(new File(line[1]), nestingLevel + 1));
                        x++;
                    } else if (commands.containsKey(line[0])) {
                        commandExecutor.accept(() -> printer.print(commands.get(line[0]).get()));
                        x++;
                    } else if (requests.containsKey(line[0])) {
                        IRequest request = requests.get(line[0]);
                        String message = request.takeWords(line.length > 1 ? Arrays.copyOfRange(line, 1, line.length) : null);
                        if (message != null) {
                            printCommandError(line[0], message);
                            continue;
                        }
                        if (request.takeProduct(this::getProduct)) {
                            sender.accept(request); x++;
                        } else printCommandError(line[0], "miss_product");
                    }
                }
                printer.print(UIManager.getString("execute_script.commands_num") + ": " + x + ".");
            } catch (IOException e) {
                printRuntimeError("io_exception");
            }
        }
    }

    private void printCommandError(String command, String message) {
        printer.print(UIManager.getString("execute_script.command_error") +
                " " + command + ". " + UIManager.getString("execute_script." + message) + ".");
    }

    private void printRuntimeError(String message) {
        printer.print(UIManager.getString("execute_script.runtime_error") +
                ". " + UIManager.getString("execute_script." + message) + ".");
    }

    private Product getProduct() {
        Product product = new Product();
        try {
            product.setName(iterator.next());

            Coordinates coordinates = new Coordinates();
            coordinates.setX(Float.parseFloat(iterator.next()));
            coordinates.setY(Double.parseDouble(iterator.next()));
            product.setCoordinates(coordinates);

            product.setPrice(Double.parseDouble(iterator.next()));

            String str = iterator.next();
            product.setUnitOfMeasure(str.equals("") ? null : UnitOfMeasure.valueOf(str));

            if ((str = iterator.next()).equals(""))
                product.setOwner(null);
            else {
                Person person = new Person();
                person.setPassportID(str);

                person.setName(iterator.next());
                person.setHeight((str = iterator.next()).equals("") ? null : Float.parseFloat(str));
                person.setWeight(Float.parseFloat(iterator.next()));

                if ((str = iterator.next()).equals(""))
                    person.setLocation(null);
                else {
                    Location location = new Location();
                    location.setName(str);
                    location.setX(Float.parseFloat(iterator.next()));
                    location.setY(Integer.parseInt(iterator.next()));
                    location.setZ(Long.parseLong(iterator.next()));
                    person.setLocation(location);
                }

                product.setOwner(person);
            }

            return product;
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return null;
        }
    }
}