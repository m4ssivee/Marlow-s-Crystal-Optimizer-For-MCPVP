package io.github.m4ssivee.mcpvpcrystal.mixin;

import io.github.m4ssivee.mcpvpcrystal.McpvpCrystal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.EndCrystalItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(EndCrystalItem.class)
public class EndCrystalItemMixin {

    // The entities above the clicked block, the one List local of useOn on every version.
    @ModifyVariable(method = "useOn", at = @At("STORE"), ordinal = 0)
    private List<Entity> mcpvpcrystal$skipHiddenCrystals(List<Entity> obstructing) {
        McpvpCrystal mod = McpvpCrystal.get();
        return mod.isDisabled() ? obstructing : mod.keptCrystals().withoutHidden(obstructing);
    }
}
