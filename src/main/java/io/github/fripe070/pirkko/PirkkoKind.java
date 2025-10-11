package io.github.fripe070.pirkko;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public enum PirkkoKind implements StringIdentifiable {
    BLANK("blank", Rarity.UNCOMMON),

    COLOR_WHITE("color/white", Rarity.UNCOMMON),
    COLOR_ORANGE("color/orange", Rarity.UNCOMMON),
    COLOR_MAGENTA("color/magenta", Rarity.UNCOMMON),
    COLOR_LIGHT_BLUE("color/light_blue", Rarity.UNCOMMON),
    COLOR_YELLOW("color/yellow", Rarity.UNCOMMON),
    COLOR_LIME("color/lime", Rarity.UNCOMMON),
    COLOR_PINK("color/pink", Rarity.UNCOMMON),
    COLOR_GRAY("color/gray", Rarity.UNCOMMON),
    COLOR_LIGHT_GRAY("color/light_gray", Rarity.UNCOMMON),
    COLOR_CYAN("color/cyan", Rarity.UNCOMMON),
    COLOR_PURPLE("color/purple", Rarity.UNCOMMON),
    COLOR_BLUE("color/blue", Rarity.UNCOMMON),
    COLOR_BROWN("color/brown", Rarity.UNCOMMON),
    COLOR_GREEN("color/green", Rarity.UNCOMMON),
    COLOR_RED("color/red", Rarity.UNCOMMON),
    COLOR_BLACK("color/black", Rarity.UNCOMMON),

    PRIDE_AROMANTIC("pride/aromantic", Rarity.UNCOMMON),
    PRIDE_ASEXUAL("pride/asexual", Rarity.UNCOMMON),
    PRIDE_BISEXUAL("pride/bisexual", Rarity.UNCOMMON),
    PRIDE_GAY_M("pride/gay_m", Rarity.UNCOMMON),
    PRIDE_GENDER_FLUID("pride/genderfluid", Rarity.UNCOMMON),
    PRIDE_LESBIAN("pride/lesbian", Rarity.UNCOMMON),
    PRIDE_NONBINARY("pride/nonbinary", Rarity.UNCOMMON),
    PRIDE_RAINBOW("pride/rainbow", Rarity.UNCOMMON),
    PRIDE_TRANSGENDER("pride/transgender", Rarity.UNCOMMON),

    LASERVIOLETT("laserviolett", Rarity.RARE),
    CERISE("cerise", Rarity.RARE),
    PHOZ("phoz", Rarity.RARE, true),
    KONGLIG("konglig", Rarity.RARE, true),
    GHOST("ghost", Rarity.RARE, true);

    private final String assetPath;
    private final Rarity rarity;
    private final @Nullable SoundEvent soundEvent;

    PirkkoKind(String assetPath, Rarity rarity) {
        this(assetPath, rarity, false);
    }
    PirkkoKind(String assetPath, Rarity rarity, boolean useCustomSound) {
        this.assetPath = assetPath;
        this.rarity = rarity;
        var sound = SoundEvent.of(Identifier.of(Pirkko.MOD_ID, "pirkko/" + this.getPath()));
        this.soundEvent = useCustomSound ? sound : null;
    }

    public String getPath() {
        return assetPath;
    }
    public String getId() {
        return assetPath.replace("/", "_");
    }
    public String getTranslationKey() {
        return assetPath.replace("/", ".");
    }

    public Rarity getRarity() {
        return rarity;
    }
    public boolean usesCustomSound() {
        return soundEvent != null;
    }
    public SoundEvent getSound() {
        return Optional.ofNullable(soundEvent).orElse(Pirkko.DEFAULT_PIRKKO_SOUND);
    }

    @Override
    public String asString() {
        return this.getId();
    }

    @Nullable
    public static PirkkoKind fromPath(String path) {
        return Stream.of(values())
            .filter(kind -> kind.getPath().equals(path))
            .findFirst()
            .orElse(null);
    }

    @Nullable
    public static PirkkoKind fromId(String id) {
        return Stream.of(values())
            .filter(kind -> kind.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}