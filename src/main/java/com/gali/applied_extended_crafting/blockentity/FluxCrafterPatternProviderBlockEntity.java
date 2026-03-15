package com.gali.applied_extended_crafting.blockentity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import com.blakebr0.extendedcrafting.tileentity.FluxAlternatorTileEntity;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import com.gali.applied_extended_crafting.menu.FluxCrafterPatternProviderMenu;
import com.gali.applied_extended_crafting.recipe.FluxCrafterRecipeMatcher;
import com.gali.applied_extended_crafting.recipe.IRecipeMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FluxCrafterPatternProviderBlockEntity extends AbstractPatternProvider
        implements FluxCrafterPatternProviderMenuHost {
    private static final int ALTERNATOR_SCAN_RADIUS = 3;
    private static final String NBT_PROCESSING_INPUTS = "processingInputs";
    private static final String NBT_PROCESSING_OUTPUTS = "processingOutputs";
    private static final String NBT_PROCESSING_ENERGY = "processingEnergy";
    private static final String NBT_PROCESSING_ENERGY_TOTAL = "processingEnergyTotal";
    private static final String NBT_PROCESSING_POWER_RATE = "processingPowerRate";

    private final FluxCrafterRecipeMatcher recipeMatcher = new FluxCrafterRecipeMatcher();
    private final List<GenericStack> processingInputs = new ArrayList<>();
    private final List<GenericStack> processingOutputs = new ArrayList<>();
    private int processingEnergy;
    private int processingEnergyTotal;
    private int processingPowerRate;

    public FluxCrafterPatternProviderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUX_CRAFTER_PATTERN_PROVIDER.get(), pos, state);
    }

    @Override
    public void openMenu(Player player, MenuLocator locator) {
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
                this.extractInputs(patternDetails).orElseGet(Map::of),
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

        var outputs = List.copyOf(Arrays.asList(patternDetails.getOutputs()));
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
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);

        ListTag processingInputsTag = new ListTag();
        for (var processingInput : this.processingInputs) {
            processingInputsTag.add(GenericStack.writeTag(processingInput));
        }
        data.put(NBT_PROCESSING_INPUTS, processingInputsTag);

        ListTag processingOutputsTag = new ListTag();
        for (var processingOutput : this.processingOutputs) {
            processingOutputsTag.add(GenericStack.writeTag(processingOutput));
        }
        data.put(NBT_PROCESSING_OUTPUTS, processingOutputsTag);
        data.putInt(NBT_PROCESSING_ENERGY, this.processingEnergy);
        data.putInt(NBT_PROCESSING_ENERGY_TOTAL, this.processingEnergyTotal);
        data.putInt(NBT_PROCESSING_POWER_RATE, this.processingPowerRate);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);

        this.processingInputs.clear();
        for (Tag processingInputTag : data.getList(NBT_PROCESSING_INPUTS, Tag.TAG_COMPOUND)) {
            var processingInput = GenericStack.readTag((CompoundTag) processingInputTag);
            if (processingInput != null) {
                this.processingInputs.add(processingInput);
            }
        }

        this.processingOutputs.clear();
        for (Tag processingOutputTag : data.getList(NBT_PROCESSING_OUTPUTS, Tag.TAG_COMPOUND)) {
            var processingOutput = GenericStack.readTag((CompoundTag) processingOutputTag);
            if (processingOutput != null) {
                this.processingOutputs.add(processingOutput);
            }
        }

        this.processingEnergy = Math.max(0, data.getInt(NBT_PROCESSING_ENERGY));
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

        return Math.min(width, this.processingEnergy * width / this.processingEnergyTotal);
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
        return this.processingEnergy;
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
        if (!this.isProcessing()) {
            return;
        }

        var alternators = this.getAlternators();
        if (alternators.isEmpty()) {
            return;
        }

        this.processingEnergy = Math.min(
                this.processingEnergyTotal,
                this.processingEnergy + (this.processingPowerRate * alternators.size())
        );

        for (var alternator : alternators) {
            alternator.getEnergy().extractEnergy(this.processingPowerRate, false);
        }

        this.setChanged();
        if (this.processingEnergy >= this.processingEnergyTotal) {
            this.finishProcessing();
        }
    }

    private List<FluxAlternatorTileEntity> getAlternators() {
        List<FluxAlternatorTileEntity> alternators = new ArrayList<>();
        var level = this.getLevel();
        if (level == null || this.processingPowerRate <= 0) {
            return alternators;
        }

        var pos = this.getBlockPos();
        BlockPos.betweenClosedStream(
                pos.offset(-ALTERNATOR_SCAN_RADIUS, -ALTERNATOR_SCAN_RADIUS, -ALTERNATOR_SCAN_RADIUS),
                pos.offset(ALTERNATOR_SCAN_RADIUS, ALTERNATOR_SCAN_RADIUS, ALTERNATOR_SCAN_RADIUS)
        ).forEach(checkPos -> {
            var tile = level.getBlockEntity(checkPos);
            if (tile instanceof FluxAlternatorTileEntity alternator
                    && alternator.getEnergy().getEnergyStored() >= this.processingPowerRate) {
                alternators.add(alternator);
            }
        });

        return alternators;
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
