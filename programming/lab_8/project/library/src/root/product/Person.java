package root.product;

import java.io.Serializable;
import java.util.Objects;

public class Person implements Comparable<Person>, Serializable {
    private String name;
    private Float height;
    private float weight;
    private String passportID;
    private Location location;

    public boolean setPassportID(String passportID) {
        if (passportID == null || passportID.length() < 4) return false;
        this.passportID = passportID; return true;
    }

    public boolean setName(String name) {
        if (name == null || name.isEmpty()) return false;
        this.name = name; return true;
    }

    public boolean setHeight(Float height) {
        if (height == null)  {
            this.height = null;
        } else if (height <= 0) return false;
        else this.height = height;
        return true;
    }

    public boolean setWeight(float weight) throws IllegalArgumentException {
        if (weight <= 0) return false;
        this.weight = weight; return true;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Float getHeight() {
        return height;
    }

    public float getWeight() {
        return weight;
    }

    public String getPassportID() {
        return passportID;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public int compareTo(Person person) {
        return name.compareTo(Objects.requireNonNull(person,
                "Ошибка сравнения одного человека с другим по имени. В качестве аргумента передан null.")
                .getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return passportID.equals(person.passportID) &&
                name.equals(person.name) &&
                Float.compare(person.weight, weight) == 0 &&
                Objects.equals(height, person.height) &&
                Objects.equals(location, person.location);
    }
}