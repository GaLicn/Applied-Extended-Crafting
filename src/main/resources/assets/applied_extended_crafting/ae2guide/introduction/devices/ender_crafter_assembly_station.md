---
navigation:
  parent: introduction/index.md
  title: Ender Crafter Assembly Station
  position: 2
  icon: applied_extended_crafting:ender_crafter_pattern_provider
categories:
  - applied extended crafting devices
item_ids:
  - applied_extended_crafting:ender_crafter_pattern_provider
---

# Ender Crafter Assembly Station

<BlockImage id="applied_extended_crafting:ender_crafter_pattern_provider" scale="5" />

The **Ender Crafter Assembly Station** automates Extended Crafting Ender Crafter recipes through AE2. It stores 9 encoded processing patterns and uses nearby Ender Alternators to determine processing speed.

## Setup

1. Connect the station to a powered ME network.
2. Place at least one <ItemLink id="extendedcrafting:ender_alternator" /> within 3 blocks of the station on each axis. The complete scan area is a 7x7x7 cube centered on the station.
3. Encode and insert an Ender Crafter processing pattern.

## Processing Speed

- With no Ender Alternator in range, the station cannot advance a recipe.
- Every nearby Ender Alternator reduces the processing time according to Extended Crafting's Ender Crafter configuration.
- Processing time never falls below 20 ticks (1 second) through normal scaling.
- With 147 or more Ender Alternators in range, recipes complete immediately.

The interface shows the active recipe and its progress. Once complete, the result is returned directly to ME storage.

## Crafting Recipe

<Recipe id="applied_extended_crafting:ender_crafter_pattern_provider" />
