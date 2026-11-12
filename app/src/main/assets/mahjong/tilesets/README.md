# Mahjong tile sets

Each folder here is a picture tile set the player can pick in **Settings → Mahjong Solitaire → Tile style**.
"Simple shapes" is built in and always available.

```
tilesets/
└── classic/              ← folder name = the set's id (lowercase, no spaces)
    ├── name.txt          ← optional display name, e.g. "Classic ivory"
    ├── dots_1.png … dots_9.png
    ├── bamboo_1.png … bamboo_9.png
    ├── characters_1.png … characters_9.png
    ├── wind_east.png  wind_south.png  wind_west.png  wind_north.png
    ├── dragon_red.png  dragon_green.png  dragon_white.png
    ├── flower_1.png … flower_4.png
    └── season_1.png … season_4.png
```

- 42 images; `.png`, `.webp` or `.jpg`.
- Draw only the **face** (the symbol): the app draws the tile body, edge, shadow and the
  selected/hint/stuck highlighting itself. A transparent background works best.
- Portrait, roughly 3:4 (e.g. 300×400 px). Images are scaled to fit.
- Missing images fall back to the simple design for that face.
- Only use artwork you made or have the rights to ship.
