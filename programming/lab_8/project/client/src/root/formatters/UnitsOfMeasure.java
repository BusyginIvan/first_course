package root.formatters;

import root.product.UnitOfMeasure;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class UnitsOfMeasure {
    private final Map<UnitOfMeasure, Unit> units = new HashMap<>();

    public UnitsOfMeasure() {
        new Unit(null, "null");
        new Unit(UnitOfMeasure.KILOGRAMS, "kilograms");
        new Unit(UnitOfMeasure.GRAMS, "grams");
        new Unit(UnitOfMeasure.LITERS, "liters");
        new Unit(UnitOfMeasure.SQUARE_METERS, "square_meters");
    }

    public Unit[] getUnits() {
        return units.values().toArray(new Unit[0]);
    }

    public Unit getUnit(UnitOfMeasure unit) {
        return units.get(unit);
    }

    public String unitToString(UnitOfMeasure unit) {
        return units.get(unit).toString();
    }

    public class Unit {
        private final UnitOfMeasure unitOfMeasure;
        private final String resource;

        private Unit(UnitOfMeasure unitOfMeasure, String resource) {
            this.unitOfMeasure = unitOfMeasure;
            this.resource = resource;
            units.put(unitOfMeasure, this);
        }

        public UnitOfMeasure getUnitOfMeasure() {
            return unitOfMeasure;
        }

        public String toString() {
            return UIManager.getString("unit_of_measure." + resource);
        }
    }
}