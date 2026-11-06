# AZYRON CLIENT 1.0 — Minecraft Java 1.21.11 (Fabric)

A beautiful, lightweight, high-performance Minecraft Java client mod for **Minecraft 1.21.11 ONLY**.
Developer: **JIGNESH GAMING**.

## Download

Built automatically by CI on every push — see the release
[`azyron-client-1.0`](https://github.com/JIGNESH-HOSTING/Azyron-client-/releases/tag/azyron-client-1.0):

- **`Azyron-Client-1.0.jar`** — the client mod. Drop it into the `mods` folder of a
  Fabric 1.21.11 instance, launch, and play.
- **`Azyron-Client-Modpack-1.0.zip`** — installation-ready modpack: AZYRON CLIENT plus
  Sodium, Lithium, FerriteCore, ImmediatelyFast, Entity Culling, More Culling,
  ModernFix, Dynamic FPS, Fabric API and Mod Menu, each resolved from Modrinth at
  build time and verified to support 1.21.11 + Fabric.

## Features

- Custom AZYRON main menu (dark premium theme, purple / electric-blue accents) and
  branded loading screen
- FPS, CPS, ping, coordinates, keystrokes and armor HUD modules with drag-to-move
  positions (HUD Editor)
- FPS Boost settings page with one-click optimization
- Potato / Balanced / Smooth FPS profiles, applied to vanilla video settings
- Default low-graphics settings on first launch: Fast graphics, 2-chunk render and
  simulation distance, clouds off, minimal particles, no smooth lighting, 50% entity
  distance, 0 mipmaps, VSync off, 60 FPS cap, 0 biome blend, entity shadows off
- In-game menu on **Right Shift** (also an "AZYRON Menu" button on the title screen)

## Install — desktop (Fabric)

1. Install Fabric Loader for 1.21.11 (fabricmc.net) or use the modpack ZIP.
2. Put `Azyron-Client-1.0.jar` (and the performance mods) into `.minecraft/mods/`.
3. Launch the `fabric-loader-1.21.11` profile and play.

## Install — PojavLauncher (Android)

1. In PojavLauncher, install the **1.21.11** version, then use **Install Fabric**
   (or import a Fabric 1.21.11 profile) so the instance runs Fabric Loader.
2. Open the instance folder (Long-press the version → "Open instance folder" /
   use a file manager on `Android/data/net.kdt.pojavlaunch/files/.minecraft` or
   the reworked `.pojavlauncher` instance dir) and copy `Azyron-Client-1.0.jar`
   plus the modpack's `mods/*.jar` into its `mods` folder.
3. Launch the Fabric 1.21.11 profile. AZYRON applies its low-graphics profile on
   first start; open the menu with **Right Shift** (attach a keyboard, or map a
   key in the launcher's controls settings).

Note: PojavLauncher runs the full desktop JVM stack on Android, so Fabric mods
work, but heavy rendering mods (Sodium) depend on your GPU drivers — Dynamic FPS
and the Potato profile keep it smooth on weaker phones.

## Build from source

```bash
cd azyron-client
./gradlew build        # requires JDK 21
# JAR: build/libs/azyron-client-1.0.jar
```

Toolchain pinned for 1.21.11: Fabric Loom 1.14, Loader 0.18.4, Yarn
1.21.11+build.6, Fabric API 0.141.4+1.21.11, Java 21, Gradle 9.2.1.
