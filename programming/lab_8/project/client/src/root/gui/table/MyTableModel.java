package root.gui.table;

import root.product.Person;
import root.product.Product;
import root.product_manager.IReadableProductManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Iterator;

public class MyTableModel extends AbstractTableModel {
    private IReadableProductManager database;
    private final String[] columnNames = new String[]
            {"short.login", "short.id", "name", "short.x", "short.y", "creation_date",
                    "price", "short.unit_of_measure", "short.owner"};

    public String getIdentifier(int i) {
        String[] words = columnNames[i].split("\\.");
        return words[words.length - 1];
    }

    public void setDatabase(IReadableProductManager database) {
        this.database = database;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        if (database == null) return 0;
        return database.getProducts().size();
    }

    @Override
    public String getColumnName(int i) {
        return UIManager.getString("product." + columnNames[i]);
    }

    @Override
    public Object getValueAt(int row, int col) {
        Product product = getRow(row);
        switch (col) {
            case 0: return product.getLogin();
            case 1: return product.getID();
            case 2: return product.getName();
            case 3: return product.getCoordinates().getX();
            case 4: return product.getCoordinates().getY();
            case 5: return product.getCreationDate();
            case 6: return product.getPrice();
            case 7: return product.getUnitOfMeasure();
            case 8:
                Person owner = product.getOwner();
                if (owner == null) return "";
                return owner.getPassportID();
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public Product getRow(int row) {
        Iterator<Product> iterator = database.iterator();
        while (iterator.hasNext() && row-- > 0) iterator.next();
        return iterator.next();
    }
}