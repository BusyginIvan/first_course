package root.gui.table;

import root.formatters.Formatters;
import root.gui.InputProduct;
import root.gui.MyPopupMenu;
import root.product.Product;
import root.tasks.ILanguageUpdateble;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.function.Function;

public class MyTable extends JPanel implements ILanguageUpdateble {
    private final JTable table;
    private final MyTableModel tableModel;
    private String login;

    public MyTable(MyTableModel tableModel, Formatters formatters) {
        super(new GridLayout(1,1));
        Dimension dimension = new Dimension(800, 800);
        //setPreferredSize(dimension);
        setMinimumSize(dimension);

        table = new JTable(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(500, 80));
        table.setFillsViewportHeight(false);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));

        table.setDefaultRenderer(Object.class, new MyCellRenderer());

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setIdentifier(tableModel.getIdentifier(i));

        // login, id, name, x, y, creation_date, price, unit_of_measure, owner
        int[] columnSizes = new int[]{450, 300, 400, 300, 300, 650, 450, 500, 500};
        for (int i = 0; i < columnSizes.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(columnSizes[i]);

        setFormatter("id", formatters.getNumberFormat()::format);
        setFormatter("x", formatters.getDecimalFormat()::format);
        setFormatter("y", formatters.getDecimalFormat()::format);
        setFormatter("creation_date", obj -> ((LocalDateTime) obj).format(formatters.getDateTimeFormatter()));
        setFormatter("price", formatters.getCurrencyFormat()::format);
        setFormatter("unit_of_measure", formatters.getUnitFormatter());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);

        this.tableModel = tableModel;
    }

    private void setFormatter(String column, Function<Object, String> formatter) {
        table.getColumn(column).setCellRenderer(new MyCellRenderer() {
            protected void setValue(Object value) {
                setText(formatter.apply(value));
            }
        });
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void createPopupMenu(MyPopupMenu popupMenu, InputProduct inputProduct) {
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() != MouseEvent.BUTTON3) return;
                Point point = event.getPoint();
                int row = table.rowAtPoint(point);
                if(row == -1) return;
                Product product = tableModel.getRow(row);
                if (!product.getLogin().equals(login)) return;
                table.setRowSelectionInterval(row, row);
                popupMenu.setId(product.getID());
                inputProduct.setProduct(product);
                popupMenu.showMenu(event);
            }
        });
    }

    @Override
    public void updateLanguage() {
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setHeaderValue(tableModel.getColumnName(i));
        tableModel.fireTableDataChanged();
    }

    public static class MyCellRenderer extends DefaultTableCellRenderer {
        protected static final Border DEFAULT_BORDER = new EmptyBorder(1, 1, 1, 1);

        MyCellRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (comp instanceof JComponent) {
                ((JComponent)comp).setBorder(DEFAULT_BORDER);
            } return comp;
        }
    }
}