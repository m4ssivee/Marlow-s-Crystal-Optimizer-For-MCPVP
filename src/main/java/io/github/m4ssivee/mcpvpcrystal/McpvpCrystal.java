package io.github.m4ssivee.mcpvpcrystal;

import io.github.m4ssivee.mcpvpcrystal.config.ModConfig;
import io.github.m4ssivee.mcpvpcrystal.crystal.CrystalBreaker;
import io.github.m4ssivee.mcpvpcrystal.crystal.FastPlacer;
import io.github.m4ssivee.mcpvpcrystal.crystal.KeptCrystals;
import io.github.m4ssivee.mcpvpcrystal.crystal.PastKeptCrystal;
import net.fabricmc.loader.api.FabricLoader;

public final class McpvpCrystal {

    public static final String MOD_ID = "mcpvp_crystal_optimizer";

    private static final McpvpCrystal INSTANCE = new McpvpCrystal();

    private final ModConfig config;

    private final KeptCrystals keptCrystals = new KeptCrystals();

    private final CrystalBreaker crystalBreaker = new CrystalBreaker(keptCrystals);

    private final PastKeptCrystal pastKeptCrystal = new PastKeptCrystal(keptCrystals);

    private final FastPlacer fastPlacer = new FastPlacer(keptCrystals);

    private McpvpCrystal() {
        config = ModConfig.load(FabricLoader.getInstance().getConfigDir()
                .resolve("marlows-crystal-optimizer-for-mcpvp.json"));
    }

    public static McpvpCrystal get() {
        return INSTANCE;
    }

    public static void initialize() {
        Log.info("Marlow's Crystal Optimizer for MCPVP loaded (enabled=" + INSTANCE.config.enabled()
                + ", fastPlace=" + INSTANCE.config.fastPlace() + ")");
    }

    public boolean isDisabled() {
        return !config.enabled();
    }

    public boolean fastPlaceEnabled() {
        return config.enabled() && config.fastPlace();
    }

    public KeptCrystals keptCrystals() {
        return keptCrystals;
    }

    public CrystalBreaker crystalBreaker() {
        return crystalBreaker;
    }

    public PastKeptCrystal pastKeptCrystal() {
        return pastKeptCrystal;
    }

    public FastPlacer fastPlacer() {
        return fastPlacer;
    }
}
