package root.connection;

import root.client.IClientSkills;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CommandExecutor {
    private final ExecutorService executor;
    private final Connector connector;
    private final IClientSkills clientSkills;

    public CommandExecutor(Connector connector, IClientSkills clientSkills) {
        this.clientSkills = clientSkills;
        executor = Executors.newSingleThreadExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
        this.connector = connector;
    }

    public void start() {
        Thread receiver = new Thread(() -> {
            while (true) {
                Consumer<IClientSkills> response = (Consumer<IClientSkills>) connector.readObject();
                execute(() -> response.accept(clientSkills));
            }
        });
        receiver.setDaemon(true);
        receiver.start();
    }

    public void execute(Runnable command) {
        executor.execute(command);
    }
}
