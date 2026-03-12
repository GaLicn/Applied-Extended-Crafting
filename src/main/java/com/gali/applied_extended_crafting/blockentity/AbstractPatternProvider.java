package com.gali.applied_extended_crafting.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import com.gali.applied_extended_crafting.recipe.IRecipeMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractPatternProvider extends PatternProviderBlockEntity
        implements IActionHost, ServerTickingBlockEntity {
    private static final String NBT_PENDING_OUTPUTS = "pendingOutputs";

    private final List<GenericStack> pendingOutputs = new ArrayList<>();

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
    public IGridNode getActionableNode() {
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

        this.pendingOutputs.addAll(patternDetails.getOutputs());
        this.saveChanges();

        return true;
    }

    @Override
    public void serverTick() {
        if (this.pendingOutputs.isEmpty()) {
            return;
        }

        var level = this.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        var grid = this.getMainNode().getGrid();
        if (grid == null) {
            return;
        }

        var storage = grid.getStorageService().getInventory();
        var actionSource = IActionSource.ofMachine(this);
        boolean changed = false;

        for (ListIterator<GenericStack> iterator = this.pendingOutputs.listIterator(); iterator.hasNext();) {
            var pendingOutput = iterator.next();
            long inserted = storage.insert(pendingOutput.what(), pendingOutput.amount(), Actionable.MODULATE,
                    actionSource);
            if (inserted <= 0) {
                continue;
            }

            changed = true;
            if (inserted >= pendingOutput.amount()) {
                iterator.remove();
            } else {
                iterator.set(new GenericStack(pendingOutput.what(), pendingOutput.amount() - inserted));
            }
        }

        if (changed) {
            this.saveChanges();
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);

        ListTag pendingOutputsTag = new ListTag();
        for (var pendingOutput : this.pendingOutputs) {
            pendingOutputsTag.add(GenericStack.writeTag(registries, pendingOutput));
        }
        data.put(NBT_PENDING_OUTPUTS, pendingOutputsTag);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);

        this.pendingOutputs.clear();
        for (Tag pendingOutputTag : data.getList(NBT_PENDING_OUTPUTS, Tag.TAG_COMPOUND)) {
            var pendingOutput = GenericStack.readTag(registries, (CompoundTag) pendingOutputTag);
            if (pendingOutput != null) {
                this.pendingOutputs.add(pendingOutput);
            }
        }
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
        private VirtualPatternProviderLogic(IManagedGridNode mainNode, AbstractPatternProvider host,
                                            int slots) {
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
