package root.database;

import root.client.IClientSkills;
import root.gui.panel.MyPanel;
import root.product.Product;
import root.product_manager.ISerializableProductManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.stream.Collectors;

import static javax.swing.UIManager.getString;

public class ClientSkills implements IClientSkills {
    private ISerializableProductManager database;
    private final AbstractTableModel tableModel;
    private final MyPanel panel;

    public ClientSkills(AbstractTableModel tableModel, MyPanel panel) {
        this.tableModel = tableModel;
        this.panel = panel;
    }

    public void setDatabase(ISerializableProductManager database) {
        this.database = database;
    }

    @Override
    public void error(String... strings) {
        JOptionPane.showMessageDialog(
                null,
                Arrays.stream(strings)
                        .map(str -> getString("error." + str))
                        .collect(Collectors.joining(" ")),
                getString("error"),
                JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    public boolean put(Product product) {
        database.put(product);
        panel.put(product.getID());
        panel.repaint();
        tableModel.fireTableDataChanged();
        return true;
    }

    @Override
    public void remove(long id) {
        String login = database.get(id).getLogin();
        database.remove(id);
        panel.remove(id, login);
        panel.repaint();
        tableModel.fireTableDataChanged();
    }

    @Override
    public void clear(String login) {
        panel.clear(login);
        database.clear(login);
        panel.repaint();
        tableModel.fireTableDataChanged();
    }
}