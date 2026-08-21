---
navigation:
  parent: introduction/index.md
  title: Flux Crafter Assembly Station
  position: 4
  icon: applied_extended_crafting:flux_crafter_pattern_provider
categories:
  - applied extended crafting devices
item_ids:
  - applied_extended_crafting:flux_crafter_pattern_provider
---

# Flux Crafter Assembly Station

<BlockImage id="applied_extended_crafting:flux_crafter_pattern_provider" scale="5" />

The **Flux Crafter Assembly Station** automates Extended Crafting Flux Crafter recipes while keeping their FE power requirement. It stores 9 encoded processing patterns and draws energy from nearby Flux Alternators.

## Setup and Energy

1. Connect the station to a powered ME network.
2. Place one or more <ItemLink id="extendedcrafting:flux_alternator" /> blocks within 3 blocks of the station on each axis. The complete scan area is a 7x7x7 cube centered on the station.
3. Supply the Flux Alternators with FE, then insert a matching Flux Crafter processing pattern.

For an active recipe, each participating Flux Alternator must contain at least the recipe's per-tick power rate. Every participating alternator supplies that amount each tick, so additional charged alternators process the same total FE requirement faster.

If no eligible alternator is available, processing pauses. The interface displays the active recipe and accumulated FE progress; the completed result is returned directly to ME storage. Recipes with no positive power requirement or power rate complete immediately.

## Crafting Recipe

<Recipe id="applied_extended_crafting:flux_crafter_pattern_provider" />


