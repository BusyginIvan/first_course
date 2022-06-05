package root.gui.panel;

import root.gui.InputProduct;
import root.gui.MyPopupMenu;
import root.product.Coordinates;
import root.product.Product;
import root.product_manager.IReadableProductManager;
import root.tasks.ILanguageUpdateble;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.Math.*;

public class MyPanel extends JPanel implements ILanguageUpdateble {
    private final int indent = 10;
    private float minX, minY, maxX, maxY;
    private int w, h, r;
    private Graphics2D g2d;

    private final JLabel zeroLabel = createLabel(), maxXLabel = createLabel(), maxYLabel = createLabel();
    private final Map<Long, MyComponent> components = Collections.synchronizedMap(new HashMap<>());

    private final ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
    private final Function<Product, String> productToString;

    private String login;
    private InputProduct inputProduct;
    private MyPopupMenu popupMenu;

    private IReadableProductManager database;
    private final ColorManager colorManager;

    public MyPanel(ColorManager colorManager, Function<Product, String> productToString) {
        this.colorManager = colorManager;
        this.productToString = productToString;
        Dimension dimension = new Dimension(800, 800);
        setMinimumSize(dimension);
        setBackground(Color.WHITE);
        setLayout(null);
        toolTipManager.setDismissDelay(20000);
        toolTipManager.setInitialDelay(100);
    }

    private JLabel createLabel() {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(140, 20));
        label.setSize(label.getPreferredSize());
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        add(label);
        return label;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setMenuAndInput(MyPopupMenu popupMenu, InputProduct inputProduct) {
        this.popupMenu = popupMenu;
        this.inputProduct = inputProduct;
    }

    public void setDatabase(IReadableProductManager database) {
        colorManager.clear();
        components.values().forEach(this::remove);
        components.clear();
        this.database = database;
        database.stream().map(Product::getID).forEach(this::put);
    }

    public void put(long id) {
        if (components.containsKey(id))
            components.get(id).updateToolTip();
        else {
            components.put(id, new MyComponent(id));
            add(components.get(id));
        }
    }

    public void remove(long id, String login) {
        rem(id);
        if (database.stream().noneMatch(product -> product.getLogin().equals(login)))
            colorManager.remove(login);
    }

    public void clear(String login) {
        colorManager.remove(login);
        database.stream().filter(product -> product.getLogin().equals(login))
                .map(Product::getID).forEach(this::rem);
    }

    private void rem(long id) {
        components.get(id).dispose();
    }

    @Override
    public void updateLanguage() {
        components.values().forEach(MyComponent::updateToolTip);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (database == null) return;

        int graphicsWidth = getWidth() - 2 * indent, graphicsHeight = getHeight() - 2 * indent;
        g2d = (Graphics2D) g.create(indent, indent, graphicsWidth, graphicsHeight);

        r = min(graphicsWidth, graphicsHeight) / 25;
        components.values().forEach(component -> component.setSize(2 * r, 2 * r));
        recalculation();

        w = graphicsWidth - 2 * r; h = graphicsHeight - 2 * r;
        int ox = r + normX(minX), oy = r + normY(maxY);

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.BLACK);
        drawArrow(ox, graphicsHeight - 1, ox, 0);
        drawArrow(0, oy, graphicsWidth - 1, oy);
        int l = 5;
        g2d.drawLine(graphicsWidth - r, oy - l, graphicsWidth - r, oy + l);
        g2d.drawLine(ox - l, r, ox + l, r);

        int labelIndent = 15;
        zeroLabel.setLocation(indent + ox - labelIndent - zeroLabel.getWidth(), indent + oy + labelIndent);
        zeroLabel.setText("0");
        maxXLabel.setLocation(getWidth() - indent - maxXLabel.getWidth(), indent + oy + labelIndent);
        maxXLabel.setText(Float.toString(maxX));
        maxYLabel.setLocation(indent + ox - labelIndent - maxYLabel.getWidth(), indent + 17);
        maxYLabel.setText(Float.toString(maxY));

        g2d.dispose();
    }

    private int normX(float x) {
        return (int) (w * x / (maxX + minX));
    }

    private int normY(double y) {
        return (int) (h * y / (maxY + minY));
    }

    private void drawArrow(int x1, int y1, int x2, int y2) {
        double len = 20, fi = Math.PI * 3 / 4;
        g2d.drawLine(x1, y1, x2, y2);
        Complex dir = Complex.byParts(x2 - x1, y2 - y1).direction().scale(len);
        Complex c1 = dir.rotation(fi), c2 = dir.rotation(-fi);
        g2d.drawLine(x2, y2, x2 + (int) c1.re(), y2 + (int) c1.im());
        g2d.drawLine(x2, y2, x2 + (int) c2.re(), y2 + (int) c2.im());
    }

    private void recalculation() {
        float def = 100f;

        Supplier<Stream<Float>> xStream = () -> components.values().stream()
                .map(component -> component.coordinates.getX());
        if (xStream.get().count() == 0) {
            minX = -def; maxX = def;
        } else {
            minX = -min(-def, xStream.get().min(Float::compare).get());
            maxX = max(def, xStream.get().max(Float::compare).get());
        }

        Supplier<Stream<Double>> yStream = () -> components.values().stream()
                .map(component -> component.coordinates.getY());
        if (yStream.get().count() == 0) {
            minY = -def; maxY = def;
        } else {
            minY = (float) -min(-def, yStream.get().min(Double::compare).get());
            maxY = (float) max(def, yStream.get().max(Double::compare).get());
        }

        int k = 9;
        if (minX < maxX / k) minX = maxX / k;
        else if (maxX < minX / k) maxX = minX / k;
        if (minY < maxY / k) minY = maxY / k;
        else if (maxY < minY / k) maxY = minY / k;
    }

    @Override
    public void paintChildren(Graphics graphics) {
        Arrays.stream(getComponents()).forEach(c ->
                c.paint(graphics.create(c.getX(), c.getY(), c.getWidth(), c.getHeight()))
        );
    }

    class MyComponent extends JComponent {
        final long id;
        JLabel label;
        Color color;
        int alpha;
        Coordinates coordinates;

        MyComponent(long id) {
            this.id = id;
            color = colorManager.getColor(database.get(id).getLogin());
            coordinates = database.get(id).getCoordinates();

            label = new JLabel(Long.toString(id));
            label.setPreferredSize(new Dimension(50, 20));
            label.setSize(label.getPreferredSize());
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground(Color.BLACK);
            add(label);

            updateToolTip();
            toolTipManager.registerComponent(this);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    if (event.getButton() != MouseEvent.BUTTON3) return;
                    Product product = database.get(id);
                    if (product == null) return;
                    if (!product.getLogin().equals(login)) return;
                    popupMenu.setId(id);
                    inputProduct.setProduct(product);
                    popupMenu.showMenu(event);
                }
            });

            alpha = 0;
            init();
        }

        void updateToolTip() {
            setToolTipText("<html>" +
                    productToString.apply(database.get(id)).replaceAll("\n", "<br>")
                    + "</html>");
        }

        Timer timer;

        void init() {
            timer = new Timer(20, event -> {
                alpha += 5;
                MyComponent.this.repaint();
                if (alpha >= 255) timer.stop();
            });
            timer.start();
        }

        void dispose() {
            timer = new Timer(15, event -> {
                alpha -= 5;
                MyComponent.this.repaint();
                if (alpha <= 0) {
                    MyPanel.this.remove(MyComponent.this);
                    components.remove(id);
                    MyPanel.this.repaint();
                    timer.stop();
                }
            });
            timer.start();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (database == null) return;
            Graphics2D g2d = (Graphics2D) g.create();

            if (database.get(id) != null)
                coordinates = database.get(id).getCoordinates();

            setLocation(normX(coordinates.getX() + minX) + indent,
                    MyPanel.this.getHeight() - indent - normY(coordinates.getY() + minY) - 2 * r);
            label.setLocation(r - label.getWidth() / 2, r - label.getHeight() / 2);

            Color black = new Color(alpha << 24, true);
            label.setForeground(black);

            g2d.setColor(new Color(color.getRGB() & 0x00FFFFFF | (alpha << 24), true));
            g2d.fillOval(1, 1, getWidth() - 3, getHeight() - 3);
            g2d.setColor(black);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(1, 1, getWidth() - 2, getHeight() - 2);

            g2d.dispose();
        }

        @Override
        public boolean contains(Point point) {
            return contains(point.x, point.y);
        }

        @Override
        public boolean contains(int x, int y) {
            return Math.hypot(x - r, y - r) < r;
        }
    }
}