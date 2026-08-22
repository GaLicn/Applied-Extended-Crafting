package com.gali.applied_extended_crafting.block;

import appeng.block.crafting.PatternProviderBlock;
import com.gali.applied_extended_crafting.blockentity.CrafterCorePatternProviderBlockEntity;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;

public class CrafterCorePatternProviderBlock extends PatternProviderBlock {

    private boolean initialized = false;

    public CrafterCorePatternProviderBlock(Properties properties) {
        super(properties);
    }

    public void initializeBlockEntity() {
        if (!this.initialized) {
            setBlockEntity(
                    (Class) CrafterCorePatternProviderBlockEntity.class,
                    (BlockEntityType) ModBlockEntities.CRAFTER_CORE_PATTERN_PROVIDER.get(),
                    null,
                    (level, pos, state, entity) -> {
                        if (entity instanceof appeng.blockentity.ServerTickingBlockEntity tickingEntity) {
                            tickingEntity.serverTick();
                        }
                    }
            );
            this.initialized = true;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        this.initializeBlockEntity();
        return new CrafterCorePatternProviderBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        this.initializeBlockEntity();
        return super.getTicker(level, state, type);
    }

}
