package root.formatters;

import root.product.Person;
import root.product.Product;
import root.tasks.ILanguageUpdateble;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static javax.swing.UIManager.getString;

public class Formatters implements ILanguageUpdateble {
    private NumberFormat decimalFormat = new DecimalFormat("0.0####");
    private NumberFormat numberFormat = NumberFormat.getInstance();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    private final Function<Object, String> unitFormatter;

    public Formatters(Function<Object, String> unitFormatter) {
        this.unitFormatter = unitFormatter;
    }

    @Override
    public void updateLanguage() {
        decimalFormat = new DecimalFormat("0.0####");
        numberFormat = NumberFormat.getInstance();
        dateTimeFormatter = dateTimeFormatter.withLocale(Locale.getDefault());
    }

    public NumberFormat getDecimalFormat() {
        return decimalFormat;
    }

    public NumberFormat getCurrencyFormat() {
        return currencyFormat;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public Function<Object, String> getUnitFormatter() {
        return unitFormatter;
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    public String productToString(Product product) {
        UnaryOperator<String> f = str -> getString("product." + str);
        return  f.apply("login") + ": " + product.getLogin() + "\n"
                + f.apply("id") + ": " + numberFormat.format(product.getID()) + "\n"
                + f.apply("name") + ": " + product.getName() + "\n"
                + f.apply("coordinates") + ":\n"
                + " x: " + numberFormat.format(product.getCoordinates().getX()) + "\n"
                + " y: " + numberFormat.format(product.getCoordinates().getY()) + "\n"
                + f.apply("creation_date") + ": " + product.getCreationDate().format(dateTimeFormatter) + "\n"
                + f.apply("price") + ": " + currencyFormat.format(product.getPrice()) + "\n"
                + f.apply("unit_of_measure") + ": " + unitFormatter.apply(product.getUnitOfMeasure()) + "\n"
                + f.apply("owner") + ": " + (product.getOwner() == null ? "отсутствует" : ownerToString(product.getOwner()));
    }

    public String ownerToString(Person owner) {
        return owner.getName() + " (" + getString("owner.passport") + " - " + owner.getPassportID() + ")";
    }
}