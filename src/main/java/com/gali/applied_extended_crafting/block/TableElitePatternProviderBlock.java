package com.gali.applied_extended_crafting.block;

import appeng.block.crafting.PatternProviderBlock;
import com.gali.applied_extended_crafting.blockentity.TableElitePatternProviderBlockEntity;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TableElitePatternProviderBlock extends PatternProviderBlock {
    private boolean initialized = false;

    public TableElitePatternProviderBlock() {
        super();
    }

    public void initializeBlockEntity() {
        if (!initialized) {
            setBlockEntity(
                    (Class) TableElitePatternProviderBlockEntity.class,
                    (BlockEntityType) ModBlockEntities.TABLE_ELITE_PATTERN_PROVIDER.get(),
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
        return new TableElitePatternProviderBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        initializeBlockEntity();
        return super.getTicker(level, state, type);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }
}
