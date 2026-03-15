package com.gali.applied_extended_crafting.block;

import appeng.block.crafting.PatternProviderBlock;
import com.gali.applied_extended_crafting.blockentity.CrafterCorePatternProviderBlockEntity;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CrafterCorePatternProviderBlock extends PatternProviderBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 2, 14),
            Block.box(0, 2, 0, 16, 5, 16),
            Block.box(1, 5, 1, 15, 6, 15),
            Block.box(0, 6, 0, 16, 12, 16),
            Block.box(1, 12, 1, 15, 13, 15),
            Block.box(0, 13, 0, 16, 16, 16)
    );

    private boolean initialized = false;

    public CrafterCorePatternProviderBlock() {
        super();
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

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CrafterCorePatternProviderBlockEntity patternProvider) {
                var pedestalStack = patternProvider.getPedestalStack();
                if (!pedestalStack.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), pedestalStack);
                }
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }
}
