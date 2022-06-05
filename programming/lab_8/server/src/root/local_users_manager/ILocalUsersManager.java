package root.local_users_manager;

import root.User;

public interface ILocalUsersManager extends IUsersValidator {
    boolean put(User user);
    boolean put(String login, String password);
    boolean invalidUser(User user);
}