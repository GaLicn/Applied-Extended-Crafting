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
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import com.blakebr0.extendedcrafting.api.crafting.ICombinationRecipe;
import com.blakebr0.extendedcrafting.init.ModBlocks;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import com.gali.applied_extended_crafting.menu.CrafterCorePatternProviderMenu;
import com.gali.applied_extended_crafting.recipe.CombinationRecipeMatcher;
import com.gali.applied_extended_crafting.recipe.IRecipeMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrafterCorePatternProviderBlockEntity extends AbstractPatternProvider
        implements CrafterCorePatternProviderMenuHost {
    private static final String NBT_PEDESTAL_STACK = "pedestalStack";
    private static final String NBT_PROCESSING_CENTER_INPUT = "processingCenterInput";
    private static final String NBT_PROCESSING_PEDESTAL_INPUTS = "processingPedestalInputs";
    private static final String NBT_PROCESSING_OUTPUTS = "processingOutputs";
    private static final String NBT_PROCESSING_PROGRESS = "processingProgress";
    private static final String NBT_PROCESSING_POWER_TOTAL = "processingPowerTotal";
    private static final String NBT_PROCESSING_POWER_RATE = "processingPowerRate";
    private static final String NBT_PROCESSING_REQUIRED_PEDESTALS = "processingRequiredPedestals";

    private final CombinationRecipeMatcher recipeMatcher = new CombinationRecipeMatcher();
    private final AppEngInternalInventory pedestalInventory = new AppEngInternalInventory(this, 1, 64);
    private final List<GenericStack> processingPedestalInputs = new ArrayList<>();
    private final List<GenericStack> processingOutputs = new ArrayList<>();

    @Nullable
    private GenericStack processingCenterInput;
    private double processingProgress;
    private int processingPowerTotal;
    private int processingPowerRate;
    private int processingRequiredPedestals;

    public CrafterCorePatternProviderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRAFTER_CORE_PATTERN_PROVIDER.get(), pos, state);
        this.pedestalInventory.setFilter(new IAEItemFilter() {
            @Override
            public boolean allowInsert(appeng.api.inventories.InternalInventory inv, int slot, ItemStack stack) {
                return CrafterCorePatternProviderBlockEntity.this.isPedestalStack(stack);
            }
        });
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(CrafterCorePatternProviderMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(CrafterCorePatternProviderMenu.TYPE, player, subMenu.getLocator());
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

        var level = this.getLevel();
        if (level == null || this.getMainNode().getGrid() == null) {
            return false;
        }

        var patternInputs = this.extractInputs(patternDetails).orElse(Map.of());
        var recipe = this.recipeMatcher.findRecipe(patternInputs, this.extractOutputs(patternDetails), level);
        if (recipe == null) {
            return false;
        }

        int requiredPedestals = this.getRequiredPedestalCount(recipe);
        if (this.getPedestalCount() < requiredPedestals) {
            return false;
        }

        var outputs = List.copyOf(patternDetails.getOutputs());
        if (outputs.isEmpty()) {
            return false;
        }

        int powerTotal = Math.max(0, recipe.getPowerCost());
        int powerRate = Math.max(0, recipe.getPowerRate());
        if (powerTotal <= 0 || powerRate <= 0) {
            return this.pushPatternToNetwork(patternDetails);
        }

        var preview = this.buildDisplayPreview(patternDetails, recipe);
        this.processingCenterInput = preview.centerInput();
        this.processingPedestalInputs.clear();
        this.processingPedestalInputs.addAll(preview.pedestalInputs());
        this.processingOutputs.clear();
        this.processingOutputs.addAll(outputs);
        this.processingProgress = 0;
        this.processingPowerTotal = powerTotal;
        this.processingPowerRate = powerRate;
        this.processingRequiredPedestals = requiredPedestals;
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
        this.pedestalInventory.writeToNBT(data, NBT_PEDESTAL_STACK, registries);

        if (this.processingCenterInput != null) {
            data.put(NBT_PROCESSING_CENTER_INPUT, GenericStack.writeTag(registries, this.processingCenterInput));
        }

        ListTag processingPedestalInputsTag = new ListTag();
        for (var processingPedestalInput : this.processingPedestalInputs) {
            processingPedestalInputsTag.add(GenericStack.writeTag(registries, processingPedestalInput));
        }
        data.put(NBT_PROCESSING_PEDESTAL_INPUTS, processingPedestalInputsTag);

        ListTag processingOutputsTag = new ListTag();
        for (var processingOutput : this.processingOutputs) {
            processingOutputsTag.add(GenericStack.writeTag(registries, processingOutput));
        }
        data.put(NBT_PROCESSING_OUTPUTS, processingOutputsTag);
        data.putDouble(NBT_PROCESSING_PROGRESS, this.processingProgress);
        data.putInt(NBT_PROCESSING_POWER_TOTAL, this.processingPowerTotal);
        data.putInt(NBT_PROCESSING_POWER_RATE, this.processingPowerRate);
        data.putInt(NBT_PROCESSING_REQUIRED_PEDESTALS, this.processingRequiredPedestals);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.pedestalInventory.readFromNBT(data, NBT_PEDESTAL_STACK, registries);

        this.processingCenterInput = data.contains(NBT_PROCESSING_CENTER_INPUT, Tag.TAG_COMPOUND)
                ? GenericStack.readTag(registries, data.getCompound(NBT_PROCESSING_CENTER_INPUT))
                : null;

        this.processingPedestalInputs.clear();
        for (Tag processingPedestalInputTag : data.getList(NBT_PROCESSING_PEDESTAL_INPUTS, Tag.TAG_COMPOUND)) {
            var processingPedestalInput = GenericStack.readTag(registries, (CompoundTag) processingPedestalInputTag);
            if (processingPedestalInput != null) {
                this.processingPedestalInputs.add(processingPedestalInput);
            }
        }

        this.processingOutputs.clear();
        for (Tag processingOutputTag : data.getList(NBT_PROCESSING_OUTPUTS, Tag.TAG_COMPOUND)) {
            var processingOutput = GenericStack.readTag(registries, (CompoundTag) processingOutputTag);
            if (processingOutput != null) {
                this.processingOutputs.add(processingOutput);
            }
        }

        this.processingProgress = Math.max(0, data.getDouble(NBT_PROCESSING_PROGRESS));
        this.processingPowerTotal = Math.max(0, data.getInt(NBT_PROCESSING_POWER_TOTAL));
        this.processingPowerRate = Math.max(0, data.getInt(NBT_PROCESSING_POWER_RATE));
        this.processingRequiredPedestals = Math.max(0, data.getInt(NBT_PROCESSING_REQUIRED_PEDESTALS));

        if (this.isProcessing()) {
            this.processingProgress = Math.min(this.processingProgress, this.processingPowerTotal);
        } else {
            this.clearProcessingState();
        }
    }

    @Override
    public AppEngInternalInventory getPedestalInventory() {
        return this.pedestalInventory;
    }

    @Override
    public boolean isProcessing() {
        return this.processingPowerTotal > 0 && this.processingPowerRate > 0 && !this.processingOutputs.isEmpty();
    }

    @Override
    public int getProcessingProgressScaled(int width) {
        if (!this.isProcessing() || width <= 0) {
            return 0;
        }

        return Math.min(width, (int) Math.floor(this.processingProgress * width / this.processingPowerTotal));
    }

    @Override
    public boolean hasProcessingPreview() {
        return this.isProcessing() && (this.processingCenterInput != null
                || !this.processingPedestalInputs.isEmpty()
                || this.getDisplayedResult() != null);
    }

    @Override
    @Nullable
    public GenericStack getDisplayedCenterInput() {
        return this.processingCenterInput;
    }

    @Override
    public List<GenericStack> getDisplayedPedestalInputs() {
        return List.copyOf(this.processingPedestalInputs);
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
    @Nullable
    public GenericStack getDisplayedPedestalStack() {
        return this.toGenericStack(this.pedestalInventory.getStackInSlot(0));
    }

    @Override
    public int getPedestalCount() {
        var pedestalStack = this.pedestalInventory.getStackInSlot(0);
        return this.isPedestalStack(pedestalStack) ? pedestalStack.getCount() : 0;
    }

    public ItemStack getPedestalStack() {
        return this.pedestalInventory.getStackInSlot(0);
    }

    private void processEnergyStep() {
        if (!this.isProcessing() || this.getPedestalCount() < this.processingRequiredPedestals) {
            return;
        }

        var grid = this.getMainNode().getGrid();
        if (grid == null) {
            return;
        }

        double remaining = this.processingPowerTotal - this.processingProgress;
        if (remaining <= 0) {
            this.finishProcessing();
            return;
        }

        double extracted = grid.getEnergyService().extractAEPower(
                Math.min(this.processingPowerRate, remaining),
                Actionable.MODULATE,
                PowerMultiplier.CONFIG
        );
        if (extracted <= 0.0001) {
            return;
        }

        this.processingProgress = Math.min(this.processingPowerTotal, this.processingProgress + extracted);
        this.setChanged();
        if (this.processingProgress + 0.0001 >= this.processingPowerTotal) {
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

    private DisplayPreview buildDisplayPreview(IPatternDetails patternDetails, ICombinationRecipe recipe) {
        var remainingInputs = this.collectDisplayInputs(patternDetails);
        var centerInput = this.takeFirstMatching(remainingInputs, recipe.getInput());
        if (centerInput == null) {
            centerInput = this.createDisplayStack(recipe.getInput());
        }

        var pedestalInputs = new ArrayList<GenericStack>();
        for (var ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) {
                continue;
            }

            var displayInput = this.takeFirstMatching(remainingInputs, ingredient);
            if (displayInput == null) {
                displayInput = this.createDisplayStack(ingredient);
            }

            if (displayInput != null) {
                pedestalInputs.add(displayInput);
            }
        }

        return new DisplayPreview(centerInput, List.copyOf(pedestalInputs));
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

    @Nullable
    private GenericStack takeFirstMatching(List<GenericStack> candidates, Ingredient ingredient) {
        for (int i = 0; i < candidates.size(); i++) {
            var candidate = candidates.get(i);
            if (!AEItemKey.is(candidate.what())) {
                continue;
            }

            var stack = ((AEItemKey) candidate.what()).toStack((int) Math.max(1, candidate.amount()));
            if (!ingredient.test(stack)) {
                continue;
            }

            candidates.remove(i);
            return candidate;
        }

        return null;
    }

    @Nullable
    private GenericStack createDisplayStack(Ingredient ingredient) {
        for (var stack : ingredient.getItems()) {
            if (!stack.isEmpty()) {
                return this.toGenericStack(stack);
            }
        }

        return null;
    }

    @Nullable
    private GenericStack toGenericStack(ItemStack stack) {
        var itemKey = AEItemKey.of(stack);
        if (stack.isEmpty() || itemKey == null) {
            return null;
        }

        return new GenericStack(itemKey, Math.max(1, stack.getCount()));
    }

    private int getRequiredPedestalCount(ICombinationRecipe recipe) {
        int count = 0;
        for (var ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty()) {
                count++;
            }
        }

        return count;
    }

    private boolean isPedestalStack(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModBlocks.PEDESTAL.get().asItem());
    }

    private void clearProcessingState() {
        this.processingCenterInput = null;
        this.processingPedestalInputs.clear();
        this.processingOutputs.clear();
        this.processingProgress = 0;
        this.processingPowerTotal = 0;
        this.processingPowerRate = 0;
        this.processingRequiredPedestals = 0;
    }

    private record DisplayPreview(@Nullable GenericStack centerInput, List<GenericStack> pedestalInputs) {
    }
}
