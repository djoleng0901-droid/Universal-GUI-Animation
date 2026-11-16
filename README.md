# Universal GUI Animation

A client-side Fabric mod for Minecraft Java Edition 26.2, focused on smooth, subtle Vanilla+ GUI entrance animations.

## Configuration

- Minecraft: 26.2
- Java: 25
- Fabric Loader: 0.19.3
- Fabric API: 0.161.0+26.2
- Mod ID: universal_gui_animation

## Automatic screen animations

The default Automatic style animates the complete screen render pass, including widgets and elements drawn by third-party screen subclasses:

- Main menu: smooth entrance from below, with a tiny overshoot before settling at the center.
- Inventory and every vanilla container (chests, crafting, furnace, etc.): subtle centered zoom with a very small bounce.
- Settings, pause menus, Mod Menu, and other GUI screens: the same subtle centered zoom.
- Mod-added screen elements, buttons, lists, icons, labels, slots, and deferred tooltips are included in the same screen transform.
- The world/panorama background and in-game HUD are not transformed.
- Terrain-loading screens and saving/progress indicators are excluded and remain visually unchanged.

The animation is applied around the non-overridable screen render wrapper, not only the base Screen.extractRenderState method. This is important for mod screens that draw additional content after calling their superclass.

## In-game settings

Press F8 to open the mod settings screen. The key can be rebound in Options > Controls > Key Binds.

Settings are saved to config/universal-gui-animation.json:

- Entrance style: Automatic, Subtle Zoom, Classic, Pop, Slide Left/Right, Drop, Soft Tilt, Flip In, Soft Swing, or Elastic Spring.
- Squeeze strength: 0.00–0.30
- Entrance duration: 250–1000 ms
- Bounce strength: 0.00–0.30

## Build

Run gradle build --no-daemon. The output JAR is in build/libs/.
