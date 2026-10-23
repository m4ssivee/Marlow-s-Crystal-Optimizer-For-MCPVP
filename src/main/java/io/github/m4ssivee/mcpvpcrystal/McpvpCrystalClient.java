package io.github.m4ssivee.mcpvpcrystal;

import net.fabricmc.api.ClientModInitializer;

public final class McpvpCrystalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        McpvpCrystal.initialize();
    }
}
