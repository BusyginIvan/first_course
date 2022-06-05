package root.remote_database;

import root.product.Product;

public interface IRemoteProductManager {
    boolean clear(String login);
    boolean add(Product product);
    boolean update(Product product);
    boolean remove(long id);
}