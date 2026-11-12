package dev.brice.parking.core.constant;

public enum SlotStatus {
    FREE("Libre"),
    OCCUPIED("Ocupado");

    private final String visualName;

    SlotStatus(String visualName) {
        this.visualName = visualName;
    }

    public String getVisualName() {
        return visualName;
    }
}
