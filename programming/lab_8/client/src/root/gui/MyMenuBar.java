package root.gui;

import root.connection.CommandExecutor;
import root.connection.Sender;
import root.server.requests.IRequest;
import root.tasks.ILanguageUpdateble;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MyMenuBar extends JMenuBar implements ILanguageUpdateble {
    private final Map<String, JMenu> menus = new HashMap<>();
    private final Map<String, Map<String, JMenuItem>> menuMaps = new HashMap<>();

    private String helpText;
    private final String EDIT = "edit", INFO = "info";

    private final Sender sender;
    private final CommandExecutor commandExecutor;
    private final InputProduct inputProduct;

    public MyMenuBar(Sender sender, CommandExecutor commandExecutor, InputProduct inputProduct) {
        addMenu(INFO); addMenu(EDIT);
        this.sender = sender;
        this.commandExecutor = commandExecutor;
        this.inputProduct = inputProduct;
    }

    private void addMenu(String resource) {
        menus.put(resource, new JMenu());
        menuMaps.put(resource, new HashMap<>());
        add(menus.get(resource));
    }

    public void createHelp(Supplier<String> helpForScripts) {
        JMenu menu = new JMenu();
        menus.put("help", menu); add(menu);
        menu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JOptionPane.showMessageDialog(
                        null, helpText + "\n\n" + helpForScripts.get(),
                        menu.getText(), JOptionPane.PLAIN_MESSAGE);
            }
        });
    }

    public void createLanguage(Map<String, Locale> locales, Runnable updater) {
        JMenu menu = new JMenu();
        menus.put("language", menu); add(menu);
        for (String language : locales.keySet()) {
            JMenuItem item = new JMenuItem(language);
            item.addActionListener(event -> {
                UIManager.getDefaults().setDefaultLocale(locales.get(language));
                JOptionPane.setDefaultLocale(locales.get(language));
                Locale.setDefault(locales.get(language));
                updater.run();
            });
            menu.add(item);
        }
    }

    public void createExecuteScript(BiConsumer<File, Integer> scriptExecutor, JFileChooser fileChooser) {
        JMenuItem menuItem = new JMenuItem();
        menuItem.addActionListener(event -> {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                commandExecutor.execute(() ->
                    scriptExecutor.accept(fileChooser.getSelectedFile(), 1)
        );});
        menuMaps.get(EDIT).put("execute_script", menuItem);
        menus.get(EDIT).add(menuItem);
    }

    public void addItemToInfo(String name, Supplier<String> supplier) {
        JMenuItem menuItem = new JMenuItem();
        menuItem.addActionListener(event -> JOptionPane.showMessageDialog(
                null, supplier.get(), menuItem.getText(), JOptionPane.PLAIN_MESSAGE));
        menuMaps.get(INFO).put(name, menuItem);
        menus.get(INFO).add(menuItem);
    }

    public void addItemToEdit(String name, IRequest request) {
        JMenuItem menuItem = new JMenuItem();
        menuItem.addActionListener(event -> {
            if (request.takeProduct(() -> {
                inputProduct.setProduct(null);
                inputProduct.setVisible(true);
                return inputProduct.getProduct();
            })) sender.accept(request);
        });
        menuMaps.get(EDIT).put(name, menuItem);
        menus.get(EDIT).add(menuItem);
    }

    @Override
    public void updateLanguage() {
        for (String menu: menus.keySet())
            menus.get(menu).setText(UIManager.getString("menu_bar." + menu));
        updateItems(INFO); updateItems(EDIT);
        helpText = UIManager.getString("menu_bar.help.text");
    }

    private void updateItems(String menu) {
        for (String submenu: menuMaps.get(menu).keySet())
            menuMaps.get(menu).get(submenu).setText(UIManager.getString("menu_bar." + menu + "." + submenu));
    }
}