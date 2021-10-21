package root.client.responses;

import root.client.IClientSkills;

import java.io.Serializable;
import java.util.function.Consumer;

public class RemoveResponse implements Consumer<IClientSkills>, Serializable {
    private final long id;

    public RemoveResponse(long id) {
        this.id = id;
    }

    @Override
    public void accept(IClientSkills clientSkills) {
        clientSkills.remove(id);
    }
}
