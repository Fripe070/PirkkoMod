package io.github.fripe070.pirkko;

import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum PirkkoKind implements StringIdentifiable {
    BLANK(0, "blank"),
    COLOR_WHITE(1, "color/white"),
    COLOR_ORANGE(2, "color/orange"),
    COLOR_MAGENTA(3, "color/magenta"),
    COLOR_LIGHT_BLUE(4, "color/light_blue"),
    COLOR_YELLOW(5, "color/yellow"),
    COLOR_LIME(6, "color/lime"),
    COLOR_PINK(7, "color/pink"),
    COLOR_GRAY(8, "color/gray"),
    COLOR_LIGHT_GRAY(9, "color/light_gray"),
    COLOR_CYAN(10, "color/cyan"),
    COLOR_PURPLE(11, "color/purple"),
    COLOR_BLUE(12, "color/blue"),
    COLOR_BROWN(13, "color/brown"),
    COLOR_GREEN(14, "color/green"),
    COLOR_RED(15, "color/red"),
    COLOR_BLACK(16, "color/black"),
    PHOZ(17, "phoz"),
    KONGLIG(18, "konglig"),
    GHOST(19, "ghost"),
    LASERVIOLETT(20, "laserviolett"),
    CERISE(21, "cerise");


    private final int index;
    private final String id;
    private final String friendlyId;

    PirkkoKind(int index, String id) {
        this.index = index;
        this.id = id;
        this.friendlyId = id.replace("/", "_");
    }

    @Override
    public String asString() {
        return friendlyId;
    }
    public String realId() {
        return id;
    }

    public int getIndex() {
        return index;
    }
    @Nullable
    public static PirkkoKind fromName(String name) {
        for (PirkkoKind kind : PirkkoKind.values()) {
            if (Objects.equals(kind.asString(), name)) {
                return kind;
            }
        }
        return null;
    }
}
