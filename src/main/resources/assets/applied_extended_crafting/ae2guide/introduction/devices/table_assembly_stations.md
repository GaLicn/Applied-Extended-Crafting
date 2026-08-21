---
navigation:
  parent: introduction/index.md
  title: Table Assembly Stations
  position: 1
  icon: applied_extended_crafting:table_ultimate_pattern_provider
categories:
  - applied extended crafting devices
item_ids:
  - applied_extended_crafting:table_basic_pattern_provider
  - applied_extended_crafting:table_advanced_pattern_provider
  - applied_extended_crafting:table_elite_pattern_provider
  - applied_extended_crafting:table_ultimate_pattern_provider
---

# Table Assembly Stations

<Row>
  <BlockImage id="applied_extended_crafting:table_basic_pattern_provider" scale="4" />
  <BlockImage id="applied_extended_crafting:table_advanced_pattern_provider" scale="4" />
  <BlockImage id="applied_extended_crafting:table_elite_pattern_provider" scale="4" />
  <BlockImage id="applied_extended_crafting:table_ultimate_pattern_provider" scale="4" />
</Row>

The four **Table Assembly Stations** automate Extended Crafting table recipes without a physical crafting grid. Each station stores 9 encoded processing patterns and exposes valid recipes to the ME autocrafting system.

## Tiers

| Station | Supported Table Size | Recipe Tier |
|---|---:|---:|
| Basic | 3x3 | 1 |
| Advanced | 5x5 | 2 |
| Elite | 7x7 | 3 |
| Ultimate | 9x9 | 4 |

For a recipe with an explicitly required tier, use the station of that exact tier. A recipe without an exact-tier requirement can also run in a higher-tier station.

## Operation

- Encode the table recipe as a processing pattern with the exact inputs, output, and quantities shown by the recipe.
- Insert it into a station of the appropriate tier. Unsupported patterns cannot be inserted.
- When AE2 starts a crafting job, the station completes the table recipe internally and sends its result to ME storage.
- No Extended Crafting table or external item transport is required beside the station.

## Crafting Recipes

### Basic Table Assembly Station

<Recipe id="applied_extended_crafting:table_basic_pattern_provider" />

### Advanced Table Assembly Station

<Recipe id="applied_extended_crafting:table_advanced_pattern_provider" />

### Elite Table Assembly Station

<Recipe id="applied_extended_crafting:table_elite_pattern_provider" />

### Ultimate Table Assembly Station

<Recipe id="applied_extended_crafting:table_ultimate_pattern_provider" />
