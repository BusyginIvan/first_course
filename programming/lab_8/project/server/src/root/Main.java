package root;

import root.response_sender.ResponseSender;
import root.local_users_manager.LocalUsersManager;
import root.product_manager.ProductManager;
import root.remote_database.RemoteDatabase;
import root.remote_database.RemoteDatabaseBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        RemoteDatabase remoteDatabase = RemoteDatabaseBuilder.newRemoteDatabase("host");

        ProductManager localDatabase = new ProductManager();
        localDatabase.setInitializationDate(remoteDatabase.getInitializationDate());
        remoteDatabase.loadCollection(localDatabase::put);
        remoteDatabase.clearOwners(localDatabase.getOwners());

        LocalUsersManager usersManager = new LocalUsersManager();
        remoteDatabase.loadUsers(usersManager::put);

        ResponseSender responseSender = new ResponseSender();
        ServerSkills serverSkills = new ServerSkills(localDatabase, remoteDatabase, usersManager, responseSender::send);
        RequestExecutor requestExecutor = new RequestExecutor(serverSkills, responseSender::send);

        Accepter accepter = new Accepter(
                PortReader.readPort("port"), Executors.newFixedThreadPool(3),
                new Connections(
                        usersManager, responseSender,
                        remoteDatabase, localDatabase,
                        requestExecutor::execute
                )::connect
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nСервер завершает работу.");
            accepter.close();
            requestExecutor.close();
            remoteDatabase.destroy();
            responseSender.close();
        }));

        while (accepter.accept());
    }
}