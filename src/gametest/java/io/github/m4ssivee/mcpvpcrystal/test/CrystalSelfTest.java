package io.github.m4ssivee.mcpvpcrystal.test;

import io.github.m4ssivee.mcpvpcrystal.McpvpCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// Dev-only in-game test. Never shipped: it lives in the gametest source set, which the release jar does not include.
// Drives a real client through the real input code paths (Minecraft.startAttack / startUseItem) in a flat creative world.
public final class CrystalSelfTest {

    private enum Stage { TITLE, CREATING, LOADING, SETUP, RUN, DONE }

    private static final CrystalSelfTest INSTANCE = new CrystalSelfTest();

    private final List<String> lines = new ArrayList<>();

    private Stage stage = Stage.TITLE;

    private int wait;

    private int step;

    private BlockPos base;

    private int failures;

    private int oldCrystalId = -1;

    private int totalTicks;

    private Vec3 start;

    public static CrystalSelfTest get() {
        return INSTANCE;
    }

    public void tick(Minecraft client) {
        totalTicks++;
        if (totalTicks > 20 * 60 * 6 && stage != Stage.DONE) {
            fail("timeout in stage " + stage + " step " + step);
            finish(client);
            return;
        }

        try {
            switch (stage) {
                case TITLE -> title(client);
                case CREATING -> creating(client);
                case LOADING -> loading(client);
                case SETUP, RUN -> run(client);
                case DONE -> { }
            }
        } catch (Throwable t) {
            fail("exception: " + t);
            t.printStackTrace();
            finish(client);
        }
    }

    private void title(Minecraft client) throws Exception {
        if (!(client.screen instanceof TitleScreen)) {
            // First launch shows the accessibility onboarding (or another notice) before the title screen.
            if (client.screen != null && client.getOverlay() == null && totalTicks % 40 == 0) {
                log("skipping screen " + client.screen.getClass().getSimpleName());
                client.setScreen(new TitleScreen());
            }
            return;
        }

        client.options.pauseOnLostFocus = false;
        for (Method m : CreateWorldScreen.class.getMethods()) {
            if (m.getName().equals("openFresh") && m.getParameterCount() == 2) {
                Object second = m.getParameterTypes()[1] == Runnable.class ? (Runnable) () -> { } : null;
                m.invoke(null, client, second);
                stage = Stage.CREATING;
                log("opened create world screen");
                return;
            }
        }
        throw new IllegalStateException("CreateWorldScreen.openFresh not found");
    }

    private void creating(Minecraft client) throws Exception {
        if (!(client.screen instanceof CreateWorldScreen screen)) {
            return;
        }

        WorldCreationUiState ui = screen.getUiState();
        ui.setName("crystaltest");
        ui.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
        ui.setAllowCommands(true);
        for (WorldCreationUiState.WorldTypeEntry entry : ui.getNormalPresetList()) {
            if (entry.preset() != null && entry.preset().is(WorldPresets.FLAT)) {
                ui.setWorldType(entry);
            }
        }

        Method onCreate = CreateWorldScreen.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(screen);
        stage = Stage.LOADING;
        log("creating flat creative world");
    }

    private void loading(Minecraft client) {
        if (client.level == null || client.player == null || client.getSingleplayerServer() == null) {
            return;
        }

        if (client.screen != null) {
            client.setScreen(null);
        }

        if (++wait < 60) {
            return;
        }

        // Applies every mixin of every loaded config right now, so a target that does not exist on this version fails here.
        MixinEnvironment.getCurrentEnvironment().audit();
        log("PASS mixin audit (all mixin targets applied)");
        wait = 0;
        stage = Stage.SETUP;
    }

    private void run(Minecraft client) throws Exception {
        if (client.screen != null) {
            client.setScreen(null);
        }

        if (wait > 0) {
            wait--;
            return;
        }

        IntegratedServer server = client.getSingleplayerServer();
        LocalPlayer player = client.player;
        ServerLevel serverLevel = server.overworld();
        ServerPlayer serverPlayer = server.getPlayerList().getPlayers().get(0);

        switch (step) {
            // ---------- setup: obsidian base 3 blocks in front, player holding crystals ----------
            case 0 -> {
                BlockPos feet = player.blockPosition();
                base = feet.offset(0, -1, 3);
                start = player.position();
                BlockPos b = base;
                server.execute(() -> {
                    serverLevel.setBlockAndUpdate(b, Blocks.OBSIDIAN.defaultBlockState());
                    serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 60, 1));
                    spawnCrystal(serverLevel, b);
                });
                log("placed obsidian at " + base + ", spawned crystal, gave Weakness II with empty hand");
                next(20);
            }
            // ---------- test 1: weakness + empty hand must NOT hide the crystal (no ghost crystal) ----------
            case 1 -> {
                EndCrystal crystal = clientCrystal(client);
                if (crystal == null) { failStep("crystal not visible on client before weakness test"); return; }
                aim(player, base);
                if (!aimingAt(client, crystal)) { failStep("crosshair is not on the crystal: " + client.hitResult); return; }
                invoke(client, "startAttack");
                boolean hidden = McpvpCrystal.get().keptCrystals().isHidden(crystal);
                check(!hidden, "weakness hit does not hide crystal");
                next(20);
            }
            case 2 -> {
                EndCrystal crystal = clientCrystal(client);
                check(crystal != null && !crystal.isRemoved() && serverCrystals(serverLevel).size() == 1,
                        "crystal still exists after weakness hit (client and server agree)");
                server.execute(() -> {
                    serverPlayer.removeEffect(MobEffects.WEAKNESS);
                    serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.END_CRYSTAL, 64));
                });
                next(20);
            }
            // ---------- test 2: a real hit hides the crystal the same tick, before the server answers ----------
            case 3 -> {
                EndCrystal crystal = clientCrystal(client);
                if (crystal == null) { failStep("crystal not visible before break test"); return; }
                check(player.getMainHandItem().is(Items.END_CRYSTAL), "client holds end crystals");
                aim(player, base);
                if (!aimingAt(client, crystal)) { failStep("crosshair is not on the crystal: " + client.hitResult); return; }
                oldCrystalId = crystal.getId();
                invoke(client, "startAttack");
                check(McpvpCrystal.get().keptCrystals().isHidden(crystal), "crystal hidden on the client the same tick it was hit");
                check(!contains(client.level.entitiesForRendering(), crystal), "hidden crystal is not rendered");
                check(!crystal.isRemoved(), "client did not delete the entity itself (server stays authoritative)");

                // ---------- test 3: place a new crystal in the SAME tick, through the hidden one ----------
                aim(player, base);
                client.hitResult = new EntityHitResult(crystal);
                invoke(client, "startUseItem");
                log("same tick: attack + place packets sent");
                next(30);
            }
            case 4 -> {
                List<EndCrystal> onServer = serverCrystals(serverLevel);
                boolean oldGone = client.level.getEntity(oldCrystalId) == null;
                check(oldGone, "server removed the broken crystal");
                check(onServer.size() == 1 && onServer.get(0).getId() != oldCrystalId,
                        "new crystal placed in the same tick as the break (server has " + onServer.size() + ")");
                // The explosion knocked the player back; put them where they stood.
                Vec3 s0 = start;
                server.execute(() -> serverPlayer.teleportTo(s0.x, s0.y, s0.z));
                next(20);
            }
            // ---------- test 4: second cycle, to be sure nothing stays stuck hidden ----------
            case 5 -> {
                EndCrystal crystal = clientCrystal(client);
                if (crystal == null) { failStep("new crystal not visible on client"); return; }
                check(!McpvpCrystal.get().keptCrystals().isHidden(crystal), "new crystal is visible (not hidden)");
                aim(player, base);
                if (!aimingAt(client, crystal)) { failStep("crosshair is not on the new crystal: " + client.hitResult); return; }
                int id = crystal.getId();
                invoke(client, "startAttack");
                check(McpvpCrystal.get().keptCrystals().isHidden(crystal), "second crystal hidden on hit");
                oldCrystalId = id;
                next(30);
            }
            case 6 -> {
                check(client.level.getEntity(oldCrystalId) == null && serverCrystals(serverLevel).isEmpty(),
                        "second crystal removed by the server");
                finish(client);
            }
            default -> { }
        }
    }

    private static void spawnCrystal(ServerLevel level, BlockPos base) {
        EndCrystal crystal = new EndCrystal(level, base.getX() + 0.5D, base.getY() + 1.0D, base.getZ() + 0.5D);
        crystal.setShowBottom(false);
        level.addFreshEntity(crystal);
    }

    private List<EndCrystal> serverCrystals(ServerLevel level) {
        return level.getEntitiesOfClass(EndCrystal.class, new AABB(base.above()).inflate(1.0D));
    }

    private EndCrystal clientCrystal(Minecraft client) {
        List<EndCrystal> found = client.level.getEntitiesOfClass(EndCrystal.class, new AABB(base.above()).inflate(1.0D));
        return found.isEmpty() ? null : found.get(0);
    }

    // Look at the top centre of the obsidian. The line passes through the crystal standing on it.
    private static void aim(LocalPlayer player, BlockPos base) {
        Vec3 eye = player.getEyePosition();
        Vec3 target = new Vec3(base.getX() + 0.5D, base.getY() + 1.0D, base.getZ() + 0.5D);
        Vec3 d = target.subtract(eye);
        float yaw = (float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0D);
        float pitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
        Minecraft.getInstance().gameRenderer.pick(1.0F);
    }

    private static boolean aimingAt(Minecraft client, Entity entity) {
        return client.hitResult instanceof EntityHitResult hit && hit.getEntity() == entity;
    }

    private static boolean contains(Iterable<Entity> entities, Entity entity) {
        for (Entity e : entities) {
            if (e == entity) {
                return true;
            }
        }
        return false;
    }

    private static void invoke(Minecraft client, String name) throws Exception {
        Method m = Minecraft.class.getDeclaredMethod(name);
        m.setAccessible(true);
        m.invoke(client);
    }

    private void next(int ticks) {
        step++;
        wait = ticks;
    }

    private void check(boolean ok, String what) {
        if (ok) {
            log("PASS " + what);
        } else {
            fail(what);
        }
    }

    private void failStep(String what) {
        fail(what);
        step = 6;
    }

    private void fail(String what) {
        failures++;
        log("FAIL " + what);
    }

    private void log(String line) {
        lines.add(line);
        System.out.println("[CrystalSelfTest] " + line);
    }

    private void finish(Minecraft client) {
        if (stage == Stage.DONE) {
            return;
        }
        stage = Stage.DONE;
        log(failures == 0 ? "RESULT: ALL PASSED" : "RESULT: " + failures + " FAILED");
        try {
            Files.write(Path.of("crystaltest-result.txt"), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.stop();
    }

    @SuppressWarnings("unused")
    private static final AtomicInteger UNUSED = new AtomicInteger();
}
