package root.response_sender;

import java.io.ObjectOutputStream;

public interface ISubscriber {
    int subscribe(ObjectOutputStream outputStream);
    void unsubscribe(int index);
}