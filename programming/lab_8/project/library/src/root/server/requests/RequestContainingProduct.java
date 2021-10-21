package root.server.requests;

import root.product.Product;

import java.util.function.Supplier;

public abstract class RequestContainingProduct extends RequestContainingUser {
    protected Product product;

    @Override
    public boolean takeProduct(Supplier<Product> supplier) {
        product = supplier.get();
        if (product == null) return false;
        product.setLogin(user.login);
        return true;
    }
}