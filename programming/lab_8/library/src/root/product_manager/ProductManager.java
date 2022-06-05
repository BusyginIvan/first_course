package root.product_manager;

import root.product.Person;
import root.product.Product;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

public class ProductManager implements ISerializableProductManager, IUpdatebleProductManager {
    private LocalDate initializationDate;
    private final SortedMap<Long, Product> products;
    private final Map<String, Person> owners;

    public ProductManager() {
        products = Collections.synchronizedSortedMap(new TreeMap<>());
        owners = Collections.synchronizedMap(new HashMap<>());
        initializationDate = LocalDate.now();
    }

    private boolean unableToAddOwner(Product product) {
        if (product.getOwner() == null) return false;
        if (owners.containsValue(product.getOwner()))
            product.setOwner(owners.get(product.getOwner().getPassportID()));
        else if (owners.containsKey(product.getOwner().getPassportID())) return true;
        else owners.put(product.getOwner().getPassportID(), product.getOwner());
        return false;
    }

    private void deleteOwner(Person owner) {
        if (owner != null)
            if (owners.containsKey(owner.getPassportID())) {
                for (Product products: products.values())
                    if (products.getOwner() != null && products.getOwner().getPassportID().equals(owner.getPassportID()))
                        return;
                owners.remove(owner.getPassportID());
            }
    }

    @Override
    public boolean put(Product product) {
        if (unableToAddOwner(product)) return false;
        products.put(product.getID(), product);
        return true;
    }

    @Override
    public void remove(long id) {;
        Product product = products.remove(id);
        if (product != null) deleteOwner(product.getOwner());
    }

    @Override
    public void clear(String login) {
        products.values().removeIf(product -> product.getLogin().equals(login));
    }

    @Override
    public Product get(long id) {
        return products.get(id);
    }

    @Override
    public Map<Long, Product> getProducts() {
        return products;
    }

    @Override
    public Map<String, Person> getOwners() {
        return owners;
    }

    @Override
    public String toString() {
        return "Список товаров (элементов типа Product) создан " + initializationDate.toString() +
                ". Текущее количество товаров: " + products.size() + ".";
    }

    @Override
    public void setInitializationDate(LocalDate initializationDate) {
        this.initializationDate = initializationDate;
    }

    @Override
    public LocalDate getInitializationDate() {
        return initializationDate;
    }

    @Override
    public boolean invalidOwner(Person owner) {
        if (owner == null) return false;
        if (owners.containsValue(owner)) return false;
        return owners.containsKey(owner.getPassportID());
    }

    @Override
    public Stream<Product> stream() {
        return products.values().stream();
    }

    @Override
    public Iterator<Product> iterator() {
        return products.values().iterator();
    }
}