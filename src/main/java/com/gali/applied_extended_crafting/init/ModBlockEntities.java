package com.gali.applied_extended_crafting.init;

import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.blockentity.TableAdvancedPatternProviderBlockEntity;
import com.gali.applied_extended_crafting.blockentity.TableBasicPatternProviderBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Applied_extended_crafting.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TableBasicPatternProviderBlockEntity>> TABLE_BASIC_PATTERN_PROVIDER =
            BLOCK_ENTITIES.register("table_basic_pattern_provider",
                    () -> BlockEntityType.Builder.of(
                            TableBasicPatternProviderBlockEntity::new,
                            ModBlocks.TABLE_BASIC_PATTERN_PROVIDER.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TableAdvancedPatternProviderBlockEntity>> TABLE_ADVANCED_PATTERN_PROVIDER =
            BLOCK_ENTITIES.register("table_advanced_pattern_provider",
                    () -> BlockEntityType.Builder.of(
                            TableAdvancedPatternProviderBlockEntity::new,
                            ModBlocks.TABLE_ADVANCED_PATTERN_PROVIDER.get()
                    ).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
