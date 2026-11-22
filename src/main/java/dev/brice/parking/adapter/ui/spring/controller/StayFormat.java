package dev.brice.parking.adapter.ui.spring.controller;

import java.time.Duration;

// Texto de cuanto tiempo estuvo un vehiculo: "2 h 15 min"
final class StayFormat {
    private StayFormat() {
    }

    static String format(Duration stay) {
        long minutes = Math.max(0, stay.toMinutes());
        long hours = minutes / 60;
        long rest = minutes % 60;
        if (hours == 0) {
            return rest + " min";
        }
        return rest == 0 ? hours + " h" : hours + " h " + rest + " min";
    }
}
