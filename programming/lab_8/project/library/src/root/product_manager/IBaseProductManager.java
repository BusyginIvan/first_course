package root.product_manager;

import root.product.Product;

public interface IBaseProductManager {
    boolean put(Product product);
    void remove(long id);
    void clear(String login);
}