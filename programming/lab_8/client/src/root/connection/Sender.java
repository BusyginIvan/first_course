package root.connection;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Sender implements Consumer<Serializable> {
    private final ExecutorService executor;
    private final Connector connector;

    public Sender(Connector connector) {
        executor = Executors.newSingleThreadExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
        this.connector = connector;
    }

    @Override
    public void accept(Serializable serializable) {
        executor.execute(() -> {
            connector.reset();
            connector.writeObject(serializable);
        });
    }
}