package root.product;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Product implements Comparable<Product>, Serializable {
    private long ID;
    private String name;
    private Coordinates coordinates;
    private LocalDateTime creationDate = LocalDateTime.now();
    private double price;
    private UnitOfMeasure unitOfMeasure;
    private Person owner;

    private String login;

    public void setLogin(String login) {
        if (login == null)
            throw new IllegalArgumentException("Ошибка! Логин пользователя не может быть null.");
        this.login = login;
    }

    public boolean setID(long ID) {
        if (ID < 1) return false;
        this.ID = ID; return true;
    }

    public boolean setName(String name) {
        if (name == null || name.isEmpty()) return false;
        this.name = name; return true;
    }

    public boolean setPrice(double price) {
        if (price <= 0) return false;
        this.price = price; return true;
    }

    public boolean setUnitOfMeasure(Object unitOfMeasure) {
        if (unitOfMeasure == null) this.unitOfMeasure = null;
        else if (unitOfMeasure instanceof UnitOfMeasure)
            this.unitOfMeasure = (UnitOfMeasure) unitOfMeasure;
        else try {
                this.unitOfMeasure = UnitOfMeasure.valueOf(unitOfMeasure.toString());
            } catch (IllegalArgumentException e) { return false; }
        return true;
    }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public boolean setCoordinates(Coordinates coordinates) {
        if (coordinates == null) return false;
        this.coordinates = coordinates; return true;
    }

    public boolean setCreationDate(LocalDateTime creationDate) {
        if (creationDate == null) return false;
        this.creationDate = creationDate; return false;
    }

    public long getID() { return ID; }
    public String getLogin() { return login; }
    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public double getPrice() { return price; }
    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public Person getOwner() { return owner; }

    @Override
    public int compareTo(Product other) {
        return (int) (price - other.getPrice());
    }
}