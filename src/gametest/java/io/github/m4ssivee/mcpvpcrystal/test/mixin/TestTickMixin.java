package io.github.m4ssivee.mcpvpcrystal.test.mixin;

import io.github.m4ssivee.mcpvpcrystal.test.CrystalSelfTest;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class TestTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void crystaltest$tick(CallbackInfo ci) {
        CrystalSelfTest.get().tick((Minecraft) (Object) this);
    }
}
