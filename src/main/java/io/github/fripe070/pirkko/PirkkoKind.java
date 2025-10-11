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
    CERISE(21, "cerise"),
    PRIDE_AROMANTIC(22, "pride/aromantic"),
    PRIDE_ASEXUAL(23, "pride/asexual"),
    PRIDE_BISEXUAL(24, "pride/bisexual"),
    PRIDE_GAY_M(25, "pride/gay_m"),
    PRIDE_GENDER_FLUID(26, "pride/genderfluid"),
    PRIDE_LESBIAN(27, "pride/lesbian"),
    PRIDE_NONBINARY(28, "pride/nonbinary"),
    PRIDE_RAINBOW(29, "pride/rainbow"),
    PRIDE_TRANSGENDER(30, "pride/transgender");


    private final int index;
    private final String id;
    private final String friendlyId;
    private final PirkkoKindData pirkkoKindData;

    PirkkoKind(int index, String id) {
        this.index = index;
        this.id = id;
        this.friendlyId = id.replace("/", "_");
        this.pirkkoKindData = new PirkkoKindData();
    }

    public static void InitializePirkkoKindData() {
        GHOST.pirkkoKindData()
                .setSound(Pirkko.PIRKKO_GHOST_SOUND);
        PHOZ.pirkkoKindData()
                .setSound(Pirkko.PIRKKO_PHOZ_SOUND);
        KONGLIG.pirkkoKindData()
                .setSound(Pirkko.PIRKKO_KONGLIG_SOUND);
    }

    @Override
    public String asString() {
        return friendlyId;
    }

    public String realId() {
        return id;
    }

    public PirkkoKindData pirkkoKindData() {
        return pirkkoKindData;
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
