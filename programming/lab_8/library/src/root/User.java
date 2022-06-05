package root;

import java.io.Serializable;

public class User implements Serializable {
    public String login, password;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }
}