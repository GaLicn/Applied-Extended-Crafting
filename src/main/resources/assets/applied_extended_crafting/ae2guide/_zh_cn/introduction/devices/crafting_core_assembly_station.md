---
navigation:
  parent: introduction/index.md
  title: 合成核心装配站
  position: 3
  icon: applied_extended_crafting:crafter_core_pattern_provider
categories:
  - applied extended crafting devices
item_ids:
  - applied_extended_crafting:crafter_core_pattern_provider
---

# 合成核心装配站

<BlockImage id="applied_extended_crafting:crafter_core_pattern_provider" scale="5" />

**合成核心装配站**是一台可独立执行 Extended Crafting 组合配方的 AE2 设备。它可保存 9 个编码处理样板，从 ME 网络抽取配方所需能量，并以内置基座储备代替实体基座结构。

## 基座储备

在专用基座槽中放入 <ItemLink id="extendedcrafting:pedestal" />。组合配方的每种外围原料需要一个基座，中心原料不计入需求。基座属于设备基础设施，不会在合成时消耗。

如果基座数量低于当前配方需求，处理会暂停；补足基座后即可继续。

## 能量与加速卡

装配站有 6 个升级槽，可安装 AE2 加速卡。加速卡会提高每 tick 抽取的 AE 能量，从而缩短目标处理时间，但不会改变配方消耗的 AE 总量。

| 加速卡数量 | 目标耗时 |
|---:|---:|
| 0 | 200 tick（10 秒） |
| 1 | 120 tick（6 秒） |
| 2 | 72 tick（3.6 秒） |
| 3 | 40 tick（2 秒） |
| 4 | 20 tick（1 秒） |
| 5 | 5 tick（0.25 秒） |
| 6 | 1 tick（0.05 秒） |

ME 网络供能不足时，进度只会暂停，不会丢失配方。无需正数能量消耗的配方会立即完成。

## 合成配方

<Recipe id="applied_extended_crafting:crafter_core_pattern_provider" />
