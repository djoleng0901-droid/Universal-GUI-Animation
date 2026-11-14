# Universal GUI Animation

A client-side Fabric mod for Minecraft Java Edition **26.2**, focused on subtle Vanilla+ GUI and HUD animations.

## Current configuration

- Minecraft: 26.2
- Java: 25
- Fabric Loader: 0.19.3
- Fabric API: 0.161.0+26.2
- Fabric Loom: 1.17.21
- Gradle: 9.5.1
- Mod ID: `universal_gui_animation`

The Minecraft 26.2 toolchain values follow the Fabric 26.2 development guidance. Loom 1.17.21 and Fabric API 0.161.0+26.2 are pinned to published artifacts. See the official [Fabric 26.2 announcement](https://fabricmc.net/2026/06/15/262.html) and [Loom documentation](https://docs.fabricmc.net/develop/loom/).

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
- [x] First GUI transition prototype using Fabric ScreenEvents
- [x] Monotonic time-based easing for the screen transition
- [ ] Verify the first Gradle build in GitHub Actions
- [ ] Add persistent animation settings
- [ ] Expand GUI animation coverage
- [ ] Add HUD animation integration
- [ ] Run in-game visual and compatibility tests

## Design goals

- Smooth, time-based animations independent of frame rate
- Vanilla-style visuals and interaction behavior
- Per-category configuration
- Narrow, compatibility-conscious rendering hooks

## Current implementation notes

The first transition prototype registers a per-screen `ScreenEvents.afterExtract` listener from `ScreenEvents.BEFORE_INIT`. It draws a short, eased black overlay when a screen is initialized. Screen instances are tracked with weak references, and each instance receives only one render listener even if it is reinitialized after a resize. This is an initial GUI transition, not yet the full GUI/HUD animation feature set.

The rendering approach uses Fabric's screen event API and Minecraft's GUI graphics extraction path rather than raw OpenGL calls. HUD animation work remains separate because Minecraft 26.2 exposes a dedicated `HudElementRegistry` API.

## Build

```bash
./gradlew build
```

The output JAR will be in `build/libs/`.
