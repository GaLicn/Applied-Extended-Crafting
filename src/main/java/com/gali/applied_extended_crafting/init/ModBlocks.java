package com.gali.applied_extended_crafting.init;

import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.block.TableAdvancedPatternProviderBlock;
import com.gali.applied_extended_crafting.block.TableBasicPatternProviderBlock;
import com.gali.applied_extended_crafting.block.TableElitePatternProviderBlock;
import com.gali.applied_extended_crafting.block.TableUltimatePatternProviderBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            ForgeRegistries.BLOCKS,
            Applied_extended_crafting.MODID
    );

    public static final RegistryObject<TableBasicPatternProviderBlock> TABLE_BASIC_PATTERN_PROVIDER = BLOCKS.register(
            "table_basic_pattern_provider",
            TableBasicPatternProviderBlock::new
    );

    public static final RegistryObject<TableAdvancedPatternProviderBlock> TABLE_ADVANCED_PATTERN_PROVIDER = BLOCKS.register(
            "table_advanced_pattern_provider",
            TableAdvancedPatternProviderBlock::new
    );

    public static final RegistryObject<TableElitePatternProviderBlock> TABLE_ELITE_PATTERN_PROVIDER = BLOCKS.register(
            "table_elite_pattern_provider",
            TableElitePatternProviderBlock::new
    );

    public static final RegistryObject<TableUltimatePatternProviderBlock> TABLE_ULTIMATE_PATTERN_PROVIDER = BLOCKS.register(
            "table_ultimate_pattern_provider",
            TableUltimatePatternProviderBlock::new
    );

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
