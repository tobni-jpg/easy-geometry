# Easy Geometry

Client-side Fabric mod for **Minecraft 26.2** that lets you visualize geometric shapes (ghost outlines in the world, like Litematica — *no blocks are placed*).

Pick a shape and size, see a translucent preview centered on your player, and use it as a building guide.

## Features

- **Shapes:** Sphere, Cylinder, Cone, Torus
- **Center always on the player** — the shape is placed once around your current position
- **Adjustable radius** (1–64) via a slider
- **Sodium-style dark settings UI**
- **Preview toggle** — show/hide the ghost overlay in the world
- **No block placement** — purely a visual overlay, safe to use
- **Persistent config** — your last shape and radius are saved

## Keybind

Press **`G`** to open the settings screen.

## Installation

1. Drop `easy-geometry-1.0.0.jar` into your Fabric client's `mods/` folder
2. Requires: Minecraft 26.2, Fabric Loader 0.19.3+, Fabric API
3. Launch, press `G`, pick a shape and radius, enable the preview

## Controls / UI

| Control | Action |
|---------|--------|
| `G` | Open the Easy Geometry settings screen |
| Form button | Cycle between Sphere / Cylinder / Cone / Torus |
| Radius slider | Adjust size (1–64) |
| Preview button | Toggle the ghost overlay |
| Show Preview | Enable overlay and close |
| Close | Close without enabling |

## Build

```bash
./gradlew build
# output: build/libs/easy-geometry-1.0.0.jar
```

## License

MIT
