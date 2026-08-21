---
navigation:
  parent: introduction/index.md
  title: 末影合成器装配站
  position: 2
  icon: applied_extended_crafting:ender_crafter_pattern_provider
categories:
  - applied extended crafting devices
item_ids:
  - applied_extended_crafting:ender_crafter_pattern_provider
---

# 末影合成器装配站

<BlockImage id="applied_extended_crafting:ender_crafter_pattern_provider" scale="5" />

**末影合成器装配站**通过 AE2 自动执行 Extended Crafting 的末影合成器配方。它可保存 9 个编码处理样板，并根据附近的末影交流器数量决定处理速度。

## 搭建方法

1. 将装配站接入有电的 ME 网络。
2. 在装配站每个坐标轴 3 格范围内放置至少一个 <ItemLink id="extendedcrafting:ender_alternator" />。完整扫描区域是以装配站为中心的 7x7x7 立方体。
3. 编码并放入末影合成器处理样板。

## 处理速度

- 范围内没有末影交流器时，装配站无法推进配方。
- 每个附近的末影交流器都会根据 Extended Crafting 的末影合成器配置缩短处理时间。
- 常规加速下的处理时间不会低于 20 tick（1 秒）。
- 范围内达到 147 个末影交流器后，配方会立即完成。

界面会显示当前配方及处理进度。处理结束后，产物会直接返回 ME 存储。

## 合成配方

<Recipe id="applied_extended_crafting:ender_crafter_pattern_provider" />
