package root.product_manager;

import root.product.Person;
import root.product.Product;

import java.time.LocalDate;
import java.util.stream.Stream;

public interface IUpdatebleProductManager extends IBaseProductManager {
    void setInitializationDate(LocalDate initializationDate);
    boolean invalidOwner(Person owner);
    Stream<Product> stream();
    Product get(long id);
}