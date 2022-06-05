package root.gui.panel;

import java.awt.*;
import java.util.*;

public class ColorManager {
    private final ArrayList<Color> colors = new ArrayList<>(Arrays.asList(
            new Color(240, 170, 0), new Color(0, 170, 240),
            new Color(170, 240, 0), new Color(170, 0, 240),
            new Color(0, 240, 170), new Color(240, 0, 170)
    ));
    private final HashMap<String, Integer> users = new HashMap<>();
    private final TreeSet<Integer> nums = new TreeSet<>();

    public Color getColor(String user) {
        putUser(user);
        return colors.get(users.get(user));
    }

    public void clear() {
        users.clear();
        nums.clear();
    }

    public void remove(String user) {
        nums.remove(users.get(user));
        users.remove(user);
    }

    private void putUser(String user) {
        if (users.containsKey(user)) return;
        Iterator<Integer> iterator = nums.iterator();
        int i = 0;
        while (iterator.hasNext() && iterator.next() == i) i++;
        if (i >= colors.size()) colors.add(newColor());
        nums.add(i); users.put(user, i);
    }

    private Color newColor() {
        return new Color(random(), random(), random());
    }

    private int random() {
        return (int) (Math.random() * 150 + 50);
    }
}