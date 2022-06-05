package root.product;

import java.io.Serializable;

public class Location implements Serializable {
    private float x;
    private Integer y;
    private Long z;
    private String name;

    public float getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Long getZ() {
        return z;
    }

    public String getName() {
        return name;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(Integer y) throws NullPointerException {
        if (y == null) throw new NullPointerException();
        this.y = y;
    }

    public void setZ(Long z) throws NullPointerException {
        if (z == null) throw new NullPointerException();
        this.z = z;
    }

    public void setName(String name) throws NullPointerException {
        if (name == null) throw new NullPointerException();
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Float.compare(location.x, x) == 0 &&
                y.equals(location.y) &&
                z.equals(location.z) &&
                name.equals(location.name);
    }
}