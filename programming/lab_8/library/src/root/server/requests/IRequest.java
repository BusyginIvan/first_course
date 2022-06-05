package root.server.requests;

import root.User;
import root.product.Product;
import root.server.IServerSkills;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IRequest extends Function<IServerSkills, String[]>, Serializable {
    IRequest setUser(User user);
    default String takeWords(String... words) { return null; }
    default boolean takeProduct(Supplier<Product> supplier) { return true; }
}