package root;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class Accepter {
    private final ExecutorService executorService;
    private final ServerSocket serverSocket;
    private final Consumer<Socket> connections;

    public Accepter(int port, ExecutorService executorService, Consumer<Socket> connections) throws IOException {
        this.connections = connections;
        this.executorService = executorService;
        serverSocket = new ServerSocket(port);
        System.out.println("Сервер будет прослушивать порт " + serverSocket.getLocalPort() + "\n");
    }

    public boolean accept() {
        try {
            System.out.println("Ожидаем подключения...");
            Socket socket = serverSocket.accept();
            System.out.println("Принято подключение.");
            executorService.execute(() -> connections.accept(socket));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void close() {
        Closer.close(serverSocket);
        executorService.shutdown();
    }
}