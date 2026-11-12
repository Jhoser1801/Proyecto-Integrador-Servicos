package dev.brice.parking.core.constant;

public enum UserRole {
    ADMIN("Administrador"),
    OPERATOR("Operador");

    private final String visualName;

    UserRole(String visualName) {
        this.visualName = visualName;
    }

    public String getVisualName() {
        return visualName;
    }
}
