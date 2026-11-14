# Universal GUI Animation

A client-side Fabric mod for Minecraft Java Edition **26.2**, focused on subtle Vanilla+ GUI and HUD animations.

## Current configuration

- Minecraft: 26.2
- Java: 25
- Fabric Loader: 0.19.3
- Fabric API: 0.161.0+26.2
- Fabric Loom: 1.17
- Gradle: 9.5.1
- Mod ID: `universal_gui_animation`

The Minecraft 26.2 toolchain values follow the Fabric 26.2 development guidance. See the official [Fabric 26.2 announcement](https://fabricmc.net/2026/06/15/262.html) and [Loom documentation](https://docs.fabricmc.net/develop/loom/).

## Open in GitHub Codespaces

1. Open this repository in GitHub.
2. Select **Code → Codespaces → Create codespace on main**.
3. Wait for the Java 25 and Gradle 9.5.1 development container to finish creating.
4. In the terminal, run:

   ```bash
   gradle wrapper --gradle-version 9.5.1
   ./gradlew build
   ```

The wrapper properties are already pinned. The first command generates the Gradle wrapper scripts and wrapper JAR inside the Codespace.

## Project status

- [x] Repository and project metadata
- [x] Java 25 Codespaces environment
- [x] Minecraft 26.2 / Fabric build configuration
- [x] Client entrypoint
- [ ] Generate Gradle wrapper in Codespaces
- [ ] Run and verify the first Gradle build
- [ ] Implement the time-based animation engine
- [ ] Add GUI and HUD integrations

## Design goals

- Smooth, time-based animations independent of frame rate
- Vanilla-style visuals and interaction behavior
- Per-category configuration
- Narrow, compatibility-conscious rendering hooks

## Build

```bash
./gradlew build
```

The output JAR will be in `build/libs/`.
