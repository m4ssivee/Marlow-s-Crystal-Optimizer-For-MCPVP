package io.github.m4ssivee.mcpvpcrystal.mixin;

import io.github.m4ssivee.mcpvpcrystal.McpvpCrystal;
import io.github.m4ssivee.mcpvpcrystal.crystal.SequencedLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements SequencedLevel {

    @Shadow
    @Final
    private BlockStatePredictionHandler blockStatePredictionHandler;

    @Override
    public int mcpvpcrystal$blockSequence() {
        return blockStatePredictionHandler.currentSequence();
    }

    // Hidden instead of removed, so a crystal the server did not break comes back once it is released.
    @Inject(method = "entitiesForRendering", at = @At("RETURN"), cancellable = true)
    private void mcpvpcrystal$hideKeptCrystals(CallbackInfoReturnable<Iterable<Entity>> cir) {
        McpvpCrystal mod = McpvpCrystal.get();
        if (!mod.isDisabled()) {
            cir.setReturnValue(mod.keptCrystals().hide(cir.getReturnValue()));
        }
    }

    @Inject(method = "handleBlockChangedAck", at = @At("HEAD"))
    private void mcpvpcrystal$releaseKeptCrystals(int sequence, CallbackInfo ci) {
        McpvpCrystal.get().keptCrystals().acknowledged(sequence);
    }
}
