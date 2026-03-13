package com.gali.applied_extended_crafting.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import com.blakebr0.extendedcrafting.api.crafting.IFluxCrafterRecipe;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import com.gali.applied_extended_crafting.menu.FluxCrafterPatternProviderMenu;
import com.gali.applied_extended_crafting.recipe.FluxCrafterRecipeMatcher;
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

public class FluxCrafterPatternProviderBlockEntity extends AbstractPatternProvider
        implements FluxCrafterPatternProviderMenuHost {
    private static final String NBT_PROCESSING_INPUTS = "processingInputs";
    private static final String NBT_PROCESSING_OUTPUTS = "processingOutputs";
    private static final String NBT_PROCESSING_ENERGY = "processingEnergy";
    private static final String NBT_PROCESSING_ENERGY_TOTAL = "processingEnergyTotal";
    private static final String NBT_PROCESSING_POWER_RATE = "processingPowerRate";

    private final FluxCrafterRecipeMatcher recipeMatcher = new FluxCrafterRecipeMatcher();
    private final List<GenericStack> processingInputs = new ArrayList<>();
    private final List<GenericStack> processingOutputs = new ArrayList<>();
    private double processingEnergy;
    private int processingEnergyTotal;
    private int processingPowerRate;

    public FluxCrafterPatternProviderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUX_CRAFTER_PATTERN_PROVIDER.get(), pos, state);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(FluxCrafterPatternProviderMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(FluxCrafterPatternProviderMenu.TYPE, player, subMenu.getLocator());
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
        if (!this.isPatternSupported(patternDetails) || this.isBusyForPush()) {
            return false;
        }

        if (this.getMainNode().getGrid() == null) {
            return false;
        }

        var level = this.getLevel();
        if (level == null) {
            return false;
        }

        var recipe = this.recipeMatcher.findRecipe(
                this.extractInputs(patternDetails).orElseGet(java.util.Map::of),
                this.extractOutputs(patternDetails),
                level
        );
        if (recipe == null) {
            return false;
        }

        int powerRequired = Math.max(0, recipe.getPowerRequired());
        int powerRate = Math.max(0, recipe.getPowerRate());
        if (powerRequired <= 0 || powerRate <= 0) {
            return this.pushPatternToNetwork(patternDetails);
        }

        var outputs = List.copyOf(patternDetails.getOutputs());
        if (outputs.isEmpty()) {
            return false;
        }

        this.processingInputs.clear();
        this.processingInputs.addAll(this.collectDisplayInputs(patternDetails));
        this.processingOutputs.clear();
        this.processingOutputs.addAll(outputs);
        this.processingEnergy = 0;
        this.processingEnergyTotal = powerRequired;
        this.processingPowerRate = powerRate;
        this.saveChanges();
        return true;
    }

    @Override
    protected boolean isBusyForPush() {
        return this.isProcessing();
    }

    @Override
    public void serverTick() {
        var level = this.getLevel();
        if (level != null && !level.isClientSide() && this.isProcessing()) {
            this.processEnergyStep();
        }

        super.serverTick();
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
        data.putDouble(NBT_PROCESSING_ENERGY, this.processingEnergy);
        data.putInt(NBT_PROCESSING_ENERGY_TOTAL, this.processingEnergyTotal);
        data.putInt(NBT_PROCESSING_POWER_RATE, this.processingPowerRate);
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

        this.processingEnergy = Math.max(0, data.getDouble(NBT_PROCESSING_ENERGY));
        this.processingEnergyTotal = Math.max(0, data.getInt(NBT_PROCESSING_ENERGY_TOTAL));
        this.processingPowerRate = Math.max(0, data.getInt(NBT_PROCESSING_POWER_RATE));
        if (!this.isProcessing()) {
            this.clearProcessingState();
        }
    }

    @Override
    public boolean isProcessing() {
        return this.processingEnergyTotal > 0 && this.processingPowerRate > 0 && !this.processingOutputs.isEmpty();
    }

    @Override
    public int getProcessingProgressScaled(int width) {
        if (!this.isProcessing() || width <= 0) {
            return 0;
        }

        return (int) Math.min(width, this.processingEnergy * width / this.processingEnergyTotal);
    }

    @Override
    public boolean hasProcessingPreview() {
        return this.isProcessing() && (!this.processingInputs.isEmpty() || this.getDisplayedResult() != null);
    }

    @Override
    public List<GenericStack> getDisplayedInputs() {
        return List.copyOf(this.processingInputs);
    }

    @Override
    @Nullable
    public GenericStack getDisplayedResult() {
        for (var output : this.processingOutputs) {
            if (AEItemKey.is(output.what())) {
                return output;
            }
        }

        return null;
    }

    @Override
    public int getMenuEnergyStored() {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, Math.floor(this.processingEnergy)));
    }

    @Override
    public int getMenuEnergyCapacity() {
        return this.processingEnergyTotal;
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

    private void processEnergyStep() {
        var grid = this.getMainNode().getGrid();
        if (grid == null) {
            return;
        }

        double remaining = Math.max(0, this.processingEnergyTotal - this.processingEnergy);
        if (remaining <= 0) {
            this.finishProcessing();
            return;
        }

        double extracted = grid.getEnergyService().extractAEPower(
                Math.min(this.processingPowerRate, remaining),
                Actionable.MODULATE,
                PowerMultiplier.CONFIG
        );
        if (extracted <= 0) {
            return;
        }

        this.processingEnergy = Math.min(this.processingEnergyTotal, this.processingEnergy + extracted);
        this.setChanged();
        if (this.processingEnergy + 0.0001 >= this.processingEnergyTotal) {
            this.finishProcessing();
        }
    }

    private void finishProcessing() {
        if (!this.isProcessing()) {
            return;
        }

        var completedOutputs = List.copyOf(this.processingOutputs);
        this.clearProcessingState();
        this.enqueueOutputs(completedOutputs);
        this.saveChanges();
    }

    private void clearProcessingState() {
        this.processingInputs.clear();
        this.processingOutputs.clear();
        this.processingEnergy = 0;
        this.processingEnergyTotal = 0;
        this.processingPowerRate = 0;
    }
}
