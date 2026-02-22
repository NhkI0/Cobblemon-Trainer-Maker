# Cobblemon Trainer Maker

A JavaFX desktop application that converts [Pokémon Showdown](https://pokemonshowdown.com/) team exports into [Cobblemon RCT](https://gitlab.com/cable-mc/cobblemon) trainer JSON files for use on Minecraft servers.

---

## Features

- **Showdown Parser** — Paste any Showdown team export and have it converted instantly. Species, held items, abilities, natures, EVs, IVs, moves, levels, shiny status, and Tera Types are all extracted automatically.
- **Trainer Config** — Set the trainer's display name, internal identity, battle format, AI select margin, and per-battle item limit.
- **Bag Items** — Build the trainer's battle bag with any item ID and quantity.
- **Loot Table Editor** — Configure what the trainer drops on defeat: guaranteed items tied to a defeat-count condition, and/or a weighted random pool with optional player-level conditions.
- **One-click generation** — Outputs `trainers/{identity}.json` and, if configured, `trainers/loot_tables/{identity}.json` directly from the app.

---

## Requirements

| Requirement | Version |
|---|---|
| Java (JDK) | 23+ |
| JavaFX | 21.0.10 (bundled via Gradle) |
| OS | Windows (packaged app), any OS (run from source) |

---

## Building & Running

### Run from source

```bash
./gradlew run
```

### Build a native Windows app

```bash
./gradlew packageApp
```

The packaged application will be created at `build/dist/CobblemonTrainerMaker/`.

---

## Usage

### 1 — Trainer Info

| Field | Description |
|---|---|
| **Name** | Display name shown in-game (e.g. `Champion Cynthia`) |
| **Identity** | Internal ID used as the filename (e.g. `cynthia_boss`) |
| **Battle Format** | `GEN_9_SINGLES`, `GEN_9_DOUBLES`, `GEN_8_*`, `GEN_7_*` |
| **Max Item Uses** | How many times the trainer can use bag items during a battle |
| **Max Select Margin** | AI difficulty tweak — how much weaker a move can be before the AI stops considering it (default `0.05`) |

### 2 — Bag Items

Enter any valid item ID (e.g. `cobblemon:full_restore`) and a quantity, then click **Add**. Select an entry and click **Remove Selected** to delete it.

### 3 — Loot Table

#### Guaranteed Drops

Each entry becomes one item in a `rolls: 1` pool. The item only drops when the `rctmod:defeat_count` condition is met.

| Field | Description |
|---|---|
| Item ID | Any valid item ID (e.g. `minecraft:diamond`) |
| Comparator | `==` exactly N defeats, `<=` / `>=` / `<` / `>`, `%` every N defeats |
| Count | The number `N` used in the condition |

#### Weighted Drops

Each entry competes by weight in a single pool drawn multiple times per win.

| Field | Description |
|---|---|
| Rolls — Fixed | Draw exactly N times from the pool |
| Rolls — Binomial | Draw a random number of times: `n` trials, `p` chance per trial (avg = n×p) |
| Entry Type | `minecraft:item` for a specific item, `minecraft:loot_table` to delegate to another table |
| Name | Item ID or loot table path (e.g. `rctmod:generic/common/pokeballs`) |
| Weight | Relative probability — 1000 is 10× more likely than 100 |
| Lvl min / max | Optional: only apply this entry when the player's level is within this range |

### 4 — Team (Showdown format)

Paste a full Showdown team export into the text area.

**Example input:**
```
Kingdra @ Choice Specs
Ability: Swift Swim
Shiny: Yes
Tera Type: Poison
EVs: 252 SpA / 4 SpD / 252 Spe
Timid Nature
IVs: 0 Atk
- Draco Meteor
- Dragon Pulse
- Surf
- Hurricane

Swampert (M) @ Leftovers
Ability: Torrent
Level: 55
EVs: 248 HP / 248 Atk / 8 SpD
Adamant Nature
- Earthquake
- Flip Turn
- Stealth Rock
- Knock Off
```

**Parsing defaults** (applied when a field is absent from the export):

| Field | Default |
|---|---|
| Level | 100 |
| Nature | hardy |
| IVs | 31 (all stats) |
| EVs | 0 (all stats) |
| Shiny | false |
| Tera Type | normal |

### 5 — Generate JSON

Click **Generate JSON**. The app writes:

- `trainers/{identity}.json` — always
- `trainers/loot_tables/{identity}.json` — only if at least one loot entry was added

---

## Output Format

### Trainer JSON (`trainers/{identity}.json`)

```json
{
  "name": "Champion Cynthia",
  "identity": "cynthia_boss",
  "ai": {
    "type": "rct",
    "data": { "maxSelectMargin": 0.05 }
  },
  "battleFormat": "GEN_9_SINGLES",
  "battleRules": { "maxItemUses": 3 },
  "bag": [
    { "item": "cobblemon:full_restore", "quantity": 3 }
  ],
  "team": [
    {
      "species": "cobblemon:spiritomb",
      "level": 58,
      "nature": "cobblemon:bold",
      "ability": "pressure",
      "shiny": false,
      "heldItem": "cobblemon:leftovers",
      "moveset": ["shadowball", "darkpulse", "willowisp", "painsplit"],
      "ivs": { "hp": 31, "atk": 31, "def": 31, "spa": 31, "spd": 31, "spe": 31 },
      "evs": { "hp": 252, "atk": 0, "def": 252, "spa": 0, "spd": 4, "spe": 0 }
    }
  ]
}
```

### Loot Table JSON (`trainers/loot_tables/{identity}.json`)

```json
{
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:diamond",
          "conditions": [{ "condition": "rctmod:defeat_count", "count": 1 }]
        }
      ]
    },
    {
      "rolls": { "type": "minecraft:binomial", "n": 3, "p": 0.35 },
      "entries": [
        {
          "type": "minecraft:loot_table",
          "name": "rctmod:generic/common/pokeballs",
          "weight": 1000
        }
      ]
    }
  ]
}
```

---

## Deploying to Your Server

Place the generated files in your datapack under:

```
data/
└── rctmod/
    ├── trainers/
    │   └── single/
    │       └── {identity}.json          ← trainer config
    └── loot_table/
        └── trainers/
            └── single/
                └── {identity}.json      ← loot table (optional)
```

---

## Name & Item Mapping

The parser automatically maps Showdown names to Cobblemon IDs using three bundled lookup tables:

| File | Purpose |
|---|---|
| `pokemons.json` | Showdown species name → Cobblemon species ID |
| `items.json` | Showdown item name → Cobblemon / Minecraft item ID |
| `aspects.json` | Showdown form suffix → Cobblemon aspect string |

**Handled cases:**
- Special characters: `Farfetch'd` → `cobblemon:farfetchd`, `Mr. Mime` → `cobblemon:mr_mime`
- Spaces to underscores: `Tapu Koko` → `cobblemon:tapu_koko`
- Regional forms: `Vulpix-Alola` → `cobblemon:vulpix` + aspect `alolan`
- Battle forms: `Rotom-Wash` → `cobblemon:rotom` + aspect `wash`
- Non-standard item IDs: `Charcoal` → `cobblemon:charcoal_stick`, `Leek` → `cobblemon:medicinal_leek`
- Minecraft-namespaced items: `Rare Bone` → `minecraft:bone`, `Snowball` → `minecraft:snowball`

If a species or item has no explicit entry in the lookup tables, the parser falls back to `cobblemon:{name_with_underscores}`.

---

## Project Structure

```
Cobblemon-Trainer-Maker/
├── src/main/
│   ├── java/com/dopamine/cobblemontrainermaker/
│   │   ├── Launcher.java               # Entry point
│   │   ├── TrainerApplication.java     # JavaFX Application class
│   │   ├── AppController.java          # UI controller
│   │   ├── ShowdownParser.java         # Showdown → Cobblemon conversion
│   │   ├── Pokemon.java                # Pokemon data model
│   │   └── module-info.java
│   └── resources/
│       ├── com/dopamine/cobblemontrainermaker/
│       │   └── app-view.fxml           # UI layout
│       └── datas/
│           ├── pokemons.json           # Species name mappings
│           ├── items.json              # Item name mappings
│           └── aspects.json           # Form/aspect mappings
├── build.gradle.kts
└── trainers/                          # Generated output (gitignored)
```

---

## Tech Stack

- **Java 23**
- **JavaFX 21.0.10** (controls + fxml)
- **Gradle** with Kotlin DSL
- **jlink + jpackage** for native packaging
