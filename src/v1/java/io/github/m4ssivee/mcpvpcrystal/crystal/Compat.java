package io.github.m4ssivee.mcpvpcrystal.crystal;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

// Minecraft 1.21 - 1.21.4: the Strength effect is still called DAMAGE_BOOST.
final class Compat {

    private Compat() {
    }

    static Holder<MobEffect> strength() {
        return MobEffects.DAMAGE_BOOST;
    }
}
