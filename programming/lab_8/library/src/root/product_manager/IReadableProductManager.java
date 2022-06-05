package root.product_manager;

import root.product.Person;
import root.product.Product;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

public interface IReadableProductManager extends Iterable<Product> {
    Map<String, Person> getOwners();
    Map<Long, Product> getProducts();
    Stream<Product> stream();
    LocalDate getInitializationDate();
    Product get(long id);
}