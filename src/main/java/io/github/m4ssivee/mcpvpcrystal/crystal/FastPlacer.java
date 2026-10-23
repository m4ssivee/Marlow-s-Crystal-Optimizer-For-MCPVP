package io.github.m4ssivee.mcpvpcrystal.crystal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

// Optional Walksy-style placement (WalksyCrystalOptimizer, MIT): while right click is held with an end crystal, place
// one every client tick instead of waiting for the vanilla right click delay. Unlike the original it only sends a packet
// when the placement is valid (obsidian or bedrock, free space above), and at most once per tick.
public final class FastPlacer {

    private final KeptCrystals keptCrystals;

    private boolean vanillaUsedThisTick;

    public FastPlacer(KeptCrystals keptCrystals) {
        this.keptCrystals = keptCrystals;
    }

    public void tickStarted() {
        vanillaUsedThisTick = false;
    }

    public void vanillaUse() {
        vanillaUsedThisTick = true;
    }

    public void tickEnded(Minecraft client) {
        if (vanillaUsedThisTick) {
            return;
        }

        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null || client.gameMode == null || client.screen != null
                || client.isPaused() || player.isSpectator() || player.isUsingItem()) {
            return;
        }

        if (!client.options.keyUse.isDown()) {
            return;
        }

        ItemStack held = player.getMainHandItem();
        if (!held.is(Items.END_CRYSTAL)) {
            return;
        }

        BlockHitResult hit = clip(level, player);
        if (hit.getType() != HitResult.Type.BLOCK || !canPlaceCrystal(level, hit.getBlockPos())) {
            return;
        }

        client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        player.swing(InteractionHand.MAIN_HAND);
    }

    private static BlockHitResult clip(ClientLevel level, LocalPlayer player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        return level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    private boolean canPlaceCrystal(ClientLevel level, BlockPos base) {
        BlockState state = level.getBlockState(base);
        if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) {
            return false;
        }

        BlockPos above = base.above();
        if (!level.isEmptyBlock(above)) {
            return false;
        }

        List<Entity> blocking = level.getEntities((Entity) null, new AABB(above.getX(), above.getY(), above.getZ(),
                above.getX() + 1.0D, above.getY() + 2.0D, above.getZ() + 1.0D));
        // A crystal this mod already hid is as good as gone, the server will have removed it by the time we arrive.
        blocking.removeIf(keptCrystals::isHidden);
        return blocking.isEmpty();
    }
}
