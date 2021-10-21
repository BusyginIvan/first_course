package root.client.responses;

import root.client.IClientSkills;

import java.io.Serializable;
import java.util.function.Consumer;
import root.product.Product;

public class PutResponse implements Consumer<IClientSkills>, Serializable {
    private final Product product;

    public PutResponse(Product product) {
        this.product = product;
    }

    @Override
    public void accept(IClientSkills clientSkills) {
        clientSkills.put(product);
    }
}
