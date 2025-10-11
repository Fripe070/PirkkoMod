package io.github.fripe070.pirkko;

import net.minecraft.sound.SoundEvent;

public class PirkkoKindData {
    private SoundEvent sound;
    PirkkoKindData() {
        sound = null;
    }
    public SoundEvent GetSound() {
        return sound;
    }
    public PirkkoKindData setSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }
}
