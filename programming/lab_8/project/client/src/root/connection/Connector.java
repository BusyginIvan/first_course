package root.connection;

import root.product_manager.ISerializableProductManager;
import root.tasks.IDisposable;
import root.tasks.ILanguageUpdateble;
import root.User;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static root.Closer.close;

public class Connector implements IDisposable, ILanguageUpdateble {
    private JOptionPane optionPane;
    private JDialog dialog;
    private final Window owner;
    private User user;
    private Runnable exit;

    private final int port;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private final Consumer<ISerializableProductManager> database;

    private final Lock lock = new ReentrantLock();
    private final Set<Thread> threads = Collections.synchronizedSet(new HashSet<>());

    public Connector(Window owner, int port, Consumer<ISerializableProductManager> database) {
        this.owner = owner;
        this.port = port;
        this.database = database;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (socket != null) {
                close(outputStream);
                close(inputStream);
                close(socket);
            }
        }));

        updateLanguage();
    }

    public void setExit(Runnable exit) {
        this.exit = exit;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean failedWriteObject(Object obj) {
        try {
            outputStream.writeObject(obj);
            return false;
        } catch (IOException | NullPointerException e) {
            return true;
        }
    }

    public void writeObject(Object obj) {
        while (failedWriteObject(obj))
            autoRegistration();
    }

    public void reset() {
        try {
            outputStream.reset();
        } catch (IOException ignored) { }
    }

    public Object tryReadObject() throws IOException {
        try {
            return inputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Object readObject() {
        while (true) {
            try {
                return tryReadObject();
            } catch (IOException | NullPointerException e) {
                //e.printStackTrace();
                autoRegistration();
            }
        }
    }

    public boolean connect() {
        try {
            //System.out.print("2.1 ");
            socket = new Socket("localhost", port);
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            //System.out.print("2.2 ");
            return true;
        } catch (IOException ignored) {
            //System.out.print("2.3 ");
            return false;
        }
    }

    private void reconnect() {
        while (true) {
            //System.out.print("2.4 ");
            dialog.setVisible(true);
            if (optionPane.getValue() == null || optionPane.getValue().equals(JOptionPane.CANCEL_OPTION))
                exit.run();
            //System.out.print("2.5 ");
            if (connect()) return;
        }
    }

    public boolean registration(boolean reg) {
        while (true) {
            //System.out.print("2.6 ");
            while (failedWriteObject(reg))
                reconnect();
            //System.out.print("2.7 ");
            if (failedWriteObject(user)) {
                reconnect();
                continue;
            }
            //System.out.print("2.8 ");
            try {
                if ((boolean) tryReadObject()) {
                    try {
                        //System.out.print("2.9 ");
                        database.accept((ISerializableProductManager) tryReadObject());
                    } catch (IOException e) {
                        e.printStackTrace();
                        //System.out.print("2.10 ");
                        reconnect();
                        continue;
                    }
                    return true;
                } return false;
            } catch (IOException e) {
                //System.out.print("2.11 ");
                reconnect();
            }
        }
    }

    public void autoRegistration() {
        //System.out.print("2.12 ");
        threads.add(Thread.currentThread());
        try {
            lock.lockInterruptibly();
            //System.out.print("2.13 ");
            //reconnect();
            if (registration(false)) {
                //System.out.print("2.14 ");
                threads.remove(Thread.currentThread());
                threads.forEach(Thread::interrupt);
                lock.unlock();
                threads.clear();
            } else {
                // message
                exit.run();
            }
        } catch (InterruptedException ignored) { }
        //System.out.print("2.15 ");
    }

    @Override
    public void dispose() {
        dialog.dispose();
    }

    @Override
    public void updateLanguage() {
        optionPane = new JOptionPane(UIManager.getString("connector.message"),
                JOptionPane.ERROR_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        dialog = optionPane.createDialog(owner, UIManager.getString("connector.title"));
        dialog.setLocationRelativeTo(null);
    }
}