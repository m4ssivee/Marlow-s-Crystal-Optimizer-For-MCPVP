package io.github.m4ssivee.mcpvpcrystal.crystal;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

// Minecraft 1.21.5 - 1.21.11: the Strength effect is called STRENGTH.
final class Compat {

    private Compat() {
    }

    static Holder<MobEffect> strength() {
        return MobEffects.STRENGTH;
    }
}
