package root.gui;

import root.formatters.Formatters;
import root.formatters.UnitsOfMeasure;
import root.product.*;
import root.tasks.IDisposable;
import root.tasks.ILanguageUpdateble;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static javax.swing.UIManager.getString;

public class InputProduct extends JDialog implements ILanguageUpdateble, IDisposable {
    private final Map<String, Line> lines = new HashMap<>();
    private final JButton button;
    private Product product;

    private final JLabel measureLabel;
    private final UnitsOfMeasure unitsOfMeasure;
    private final JComboBox<UnitsOfMeasure.Unit> unitsBox;

    public InputProduct(Frame owner, UnitsOfMeasure unitsOfMeasure, Formatters formatters) {
        super(owner, true);
        this.unitsOfMeasure = unitsOfMeasure;
        Dimension dimension = new Dimension(580, 620);
        setSize(dimension); setMinimumSize(dimension);
        setLocationRelativeTo(null);
        setContentPane(Box.createVerticalBox());

        // Характеристики товара.

        lines.put("name", new Line(new JTextField()));

        newNumberField("x", formatters::getDecimalFormat,
                formatter -> formatter.setMinimum(-230f), 0f, null);
        newNumberField("y", formatters::getDecimalFormat,
                formatter -> formatter.setValueClass(Double.class), 0.0, null);

        NumberFormatter priceFormatter = new NumberFormatter(formatters.getCurrencyFormat());
        priceFormatter.setMinimum(0.0);
        JFormattedTextField priceField = createNumberField(priceFormatter, 0.0);
        lines.put("price", new Line(priceField));

        unitsBox = new JComboBox<>(unitsOfMeasure.getUnits());
        measureLabel = newLine(unitsBox);

        // Характеристики владельца.

        Set<Line> ownerLines = new HashSet<>();
        Set<Line> locationLines = new HashSet<>();
        JTextField locationName = new JTextField();

        JTextField passportField = new JTextField();
        lines.put("passport", new Line(passportField));
        passportField.addCaretListener(event -> {
            if (passportField.getText().isEmpty()) {
                ownerLines.forEach(line -> line.setEditable(false));
                locationLines.forEach(line -> line.setEditable(false));
            } else {
                ownerLines.forEach(line -> line.setEditable(true));
                if (!locationName.getText().isEmpty())
                    locationLines.forEach(line -> line.setEditable(true));
            }
        });

        lines.put("owner.name", new Line(new JTextField()));
        ownerLines.add(lines.get("owner.name"));

        newNumberField("owner.height", formatters::getDecimalFormat,
                formatter -> formatter.setMinimum(0f),
                null, ownerLines);
        newNumberField("owner.weight", formatters::getDecimalFormat,
                formatter -> formatter.setMinimum(0f),
                0f, ownerLines);

        // Характеристики локации.

        lines.put("location.name", new Line(locationName));
        locationName.addCaretListener(event -> {
            if (locationName.getText().isEmpty())
                locationLines.forEach(line -> line.setEditable(false));
            else if (!passportField.getText().isEmpty())
                locationLines.forEach(line -> line.setEditable(true));
        });
        ownerLines.add(lines.get("location.name"));

        newNumberField("location.x", formatters::getDecimalFormat,
                formatter -> formatter.setValueClass(Float.class),
                0f, locationLines);
        newNumberField("location.y", formatters::getNumberFormat,
                formatter -> formatter.setValueClass(Integer.class),
                0, locationLines);
        newNumberField("location.z", formatters::getNumberFormat,
                formatter -> formatter.setValueClass(Long.class),
                0, locationLines);

        if (product == null || product.getOwner() == null) {
            ownerLines.forEach(line -> line.setEditable(false));
            locationLines.forEach(line -> line.setEditable(false));
        } else if (product.getOwner().getLocation() == null)
            locationLines.forEach(line -> line.setEditable(false));

        // Кнопка проверки и завершения ввода.

        getContentPane().add(Box.createGlue());
        button = new JButton();
        getContentPane().add(button);
        getContentPane().add(Box.createGlue());

        button.addActionListener(event -> {
            Product product = new Product();
            if (!product.setName(getText("name"))) {
                drawError("name"); return;
            }
            Coordinates coordinates = new Coordinates();
            coordinates.setX((float) getValue("x"));
            coordinates.setY((double) getValue("y"));
            product.setCoordinates(coordinates);
            if (!product.setPrice((double) priceField.getValue())) {
                drawError("price"); return;
            }
            product.setUnitOfMeasure(((UnitsOfMeasure.Unit) unitsBox.getSelectedItem()).getUnitOfMeasure());

            if (getText("passport").isEmpty()) {
                this.product = product;
                setVisible(false);
                return;
            }

            Person person = new Person();
            if (!person.setPassportID(getText("passport"))) {
                drawError("passport"); return;
            }
            if (!person.setName(getText("owner.name"))) {
                drawError("owner.name"); return;
            }
            person.setHeight((Float) getValue("owner.height"));
            person.setWeight((float) getValue("owner.weight"));
            product.setOwner(person);

            if (getText("location.name").isEmpty()) {
                this.product = product;
                setVisible(false);
                return;
            }

            Location location = new Location();
            location.setName(locationName.getText());
            location.setX((float) getValue("location.x"));
            location.setY((int) getValue("location.y"));
            location.setZ((long) getValue("location.z"));
            person.setLocation(location);

            this.product = product;
            setVisible(false);
        });

        updateLanguage();
    }

    @Override
    public void updateLanguage() {
        setTitle(getString("InputProduct.title"));
        lines.forEach((code, line) -> line.updateLanguage(code));
        button.setText(getString("InputProduct.button"));
        measureLabel.setText(getString("InputProduct.measureLabel"));
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            if (product == null) {
                setProductValues("", 0f, 0.0, 0.0, null);
                setOwnerValues("", "", 0f, 0f);
            } else {
                setProductValues(product.getName(),
                        product.getCoordinates().getX(), product.getCoordinates().getY(),
                        product.getPrice(), product.getUnitOfMeasure());
                Person owner = product.getOwner();
                if (owner == null) {
                    setOwnerValues("", "", 0f, 0f);
                    setLocationValues("", 0f, 0, 0L);
                } else {
                    setOwnerValues(owner.getPassportID(), owner.getName(),
                            owner.getHeight(), owner.getWeight());
                    Location loc = owner.getLocation();
                    if (loc == null)
                        setLocationValues("", 0f, 0, 0L);
                    else
                        setLocationValues(loc.getName(), loc.getX(), loc.getY(), loc.getZ());
                }
                product = null;
            }
        }
        super.setVisible(visible);
    }

    private void setProductValues(String name, float x, double y, double price, UnitOfMeasure unit) {
        setText("name", name);
        setValue("x", x);
        setValue("y", y);
        setValue("price", price);
        unitsBox.setSelectedItem(unitsOfMeasure.getUnit(unit));
    }

    private void setOwnerValues(String passport, String name, Float h, Float w) {
        setText("passport", passport);
        setText("owner.name", name);
        setValue("owner.height", h);
        setValue("owner.weight", w);
    }

    private void setLocationValues(String name, Float x, Integer y, Long z) {
        setText("location.name", name);
        setValue("location.x", x);
        setValue("location.y", y);
        setValue("location.z", z);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    private void drawError(String key) {
        lines.get(key).invalidText = true;
        lines.get(key).layer.getView().repaint();
    }

    private String getText(String key) {
        return lines.get(key).layer.getView().getText();
    }

    private void setText(String key, String text) {
        lines.get(key).layer.getView().setText(text);
    }

    private Object getValue(String key) {
        return ((JFormattedTextField) lines.get(key).layer.getView()).getValue();
    }

    private void setValue(String key, Object obj) {
        ((JFormattedTextField) lines.get(key).layer.getView()).setValue(obj);
    }

    private void newNumberField(String code, Supplier<NumberFormat> supplier,
                                Consumer<NumberFormatter> consumer,
                                Number def, Set<Line> lineSet) {
        NumberFormatter formatter = new NumberFormatter();
        consumer.accept(formatter);
        JFormattedTextField field = createNumberField(formatter, def);
        lines.put(code, new Line(field, ()->{
            formatter.setFormat(supplier.get());
            formatter.install(field);
        }));
        if (lineSet != null) lineSet.add(lines.get(code));
    }

    private JFormattedTextField createNumberField(NumberFormatter formatter, Number def) {
        formatter.setAllowsInvalid(false);
        JFormattedTextField field = new JFormattedTextField(formatter);
        if (def != null) field.setValue(def);
        return field;
    }

    private JLabel newLine(JComponent component) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(160, 20));
        label.setHorizontalAlignment(SwingConstants.RIGHT);

        getContentPane().add(Box.createGlue());
        FlowLayout flowLayout = new FlowLayout(); flowLayout.setHgap(10);
        JPanel panel = new JPanel(flowLayout); getContentPane().add(panel);
        panel.add(label); panel.add(component);

        return label;
    }

    private class Line {
        JLabel label;
        JLayer<JTextField> layer;
        Runnable fieldUpdate;
        boolean invalidText = false;

        Line(JTextField field, Runnable fieldUpdate) {
            this.fieldUpdate = fieldUpdate;

            field.setColumns(20);
            field.addCaretListener(event -> {
                invalidText = false;
                field.repaint();
            });

            layer = new JLayer<>(field, new MyLayer());
            label = newLine(layer);
        }

        Line(JTextField field) { this(field, null); }

        void updateLanguage(String code) {
            label.setText(getString("InputProduct." + code));
            if (fieldUpdate != null) fieldUpdate.run();
        }

        void setColor(Color color) {
            label.setForeground(color);
            layer.getView().setForeground(color);
        }

        void setEditable(boolean editable) {
            layer.getView().setEditable(editable);
            if (editable) setColor(Color.BLACK);
            else setColor(Color.GRAY);
        }

        class MyLayer extends LayerUI<JTextField> {
            @Override
            public void paint(Graphics graphics, JComponent component) {
                super.paint (graphics, component);

                if (invalidText) {
                    Graphics2D graphics2D = (Graphics2D) graphics.create();

                    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = component.getWidth(), h = component.getHeight();
                    int r = 6, pad = 5;
                    int x = w - pad - r, y = h / 2;
                    int p =  (int) Math.rint(r / Math.sqrt(2)) - 1;
                    graphics2D.setPaint(Color.red);
                    graphics2D.fillOval(x - r, y - r, 2 * r, 2 * r);
                    graphics2D.setPaint(Color.white);
                    graphics2D.drawLine(x - p, y - p, x + p - 1, y + p - 1);
                    graphics2D.drawLine(x - p, y + p - 1, x + p - 1, y - p);

                    graphics2D.dispose();
                }
            }
        }
    }
}