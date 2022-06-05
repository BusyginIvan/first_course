package root.gui;

import root.product.Product;
import root.server.requests.IRequestContainingID;
import root.server.requests.concrete_requests.RemoveByIDRequest;
import root.server.requests.concrete_requests.UpdateRequest;
import root.tasks.ILanguageUpdateble;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MyPopupMenu extends JPopupMenu implements ILanguageUpdateble {
    private final Consumer<Serializable> sender;
    private long id;
    private final Map<String, JMenuItem> itemsMap = new HashMap<>();

    public MyPopupMenu(Consumer<Serializable> sender) {
        this.sender = sender;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void createItem(String code, IRequestContainingID request, InputProduct inputProduct) {
        JMenuItem menuItem = new JMenuItem();
        menuItem.addActionListener(event -> {
            if (request.takeProduct(() -> {
                inputProduct.setVisible(true);
                return inputProduct.getProduct();
            })) {
                request.setID(id);
                sender.accept(request);
            }
        });
        add(menuItem);
        itemsMap.put(code, menuItem);
    }

    @Override
    public void updateLanguage() {
        itemsMap.forEach((code, item) -> item.setText(UIManager.getString("popup_menu." + code)));
    }

    public void showMenu(MouseEvent event) {
        show(event.getComponent(), event.getX(), event.getY());
    }
}