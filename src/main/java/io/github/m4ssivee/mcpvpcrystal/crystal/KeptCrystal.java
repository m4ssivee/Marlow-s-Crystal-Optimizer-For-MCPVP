package io.github.m4ssivee.mcpvpcrystal.crystal;

public interface KeptCrystal {

    void mcpvpcrystal$keep(long keptAt, int sequence);

    void mcpvpcrystal$release();

    boolean mcpvpcrystal$isKept();

    long mcpvpcrystal$keptAt();

    int mcpvpcrystal$sequence();
}
