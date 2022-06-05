package root;

public class Closer {
    public static void close(AutoCloseable cloneable) {
        try {
            cloneable.close();
        } catch (Exception ignored) {}
    }
}