package com.gali.applied_extended_crafting.block;

import com.gali.applied_extended_crafting.blockentity.TableBasicPatternProviderBlockEntity;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TableBasicPatternProviderBlock extends appeng.block.crafting.PatternProviderBlock {
    private boolean initialized = false;

    public TableBasicPatternProviderBlock() {
        super();
    }

    public void initializeBlockEntity() {
        if (!initialized) {
            setBlockEntity(
                    (Class) TableBasicPatternProviderBlockEntity.class,
                    (BlockEntityType) ModBlockEntities.TABLE_BASIC_PATTERN_PROVIDER.get(),
                    null,
                    (level, pos, state, entity) -> {
                        if (entity instanceof appeng.blockentity.ServerTickingBlockEntity tickingEntity) {
                            tickingEntity.serverTick();
                        }
                    }
            );
            initialized = true;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        initializeBlockEntity();
        return new TableBasicPatternProviderBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        initializeBlockEntity();
        return super.getTicker(level, state, type);
    }
}
