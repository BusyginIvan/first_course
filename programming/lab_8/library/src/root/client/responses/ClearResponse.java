package root.client.responses;

import root.client.IClientSkills;

import java.io.Serializable;
import java.util.function.Consumer;

public class ClearResponse implements Consumer<IClientSkills>, Serializable {
    private final String login;

    public ClearResponse(String login) {
        this.login = login;
    }

    @Override
    public void accept(IClientSkills clientSkills) {
        clientSkills.clear(login);
    }
}
