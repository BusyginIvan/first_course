package root.local_users_manager;

import root.StringEncoder;
import root.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LocalUsersManager implements ILocalUsersManager {
    private final Map<String, String> users;

    public LocalUsersManager() {
        users = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public boolean put(String login, String password) {
        if (invalidUser(new User(login, password))) return false;
        users.put(login, password);
        return true;
    }

    @Override
    public boolean put(User user) {
        return put(user.login, user.password);
    }

    @Override
    public boolean contains(String login) {
        return users.containsKey(login);
    }

    @Override
    public boolean contains(User user) {
        return contains(user.login, user.password);
    }

    @Override
    public boolean contains(String login, String password) {
        return users.containsKey(login) && users.get(login).equals(StringEncoder.encrypt(password));
    }

    @Override
    public boolean invalidUser(User user) {
        return user.login == null || user.login.isEmpty() || user.password == null || user.password.isEmpty();
    }
}