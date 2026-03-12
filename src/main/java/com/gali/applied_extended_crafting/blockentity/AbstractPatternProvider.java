package com.gali.applied_extended_crafting.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import com.gali.applied_extended_crafting.recipe.IRecipeMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractPatternProvider extends PatternProviderBlockEntity implements IActionHost {
    protected AbstractPatternProvider(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    protected abstract IRecipeMatcher<?> getRecipeMatcher();

    protected int getPatternSlotCount() {
        return 9;
    }

    @Override
    protected PatternProviderLogic createLogic() {
        return new VirtualPatternProviderLogic(this.getMainNode(), this, this.getPatternSlotCount());
    }

    @Override
    public appeng.api.networking.IGridNode getActionableNode() {
        return this.getMainNode().getNode();
    }

    protected boolean isPatternSupported(IPatternDetails patternDetails) {
        var level = this.getLevel();
        if (level == null) {
            return false;
        }

        var patternInputs = this.extractInputs(patternDetails);
        if (patternInputs.isEmpty()) {
            return false;
        }

        return this.getRecipeMatcher().matchesRecipe(patternInputs.get(), this.extractOutputs(patternDetails), level);
    }

    protected boolean pushPatternToNetwork(IPatternDetails patternDetails) {
        if (!this.isPatternSupported(patternDetails)) {
            return false;
        }

        var grid = this.getMainNode().getGrid();
        if (grid == null) {
            return false;
        }

        var storage = grid.getStorageService().getInventory();
        var actionSource = IActionSource.ofMachine(this);
        List<GenericStack> outputs = patternDetails.getOutputs();

        for (var output : outputs) {
            long inserted = storage.insert(output.what(), output.amount(), Actionable.SIMULATE, actionSource);
            if (inserted != output.amount()) {
                return false;
            }
        }

        for (var output : outputs) {
            storage.insert(output.what(), output.amount(), Actionable.MODULATE, actionSource);
        }

        return true;
    }

    protected Optional<Map<AEKey, Long>> extractInputs(IPatternDetails patternDetails) {
        Map<AEKey, Long> result = new HashMap<>();

        for (var input : patternDetails.getInputs()) {
            var possibleInputs = input.getPossibleInputs();
            if (possibleInputs.length != 1) {
                return Optional.empty();
            }

            var possibleInput = possibleInputs[0];
            result.merge(possibleInput.what(), possibleInput.amount() * input.getMultiplier(), Long::sum);
        }

        return Optional.of(result);
    }

    protected Map<AEKey, Long> extractOutputs(IPatternDetails patternDetails) {
        Map<AEKey, Long> result = new HashMap<>();

        for (var output : patternDetails.getOutputs()) {
            result.merge(output.what(), output.amount(), Long::sum);
        }

        return result;
    }

    private final class VirtualPatternProviderLogic extends PatternProviderLogic {
        private VirtualPatternProviderLogic(appeng.api.networking.IManagedGridNode mainNode, AbstractPatternProvider host, int slots) {
            super(mainNode, host, slots);
        }

        @Override
        public List<IPatternDetails> getAvailablePatterns() {
            var level = AbstractPatternProvider.this.getLevel();
            if (level == null) {
                return List.of();
            }

            return super.getAvailablePatterns().stream()
                    .filter(AbstractPatternProvider.this::isPatternSupported)
                    .toList();
        }

        @Override
        public boolean pushPattern(IPatternDetails patternDetails, appeng.api.stacks.KeyCounter[] inputHolder) {
            return AbstractPatternProvider.this.pushPatternToNetwork(patternDetails);
        }

        @Override
        public boolean isBusy() {
            return false;
        }
    }
}
