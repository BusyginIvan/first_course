package root.product;

import java.io.Serializable;

public class Coordinates implements Comparable<Coordinates>, Serializable {
    private float x;
    private double y;

    public boolean setX(Float x) {
        if (x == null || x <= -230) return false;
        this.x = x; return true;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Float getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public int compareTo(Coordinates coordinates) {
        return (int)(Math.abs(x + y) - Math.abs(coordinates.x + coordinates.y));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Double.compare(that.y, y) == 0 && Float.compare(that.x, x) == 0;
    }
}