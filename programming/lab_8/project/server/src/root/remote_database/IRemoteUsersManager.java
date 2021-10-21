package root.remote_database;

import root.User;

import java.sql.SQLException;

public interface IRemoteUsersManager {
    void put(User user) throws SQLException;
}