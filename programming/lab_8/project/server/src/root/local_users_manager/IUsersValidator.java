package root.local_users_manager;

import root.User;

public interface IUsersValidator {
    boolean contains(String login);
    boolean contains(User user);
    boolean contains(String login, String password);
}