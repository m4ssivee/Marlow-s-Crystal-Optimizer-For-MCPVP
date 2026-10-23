package io.github.m4ssivee.mcpvpcrystal.mixin;

import io.github.m4ssivee.mcpvpcrystal.crystal.KeptCrystal;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EndCrystal.class)
public class EndCrystalMixin implements KeptCrystal {

    @Unique
    private boolean mcpvpcrystal$kept;

    @Unique
    private long mcpvpcrystal$keptAt;

    @Unique
    private int mcpvpcrystal$sequence;

    @Override
    public void mcpvpcrystal$keep(long keptAt, int sequence) {
        mcpvpcrystal$kept = true;
        mcpvpcrystal$keptAt = keptAt;
        mcpvpcrystal$sequence = sequence;
    }

    @Override
    public void mcpvpcrystal$release() {
        mcpvpcrystal$kept = false;
    }

    @Override
    public boolean mcpvpcrystal$isKept() {
        return mcpvpcrystal$kept;
    }

    @Override
    public long mcpvpcrystal$keptAt() {
        return mcpvpcrystal$keptAt;
    }

    @Override
    public int mcpvpcrystal$sequence() {
        return mcpvpcrystal$sequence;
    }
}
