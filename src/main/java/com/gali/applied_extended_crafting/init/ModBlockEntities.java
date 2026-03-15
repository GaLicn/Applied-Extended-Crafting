package com.gali.applied_extended_crafting.init;

import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.blockentity.CrafterCorePatternProviderBlockEntity;
import com.gali.applied_extended_crafting.blockentity.EnderCrafterPatternProviderBlockEntity;
import com.gali.applied_extended_crafting.blockentity.FluxCrafterPatternProviderBlockEntity;
import com.gali.applied_extended_crafting.blockentity.TableAdvancedPatternProviderBlockEntity;
import com.gali.applied_extended_crafting.blockentity.TableBasicPatternProviderBlockEntity;
import com.gali.applied_extended_crafting.blockentity.TableElitePatternProviderBlockEntity;
import com.gali.applied_extended_crafting.blockentity.TableUltimatePatternProviderBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Applied_extended_crafting.MODID);

    public static final RegistryObject<BlockEntityType<TableBasicPatternProviderBlockEntity>> TABLE_BASIC_PATTERN_PROVIDER =
            BLOCK_ENTITIES.register("table_basic_pattern_provider",
                    () -> BlockEntityType.Builder.of(
                            TableBasicPatternProviderBlockEntity::new,
                            ModBlocks.TABLE_BASIC_PATTERN_PROVIDER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<TableAdvancedPatternProviderBlockEntity>> TABLE_ADVANCED_PATTERN_PROVIDER =
            BLOCK_ENTITIES.register("table_advanced_pattern_provider",
                    () -> BlockEntityType.Builder.of(
                            TableAdvancedPatternProviderBlockEntity::new,
                            ModBlocks.TABLE_ADVANCED_PATTERN_PROVIDER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<TableElitePatternProviderBlockEntity>> TABLE_ELITE_PATTERN_PROVIDER =
            BLOCK_ENTITIES.register("table_elite_pattern_provider",
                    () -> BlockEntityType.Builder.of(
                            TableElitePatternProviderBlockEntity::new,
                            ModBlocks.TABLE_ELITE_PATTERN_PROVIDER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<TableUltimatePatternProviderBlockEntity>> TABLE_ULTIMATE_PATTERN_PROVIDER =
            BLOCK_ENTITIES.register("table_ultimate_pattern_provider",
                    () -> BlockEntityType.Builder.of(
                            TableUltimatePatternProviderBlockEntity::new,
                            ModBlocks.TABLE_ULTIMATE_PATTERN_PROVIDER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<EnderCrafterPatternProviderBlockEntity>> ENDER_CRAFTER_PATTERN_PROVIDER =
            BLOCK_ENTITIES.register("ender_crafter_pattern_provider",
                    () -> BlockEntityType.Builder.of(
                            EnderCrafterPatternProviderBlockEntity::new,
                            ModBlocks.ENDER_CRAFTER_PATTERN_PROVIDER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<CrafterCorePatternProviderBlockEntity>> CRAFTER_CORE_PATTERN_PROVIDER =
            BLOCK_ENTITIES.register("crafter_core_pattern_provider",
                    () -> BlockEntityType.Builder.of(
                            CrafterCorePatternProviderBlockEntity::new,
                            ModBlocks.CRAFTER_CORE_PATTERN_PROVIDER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<FluxCrafterPatternProviderBlockEntity>> FLUX_CRAFTER_PATTERN_PROVIDER =
            BLOCK_ENTITIES.register("flux_crafter_pattern_provider",
                    () -> BlockEntityType.Builder.of(
                            FluxCrafterPatternProviderBlockEntity::new,
                            ModBlocks.FLUX_CRAFTER_PATTERN_PROVIDER.get()
                    ).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
