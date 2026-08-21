# Changelog

## [1.2.0]

### Changed / 变更
- 自动化产出改为以注册配方的实际输出为准，而不是直接使用样板记录的输出
  - Automation output now uses the registered recipe's actual result instead of the output stored in the pattern.

### Fixed / 修复
- 修复配方校验时因输出物品 NBT 不匹配而无法识别配方的问题（[issue #1](https://github.com/GaLicn/Applied-Extended-Crafting/issues/1)）
  - Fixed recipe validation failing when the output item's NBT differs ([issue #1](https://github.com/GaLicn/Applied-Extended-Crafting/issues/1)).

### Docs / 文档
- 增加 GuideME 指南，介绍模组中的功能方块
  - Added a GuideME guide introducing the mod's functional blocks.
