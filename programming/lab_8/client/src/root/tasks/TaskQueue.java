package root.tasks;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

public class TaskQueue<T> {
    private final Queue<T> tasks = new ArrayDeque<>();
    private final Consumer<T> action;

    public TaskQueue(Consumer<T> action) {
        this.action = action;
    }

    public void add(T task) {
        tasks.add(task);
    }

    public void remove() {
        tasks.remove();
    }

    public void execute() {
        tasks.forEach(action);
    }
}