package com.gali.applied_extended_crafting.blockentity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import com.gali.applied_extended_crafting.menu.EnderCrafterPatternProviderMenu;
import com.gali.applied_extended_crafting.recipe.EnderCrafterRecipeMatcher;
import com.gali.applied_extended_crafting.recipe.IRecipeMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnderCrafterPatternProviderBlockEntity extends AbstractPatternProvider {
    private static final String NBT_PROCESSING_INPUTS = "processingInputs";
    private static final String NBT_PROCESSING_OUTPUTS = "processingOutputs";
    private static final String NBT_PROCESSING_TIME = "processingTime";
    private static final String NBT_PROCESSING_TIME_TOTAL = "processingTimeTotal";
    private static final int PROCESSING_DURATION = 100;

    private final EnderCrafterRecipeMatcher recipeMatcher = new EnderCrafterRecipeMatcher();
    private final List<GenericStack> processingInputs = new ArrayList<>();
    private final List<GenericStack> processingOutputs = new ArrayList<>();
    private int processingTime;
    private int processingTimeTotal;

    public EnderCrafterPatternProviderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENDER_CRAFTER_PATTERN_PROVIDER.get(), pos, state);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(EnderCrafterPatternProviderMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(EnderCrafterPatternProviderMenu.TYPE, player, subMenu.getLocator());
    }

    @Override
    protected IRecipeMatcher<?> getRecipeMatcher() {
        return this.recipeMatcher;
    }

    @Override
    protected int getPatternSlotCount() {
        return 9;
    }

    @Override
    protected boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (this.isBusyForPush() || !this.isPatternSupported(patternDetails)) {
            return false;
        }

        if (this.getMainNode().getGrid() == null) {
            return false;
        }

        var outputs = List.copyOf(patternDetails.getOutputs());
        if (outputs.isEmpty()) {
            return false;
        }

        this.processingInputs.clear();
        this.processingInputs.addAll(this.collectDisplayInputs(patternDetails));
        this.processingOutputs.clear();
        this.processingOutputs.addAll(outputs);
        this.processingTime = PROCESSING_DURATION;
        this.processingTimeTotal = PROCESSING_DURATION;
        this.saveChanges();

        return true;
    }

    @Override
    protected boolean isBusyForPush() {
        return this.isProcessing();
    }

    @Override
    public void serverTick() {
        super.serverTick();

        var level = this.getLevel();
        if (level == null || level.isClientSide() || !this.isProcessing()) {
            return;
        }

        this.processingTime--;
        if (this.processingTime > 0) {
            if (this.processingTime % 20 == 0) {
                this.setChanged();
            }
            return;
        }

        var completedOutputs = List.copyOf(this.processingOutputs);
        this.clearProcessingState();
        this.enqueueOutputs(completedOutputs);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);

        ListTag processingInputsTag = new ListTag();
        for (var processingInput : this.processingInputs) {
            processingInputsTag.add(GenericStack.writeTag(registries, processingInput));
        }
        data.put(NBT_PROCESSING_INPUTS, processingInputsTag);

        ListTag processingOutputsTag = new ListTag();
        for (var processingOutput : this.processingOutputs) {
            processingOutputsTag.add(GenericStack.writeTag(registries, processingOutput));
        }
        data.put(NBT_PROCESSING_OUTPUTS, processingOutputsTag);
        data.putInt(NBT_PROCESSING_TIME, this.processingTime);
        data.putInt(NBT_PROCESSING_TIME_TOTAL, this.processingTimeTotal);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);

        this.processingInputs.clear();
        for (Tag processingInputTag : data.getList(NBT_PROCESSING_INPUTS, Tag.TAG_COMPOUND)) {
            var processingInput = GenericStack.readTag(registries, (CompoundTag) processingInputTag);
            if (processingInput != null) {
                this.processingInputs.add(processingInput);
            }
        }

        this.processingOutputs.clear();
        for (Tag processingOutputTag : data.getList(NBT_PROCESSING_OUTPUTS, Tag.TAG_COMPOUND)) {
            var processingOutput = GenericStack.readTag(registries, (CompoundTag) processingOutputTag);
            if (processingOutput != null) {
                this.processingOutputs.add(processingOutput);
            }
        }

        this.processingTime = Math.max(0, data.getInt(NBT_PROCESSING_TIME));
        this.processingTimeTotal = Math.max(0, data.getInt(NBT_PROCESSING_TIME_TOTAL));
        if (this.processingTime <= 0 || this.processingOutputs.isEmpty()) {
            this.clearProcessingState();
        }
    }

    public boolean isProcessing() {
        return this.processingTime > 0 && this.processingTimeTotal > 0 && !this.processingOutputs.isEmpty();
    }

    public int getProcessingProgressScaled(int width) {
        if (!this.isProcessing() || width <= 0) {
            return 0;
        }

        long completedTime = (long) this.processingTimeTotal - this.processingTime;
        return (int) Math.min(width, completedTime * width / this.processingTimeTotal);
    }

    public boolean hasProcessingPreview() {
        return this.isProcessing() && (!this.processingInputs.isEmpty() || this.getDisplayedResult() != null);
    }

    public List<GenericStack> getDisplayedInputs() {
        return List.copyOf(this.processingInputs);
    }

    @Nullable
    public GenericStack getDisplayedResult() {
        for (var output : this.processingOutputs) {
            if (AEItemKey.is(output.what())) {
                return output;
            }
        }

        return null;
    }

    private List<GenericStack> collectDisplayInputs(IPatternDetails patternDetails) {
        var result = new ArrayList<GenericStack>();

        for (var input : patternDetails.getInputs()) {
            var possibleInputs = input.getPossibleInputs();
            if (possibleInputs.length == 0) {
                continue;
            }

            var displayInput = possibleInputs[0];
            result.add(new GenericStack(displayInput.what(), Math.max(1, displayInput.amount() * input.getMultiplier())));
        }

        return result;
    }

    private void clearProcessingState() {
        this.processingInputs.clear();
        this.processingOutputs.clear();
        this.processingTime = 0;
        this.processingTimeTotal = 0;
    }
}
