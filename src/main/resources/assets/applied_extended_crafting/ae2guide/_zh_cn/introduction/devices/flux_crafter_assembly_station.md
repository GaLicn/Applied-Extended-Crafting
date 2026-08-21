---
navigation:
  parent: introduction/index.md
  title: 通量合成器装配站
  position: 4
  icon: applied_extended_crafting:flux_crafter_pattern_provider
categories:
  - applied extended crafting devices
item_ids:
  - applied_extended_crafting:flux_crafter_pattern_provider
---

# 通量合成器装配站

<BlockImage id="applied_extended_crafting:flux_crafter_pattern_provider" scale="5" />

**通量合成器装配站**可以自动执行 Extended Crafting 的通量合成器配方，并保留配方原有的 FE 能量需求。它可保存 9 个编码处理样板，并从附近的通量交流器抽取能量。

## 搭建与供能

1. 将装配站接入有电的 ME 网络。
2. 在装配站每个坐标轴 3 格范围内放置一个或多个 <ItemLink id="extendedcrafting:flux_alternator" />。完整扫描区域是以装配站为中心的 7x7x7 立方体。
3. 为通量交流器供应 FE，然后放入匹配的通量合成器处理样板。

处理配方时，每个参与工作的通量交流器都必须至少存有该配方的单 tick 功率需求。每个符合条件的通量交流器每 tick 都会提供这部分能量，因此增加已充能的交流器可以更快满足相同的 FE 总需求。

没有可用交流器时，处理会暂停。界面会显示当前配方及已积累的 FE 进度；完成后的产物会直接返回 ME 存储。无需正数总能量或单 tick 功率的配方会立即完成。

## 合成配方

<Recipe id="applied_extended_crafting:flux_crafter_pattern_provider" />


