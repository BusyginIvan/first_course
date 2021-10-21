package root.response_sender;

import root.client.responses.PrintStringResponse;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResponseSender implements ISubscriber {
    private final ExecutorService executorService;
    private final SortedMap<Integer, ObjectOutputStream> listeners;

    public ResponseSender() {
        executorService = Executors.newSingleThreadExecutor();
        listeners = Collections.synchronizedSortedMap(new TreeMap<>());
    }

    @Override
    public int subscribe(ObjectOutputStream outputStream) {
        int index = 1;
        for (int key: listeners.keySet()) {
            if (index < key) break;
            index++;
        }
        listeners.put(index, outputStream);
        return index;
    }

    @Override
    public void unsubscribe(int index) {
        listeners.remove(index);
    }

    public void send(int index, String... messages) {
        if (messages == null) return;
        executorService.execute(() -> {
            try {
                listeners.get(index).writeObject(new PrintStringResponse(messages));
            } catch (IOException ignored) { }
        });
    }

    public void send(Serializable object) {
        if (object == null) return;
        executorService.execute(() -> listeners.values().forEach(outputStream -> {
            try {
                outputStream.writeObject(object);
            } catch (IOException ignored) { }
        }));
    }

    public void close() {
        executorService.shutdown();
    }
}