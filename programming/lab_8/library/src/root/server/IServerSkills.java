package root.server;

import root.User;
import root.product.Product;

import java.io.Serializable;

public interface IServerSkills {
    String[] removeFirst(User user);
    String[] clear(User user);
    String[] removeByID(long id, User user);
    String[] add(Product product, String password);
    String[] addIfMax(Product product, String password);
    String[] update(Product product, String password);
}