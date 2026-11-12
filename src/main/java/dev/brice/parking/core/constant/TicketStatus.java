package dev.brice.parking.core.constant;

public enum TicketStatus {
    OPEN("Dentro"),
    CLOSED("Cerrado");

    private final String visualName;

    TicketStatus(String visualName) {
        this.visualName = visualName;
    }

    public String getVisualName() {
        return visualName;
    }
}
