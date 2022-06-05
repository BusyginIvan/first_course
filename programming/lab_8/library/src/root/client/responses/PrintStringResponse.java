package root.client.responses;

import root.client.IClientSkills;

import java.io.Serializable;
import java.util.function.Consumer;

public class PrintStringResponse implements Consumer<IClientSkills>, Serializable {
    private final String[] messages;

    public PrintStringResponse(String... messages) {
        this.messages = messages;
    }

    @Override
    public void accept(IClientSkills clientSkills) {
        clientSkills.error(messages);
    }
}
