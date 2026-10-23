# Marlow's Crystal Optimizer for MCPVP

A client-side Fabric mod that makes end crystals feel instant on the **MCPVP** server.

When you hit an end crystal, vanilla Minecraft keeps showing it until the server tells your client it is gone, which
takes a full round trip of your ping. This mod hides the crystal on your screen the moment your hit would break it, so
you can place the next crystal right away, even in the same tick.

## Features

- **Instant crystal break**: a crystal you break disappears on your client immediately instead of after the round trip.
- **Place through the broken crystal**: right click with an end crystal on the obsidian or bedrock behind a crystal you
  just broke and the new crystal goes down in the same tick.
- **Hit the next crystal**: an attack aimed at a crystal behind the one you just broke reaches it.
- **No ghost crystals**: the damage check follows vanilla `Player.attack`, so a hit that cannot break the crystal
  (for example with Weakness and an empty hand) does not hide it. If the server ignores your hit, the crystal shows up
  again by itself.
- **Server stays in charge**: the client never deletes the crystal entity. It is only hidden until the server removes it.
- **Optional fast place** (off by default): places a crystal every tick while right click is held, Walksy style.

## Supported versions

| Jar | Minecraft |
|---|---|
| `MarlowsCrystalOptimizerForMCPVP-<version>+mc1.21-1.21.4.jar` | 1.21 - 1.21.4 |
| `MarlowsCrystalOptimizerForMCPVP-<version>+mc1.21.5-1.21.10.jar` | 1.21.5 - 1.21.10 |
| `MarlowsCrystalOptimizerForMCPVP-<version>+mc1.21.11.jar` | 1.21.11 |

Fabric Loader only. Fabric API is **not** required.

## Configuration

`config/marlows-crystal-optimizer-for-mcpvp.json` is created on first launch:

```json
{
  "enabled": true,
  "fastPlace": false
}
```

## Building

Requires JDK 21.

```
gradle -Pvariant=v1 build   # 1.21 - 1.21.4
gradle -Pvariant=v2 build   # 1.21.5 - 1.21.10
gradle -Pvariant=v3 build   # 1.21.11
```

`-Pmc=<version>` compiles against a specific Minecraft version of that range. `runCrystalTest` starts a dev client that
runs an automated in-game self test (dev only, not part of the release jar).

## Credits

This mod is based on two MIT licensed projects:

- [Marlow's Crystal Optimizer](https://github.com/Bram1903/MarlowsCrystalOptimizer) by Bram and Marlow: the client-side
  crystal hiding, the damage check and seeing past broken crystals.
- [WalksyCrystalOptimizer](https://github.com/Walksy/WalksyCrystalOptimizer) by Walksy: the fast placement idea.

## License

MIT, see [LICENSE](LICENSE).
