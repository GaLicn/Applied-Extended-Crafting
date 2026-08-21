---
navigation:
  title: Applied Extended Crafting
  position: 70
---

# Applied Extended Crafting Guide

**Applied Extended Crafting** connects AE2 autocrafting with the machines and recipes from **Extended Crafting**. Its Assembly Stations act as specialized pattern providers: they store encoded processing patterns, perform the corresponding recipe internally, and return completed items directly to ME storage.

## Device Index

<CategoryIndex category="applied extended crafting devices"></CategoryIndex>

## Common Workflow

1. Connect an Assembly Station to a powered ME network.
2. Encode the matching Extended Crafting recipe as an AE2 processing pattern.
3. Insert the pattern into one of the station's 9 pattern slots.
4. Request the result from an ME terminal as usual.

Each station only accepts patterns for its supported recipe type. Pattern inputs and the single output must match the registered recipe and its quantities. Standard AE2 pattern-provider controls, such as priority, blocking mode, and redstone control, remain available.

> Completed outputs are inserted directly into ME storage. If storage cannot accept them immediately, the station retains them and retries later.
