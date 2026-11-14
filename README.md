# Universal GUI Animation

A client-side Fabric mod for Minecraft Java Edition 26.2, focused on smooth, subtle Vanilla+ GUI entrance animations.

## Configuration

- Minecraft: 26.2
- Java: 25
- Fabric Loader: 0.19.3
- Fabric API: 0.161.0+26.2
- Mod ID: universal_gui_animation

## GUI entrance animation

Every newly opened GUI screen animates upward from below its resting position into the center. The transition uses cubic ease-out movement, a restrained damped bounce, and a subtle squash-and-stretch accent. It affects screens such as inventory, containers, crafting, pause, and title menus. It does not animate the in-game HUD.

## In-game settings

Press F8 to open the mod settings screen. The key can be rebound in Options > Controls > Key Binds.

Settings are saved to config/universal-gui-animation.json and can be changed in-game:

- Squeeze strength: 0.00–0.30
- Entrance duration: 250–1000 ms (lower is faster)
- Bounce strength: 0.00–0.30

## Build

Run gradle build --no-daemon. The output JAR is in build/libs/.
