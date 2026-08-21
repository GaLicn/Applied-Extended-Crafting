---
navigation:
  parent: introduction/index.md
  title: Crafting Core Assembly Station
  position: 3
  icon: applied_extended_crafting:crafter_core_pattern_provider
categories:
  - applied extended crafting devices
item_ids:
  - applied_extended_crafting:crafter_core_pattern_provider
---

# Crafting Core Assembly Station

<BlockImage id="applied_extended_crafting:crafter_core_pattern_provider" scale="5" />

The **Crafting Core Assembly Station** performs Extended Crafting combination recipes as a self-contained AE2 device. It stores 9 encoded processing patterns, draws the recipe's power cost from the ME network, and uses an internal supply of pedestal blocks instead of a physical pedestal structure.

## Pedestal Supply

Place <ItemLink id="extendedcrafting:pedestal" /> blocks in the dedicated pedestal slot. The station needs one pedestal for every peripheral ingredient in the selected combination recipe; the center ingredient does not require one. Pedestals are infrastructure and are not consumed.

If the pedestal count drops below the active recipe's requirement, processing pauses until enough pedestals are supplied again.

## Power and Speed Cards

The station has 6 upgrade slots for AE2 Speed Cards. Speed Cards increase the amount of AE consumed per tick, shortening the target processing duration without changing the recipe's total AE cost.

| Speed Cards | Target Duration |
|---:|---:|
| 0 | 200 ticks (10 s) |
| 1 | 120 ticks (6 s) |
| 2 | 72 ticks (3.6 s) |
| 3 | 40 ticks (2 s) |
| 4 | 20 ticks (1 s) |
| 5 | 5 ticks (0.25 s) |
| 6 | 1 tick (0.05 s) |

If the ME network cannot provide enough power, progress pauses rather than losing the recipe. Recipes with no positive power requirement complete immediately.

## Crafting Recipe

<Recipe id="applied_extended_crafting:crafter_core_pattern_provider" />


