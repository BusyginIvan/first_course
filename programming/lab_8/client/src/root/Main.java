package root;

import root.connection.*;
import root.database.ClientSkills;
import root.database.Info;
import root.formatters.Formatters;
import root.formatters.UnitsOfMeasure;
import root.gui.*;
import root.gui.panel.ColorManager;
import root.gui.panel.MyPanel;
import root.gui.registrar.RegistrarController;
import root.gui.registrar.RegistrarDialog;
import root.gui.table.MyTable;
import root.gui.table.MyTableModel;
import root.product.UnitOfMeasure;
import root.server.requests.IRequest;
import root.server.requests.IRequestContainingID;
import root.server.requests.concrete_requests.*;
import root.tasks.*;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.*;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) throws Exception {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        UIManager.getDefaults().addResourceBundle("gui");
        setUIFont(new javax.swing.plaf.FontUIResource("Tahoma", Font.PLAIN,18));

        String[][] codes = new String[][] {{"ru", "RU"}, {"en", "GBR"}, {"no", "NO"}, {"sq", "AL"}};
        SortedMap<String, Locale> locales = new TreeMap<>();
        Arrays.stream(codes).forEach(code -> {
            Locale locale = new Locale(code[0], code[1]);
            locales.put(locale.getDisplayName(locale), locale);
        });

        UnitsOfMeasure units = new UnitsOfMeasure();
        Formatters formatters = new Formatters(obj -> units.unitToString((UnitOfMeasure) obj));
        MyTableModel tableModel = new MyTableModel();
        MyTable table = new MyTable(tableModel, formatters);
        Info info = new Info(formatters);
        MyPanel panel = new MyPanel(new ColorManager(), formatters::productToString);
        MyFrame frame = new MyFrame(panel, table);

        JOptionPane.setDefaultLocale(locales.get(locales.firstKey()));
        String language = (String) JOptionPane.showInputDialog(
                frame, null, "Choose language", JOptionPane.PLAIN_MESSAGE, null,
                locales.keySet().toArray(), locales.lastKey());
        if (language == null) {
            frame.dispose();
            return;
        }
        UIManager.getDefaults().setDefaultLocale(locales.get(language));
        JOptionPane.setDefaultLocale(locales.get(language));
        Locale.setDefault(locales.get(language));

        frame.updateLanguage(); table.updateLanguage();

        ClientSkills clientSkills = new ClientSkills(tableModel, panel);

        Connector connector = new Connector(frame, PortReader.readPort("port"), database -> {
            clientSkills.setDatabase(database);
            info.setDatabase(database);
            tableModel.setDatabase(database);
            panel.setDatabase(database);
        });

        RegistrarDialog registrarDialog = new RegistrarDialog(frame);
        RegistrarController registrarController = new RegistrarController(registrarDialog, connector);

        TaskQueue<IDisposable> disposer = new TaskQueue<>(IDisposable::dispose) {
            @Override public void execute() {
                connector.failedWriteObject(null);
                super.execute();
                System.exit(0);
            }
        };
        disposer.add(frame); disposer.add(connector); disposer.add(registrarDialog);
        connector.setExit(disposer::execute); registrarDialog.setExit(disposer::execute);

        registrarDialog.setVisible(true);
        User user = registrarController.getUser();
        registrarDialog.dispose();
        disposer.remove();
        table.setLogin(user.login);
        panel.setLogin(user.login);

        CommandExecutor commandExecutor = new CommandExecutor(connector, clientSkills);
        commandExecutor.start();
        Sender sender = new Sender(connector);

        TaskQueue<ILanguageUpdateble> languageUpdater = new TaskQueue<>(ILanguageUpdateble::updateLanguage);
        languageUpdater.add(frame); languageUpdater.add(connector);
        languageUpdater.add(formatters); languageUpdater.add(panel);
        languageUpdater.add(table);

        Printer printer = new Printer();
        disposer.add(printer); languageUpdater.add(printer);

        MyFileChooser fileChooser = new MyFileChooser();
        languageUpdater.add(fileChooser);

        InputProduct inputProduct = new InputProduct(frame, units, formatters);
        disposer.add(inputProduct); languageUpdater.add(inputProduct);
        tableModel.fireTableDataChanged();

        ScriptExecutor scriptExecutor = new ScriptExecutor(sender, commandExecutor::execute, printer);

        MyPopupMenu popupMenu = new MyPopupMenu(sender);
        languageUpdater.add(popupMenu);
        table.createPopupMenu(popupMenu, inputProduct);
        panel.setMenuAndInput(popupMenu, inputProduct);

        MyMenuBar menuBar = new MyMenuBar(sender, commandExecutor, inputProduct);
        menuBar.createExecuteScript(scriptExecutor::executeScript, fileChooser);
        menuBar.createHelp(scriptExecutor::getHelpText);
        menuBar.createLanguage(locales, languageUpdater::execute);
        new Object() {
            {
                addCommand("info", info::info);
                addCommand("print_unique_owner", info::printUniqueOwner);
                addCommand("head", info::head);
                addCommand("min_by_owner", info::minByOwner);
                addCommand("max_by_coordinates", info::maxByCoordinates);
                addRequest("clear", new ClearRequest().setUser(user));
                addRequest("remove_first", new RemoveFirstRequest().setUser(user));
                addRequest("add", new AddRequest().setUser(user));
                addRequest("add_if_max", new AddIfMaxRequest().setUser(user));
                addIDRequest("update", new UpdateRequest().setUser(user));
                addIDRequest("remove_by_id", new RemoveByIDRequest().setUser(user));
            }

            private void addCommand(String name, Supplier<String> action) {
                scriptExecutor.addCommand(name, action);
                menuBar.addItemToInfo(name, action);
            }

            private void addRequest(String name, IRequest request) {
                menuBar.addItemToEdit(name, request);
                scriptExecutor.addRequest(name, request);
            }

            private void addIDRequest(String name, IRequest request) {
                popupMenu.createItem(name, (IRequestContainingID) request, inputProduct);
                scriptExecutor.addRequest(name, request);
            }
        };
        menuBar.updateLanguage(); languageUpdater.add(menuBar);
        popupMenu.updateLanguage();

        frame.setJMenuBar(menuBar); frame.pack();
        frame.setExit(disposer::execute);

        frame.setOpacity(1);
    }

    public static void setUIFont(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while(keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if(value instanceof FontUIResource) UIManager.put(key, f);
        }
    }
}
