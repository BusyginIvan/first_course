package root.database;

import root.formatters.Formatters;
import root.product.Product;
import root.product_manager.IReadableProductManager;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.swing.UIManager.getString;

public class Info {
    private IReadableProductManager database;
    private final Formatters formatters;

    public Info(Formatters formatters) {
        this.formatters = formatters;
    }

    public void setDatabase(IReadableProductManager database) {
        this.database = database;
    }

    public String info() {
        return getString("info.initialization_date") + ": " +
                database.getInitializationDate() + "\n" +
                getString("info.products_number") + ": " +
                database.getProducts().size();
    }

    public String printUniqueOwner() {
        String str = database.getOwners().values().stream()
                .map(formatters::ownerToString).collect(Collectors.joining("\n"));
        if (str.isEmpty()) return getString("error.unique_owner");
        return str;
    }

    public String minByOwner() {
        if (database.getProducts().size() == 0)
            return getString("error.empty");
        else {
            Optional<Product> optional = database.stream()
                    .filter(product -> product.getOwner() != null)
                    .min(Comparator.comparing(Product::getOwner));
            return optional.map(formatters::productToString).orElseGet(() -> getString("error.min_by_owner"));
        }
    }

    public String maxByCoordinates() {
        Optional<Product> optional = database.stream()
                .max(Comparator.comparing(Product::getCoordinates));
        return optional.map(formatters::productToString).orElseGet(() -> getString("error.empty"));
    }

    public String head() {
        Optional<Product> optional = database.stream().max(Product::compareTo);
        return optional.map(formatters::productToString).orElseGet(() -> getString("error.empty"));
    }
}