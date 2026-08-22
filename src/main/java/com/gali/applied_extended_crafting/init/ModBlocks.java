package com.gali.applied_extended_crafting.init;

import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.block.CrafterCorePatternProviderBlock;
import com.gali.applied_extended_crafting.block.EnderCrafterPatternProviderBlock;
import com.gali.applied_extended_crafting.block.FluxCrafterPatternProviderBlock;
import com.gali.applied_extended_crafting.block.TableAdvancedPatternProviderBlock;
import com.gali.applied_extended_crafting.block.TableBasicPatternProviderBlock;
import com.gali.applied_extended_crafting.block.TableElitePatternProviderBlock;
import com.gali.applied_extended_crafting.block.TableUltimatePatternProviderBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Applied_extended_crafting.MODID);

    public static final DeferredBlock<TableBasicPatternProviderBlock> TABLE_BASIC_PATTERN_PROVIDER = BLOCKS.registerBlock(
            "table_basic_pattern_provider",
            properties -> new TableBasicPatternProviderBlock(properties.noOcclusion())
    );

    public static final DeferredBlock<TableAdvancedPatternProviderBlock> TABLE_ADVANCED_PATTERN_PROVIDER = BLOCKS.registerBlock(
            "table_advanced_pattern_provider",
            properties -> new TableAdvancedPatternProviderBlock(properties.noOcclusion())
    );

    public static final DeferredBlock<TableElitePatternProviderBlock> TABLE_ELITE_PATTERN_PROVIDER = BLOCKS.registerBlock(
            "table_elite_pattern_provider",
            properties -> new TableElitePatternProviderBlock(properties.noOcclusion())
    );

    public static final DeferredBlock<TableUltimatePatternProviderBlock> TABLE_ULTIMATE_PATTERN_PROVIDER = BLOCKS.registerBlock(
            "table_ultimate_pattern_provider",
            properties -> new TableUltimatePatternProviderBlock(properties.noOcclusion())
    );

    public static final DeferredBlock<EnderCrafterPatternProviderBlock> ENDER_CRAFTER_PATTERN_PROVIDER = BLOCKS.registerBlock(
            "ender_crafter_pattern_provider",
            properties -> new EnderCrafterPatternProviderBlock(properties.noOcclusion())
    );

    public static final DeferredBlock<CrafterCorePatternProviderBlock> CRAFTER_CORE_PATTERN_PROVIDER = BLOCKS.registerBlock(
            "crafter_core_pattern_provider",
            properties -> new CrafterCorePatternProviderBlock(properties.noOcclusion())
    );

    public static final DeferredBlock<FluxCrafterPatternProviderBlock> FLUX_CRAFTER_PATTERN_PROVIDER = BLOCKS.registerBlock(
            "flux_crafter_pattern_provider",
            properties -> new FluxCrafterPatternProviderBlock(properties.noOcclusion())
    );

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
