package dev.brice.parking.core.constant;

import java.util.regex.Pattern;

public enum VehicleType {
    // Formato de placa en Colombia: carro = 3 letras + 3 numeros, moto = 3 letras + 2 numeros + 1 letra
    CAR("Carro", "^[A-Z]{3}[0-9]{3}$", "ABC123", "3 letras y 3 numeros"),
    MOTORCYCLE("Moto", "^[A-Z]{3}[0-9]{2}[A-Z]$", "ABC12D", "3 letras, 2 numeros y 1 letra");

    private final String visualName;
    private final Pattern platePattern;
    private final String plateExample;
    private final String plateDescription;

    VehicleType(String visualName, String platePattern, String plateExample, String plateDescription) {
        this.visualName = visualName;
        this.platePattern = Pattern.compile(platePattern);
        this.plateExample = plateExample;
        this.plateDescription = plateDescription;
    }

    public String getVisualName() {
        return visualName;
    }

    public String getPlateExample() {
        return plateExample;
    }

    public String getPlateDescription() {
        return plateDescription;
    }

    // La placa debe llegar ya en mayusculas y sin espacios
    public boolean isValidPlate(String plate) {
        return plate != null && platePattern.matcher(plate).matches();
    }
}
