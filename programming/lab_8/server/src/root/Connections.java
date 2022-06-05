package root;

import root.response_sender.ISubscriber;
import root.local_users_manager.ILocalUsersManager;
import root.product_manager.IReadableProductManager;
import root.remote_database.IRemoteUsersManager;
import root.server.IServerSkills;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static root.StringEncoder.encrypt;

public class Connections {
    private final ILocalUsersManager localUsersManager;
    private final IRemoteUsersManager remoteUsersManager;
    private final IReadableProductManager productManager;
    private final ISubscriber subscriber;
    private final BiConsumer<Integer, Function<IServerSkills, String[]>> requestExecutor;
    private final Object LOCK = new Object();

    public Connections(ILocalUsersManager localUsersManager,
                       ISubscriber subscriber,
                       IRemoteUsersManager remoteUsersManager,
                       IReadableProductManager productManager,
                       BiConsumer<Integer, Function<IServerSkills, String[]>> requestExecutor) {
        this.productManager = productManager;
        this.localUsersManager = localUsersManager;
        this.remoteUsersManager = remoteUsersManager;
        this.subscriber = subscriber;
        this.requestExecutor = requestExecutor;
    }

    public void connect(Socket socket) {
        new Connection(socket);
    }

    private class Connection {
        private ObjectOutputStream outputStream;
        private ObjectInputStream inputStream;
        private int index;
        private User user;

        private Connection(Socket socket) {
            try {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());

                if (registration()) {
                    outputStream.writeObject(productManager);
                    index = subscriber.subscribe(outputStream);
                    while (true) {
                        Object request = inputStream.readObject();
                        if (request == null) break;
                        requestExecutor.accept(index, (Function<IServerSkills, String[]>) request);
                    }
                    System.out.println("Клиент '" + user.login + "' отключился.");
                } else System.out.println("Клиент отключился.");
            } catch (IOException | ClassNotFoundException | SQLException e) {
                //e.printStackTrace();
                System.out.println("Потеряно соединение с клиентом.");
            }

            subscriber.unsubscribe(index);
            Closer.close(outputStream);
            Closer.close(inputStream);
            Closer.close(socket);
        }

        private boolean registration() throws IOException, ClassNotFoundException, SQLException {
            while (true) {
                Boolean reg = (Boolean) inputStream.readObject();
                if (reg == null) return false;
                user = (User) inputStream.readObject();
                if (user == null) return false;
                synchronized (LOCK) {
                    if (reg) {
                        if (localUsersManager.contains(user.login) || localUsersManager.invalidUser(user))
                            outputStream.writeObject(false);
                        else {
                            outputStream.writeObject(true);
                            user.password = encrypt(user.password);
                            synchronized (remoteUsersManager) {
                                remoteUsersManager.put(user);
                            }
                            localUsersManager.put(user);
                            return true;
                        }
                    } else {
                        if (localUsersManager.contains(user)) {
                            outputStream.writeObject(true);
                            return true;
                        } else
                            outputStream.writeObject(false);
                    }
                }
            }
        }
    }
}