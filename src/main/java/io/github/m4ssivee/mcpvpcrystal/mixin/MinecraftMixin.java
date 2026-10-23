package io.github.m4ssivee.mcpvpcrystal.mixin;

import io.github.m4ssivee.mcpvpcrystal.McpvpCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void mcpvpcrystal$tickStarted(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        McpvpCrystal mod = McpvpCrystal.get();
        mod.fastPlacer().tickStarted();
        if (client.level != null && !client.isPaused()) {
            mod.keptCrystals().ticked();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void mcpvpcrystal$tickEnded(CallbackInfo ci) {
        McpvpCrystal mod = McpvpCrystal.get();
        if (mod.fastPlaceEnabled()) {
            mod.fastPlacer().tickEnded((Minecraft) (Object) this);
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void mcpvpcrystal$aimPastKeptCrystal(CallbackInfoReturnable<Boolean> cir) {
        McpvpCrystal mod = McpvpCrystal.get();
        if (!mod.isDisabled()) {
            mod.pastKeptCrystal().attackStarted((Minecraft) (Object) this);
        }
    }

    @Inject(method = "startAttack", at = @At("RETURN"))
    private void mcpvpcrystal$restoreAttackCrosshair(CallbackInfoReturnable<Boolean> cir) {
        McpvpCrystal.get().pastKeptCrystal().attackEnded((Minecraft) (Object) this);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void mcpvpcrystal$placePastKeptCrystal(CallbackInfo ci) {
        McpvpCrystal mod = McpvpCrystal.get();
        mod.fastPlacer().vanillaUse();
        if (!mod.isDisabled()) {
            mod.pastKeptCrystal().useStarted((Minecraft) (Object) this);
        }
    }

    // The stack each hand tries is the one ItemStack local on every version, stored before the hit result is read.
    @ModifyVariable(method = "startUseItem", at = @At("STORE"), ordinal = 0)
    private ItemStack mcpvpcrystal$aimForHand(ItemStack stack) {
        McpvpCrystal.get().pastKeptCrystal().handTried((Minecraft) (Object) this, stack);
        return stack;
    }

    @Inject(method = "startUseItem", at = @At("RETURN"))
    private void mcpvpcrystal$restoreUseCrosshair(CallbackInfo ci) {
        McpvpCrystal.get().pastKeptCrystal().useEnded((Minecraft) (Object) this);
    }
}
