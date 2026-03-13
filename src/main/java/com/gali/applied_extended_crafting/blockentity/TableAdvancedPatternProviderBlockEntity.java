package com.gali.applied_extended_crafting.blockentity;

import com.gali.applied_extended_crafting.init.ModBlockEntities;
import com.gali.applied_extended_crafting.recipe.IRecipeMatcher;
import com.gali.applied_extended_crafting.recipe.TableRecipeMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TableAdvancedPatternProviderBlockEntity extends AbstractPatternProvider {
    private final TableRecipeMatcher recipeMatcher = new TableRecipeMatcher(2);

    public TableAdvancedPatternProviderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TABLE_ADVANCED_PATTERN_PROVIDER.get(), pos, state);
    }

    @Override
    protected IRecipeMatcher<?> getRecipeMatcher() {
        return this.recipeMatcher;
    }

    @Override
    protected int getPatternSlotCount() {
        return 9;
    }
}
