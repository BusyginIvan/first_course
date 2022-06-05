package root;

import root.server.IServerSkills;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class RequestExecutor {
    private final ExecutorService executorService;
    private final IServerSkills serverSkills;
    private final BiConsumer<Integer, String[]> responseSender;

    public RequestExecutor(IServerSkills serverSkills, BiConsumer<Integer, String[]> responseSender) {
        this.responseSender = responseSender;
        executorService = Executors.newSingleThreadExecutor();
        this.serverSkills = serverSkills;
    }

    public void execute(int index, Function<IServerSkills, String[]> command) {
        executorService.execute(() -> responseSender.accept(index, command.apply(serverSkills)));
    }

    public void close() {
        executorService.shutdown();
    }
}