package com.gali.applied_extended_crafting.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.api.config.PowerMultiplier;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import com.gali.applied_extended_crafting.menu.TablePatternProviderMenu;
import com.gali.applied_extended_crafting.recipe.IRecipeMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractPatternProvider extends PatternProviderBlockEntity
        implements IActionHost, ServerTickingBlockEntity, InternalInventoryHost {
    private static final String NBT_PENDING_OUTPUTS = "pendingOutputs";
    private static final String NBT_POWERED = "powered";

    private final List<GenericStack> pendingOutputs = new ArrayList<>();
    private boolean powered;

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
    public void onReady() {
        super.onReady();

        if (!this.isClientSide()) {
            this.updatePowerState(true);
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);

        if (!this.isClientSide() && reason != IGridNodeListener.State.GRID_BOOT) {
            this.updatePowerState(true);
        }
    }

    @Override
    public IGridNode getActionableNode() {
        return this.getMainNode().getNode();
    }

    @Override
    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(TablePatternProviderMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(TablePatternProviderMenu.TYPE, player, subMenu.getLocator());
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(this.getMainMenuIcon());
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(this.getBlockState().getBlock());
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

    protected boolean isPatternItemValid(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        var level = this.getLevel();
        if (level == null) {
            return false;
        }

        var patternDetails = PatternDetailsHelper.decodePattern(stack, level);
        return patternDetails != null && this.isPatternSupported(patternDetails);
    }

    protected boolean pushPatternToNetwork(IPatternDetails patternDetails) {
        if (!this.isPatternSupported(patternDetails)) {
            return false;
        }

        var grid = this.getMainNode().getGrid();
        if (grid == null) {
            return false;
        }

        this.enqueueOutputs(Arrays.asList(patternDetails.getOutputs()));

        return true;
    }

    protected void enqueueOutputs(List<GenericStack> outputs) {
        if (outputs.isEmpty()) {
            return;
        }

        this.pendingOutputs.addAll(outputs);
        this.saveChanges();
    }

    protected boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return this.pushPatternToNetwork(patternDetails);
    }

    protected boolean isBusyForPush() {
        return false;
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
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);

        ListTag pendingOutputsTag = new ListTag();
        for (var pendingOutput : this.pendingOutputs) {
            pendingOutputsTag.add(GenericStack.writeTag(pendingOutput));
        }
        data.put(NBT_PENDING_OUTPUTS, pendingOutputsTag);
    }

    @Override
    protected void saveVisualState(CompoundTag data) {
        super.saveVisualState(data);
        data.putBoolean(NBT_POWERED, this.powered);
    }

    @Override
    protected void loadVisualState(CompoundTag data) {
        super.loadVisualState(data);
        this.powered = data.getBoolean(NBT_POWERED);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);

        this.pendingOutputs.clear();
        for (Tag pendingOutputTag : data.getList(NBT_PENDING_OUTPUTS, Tag.TAG_COMPOUND)) {
            var pendingOutput = GenericStack.readTag((CompoundTag) pendingOutputTag);
            if (pendingOutput != null) {
                this.pendingOutputs.add(pendingOutput);
            }
        }
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        boolean oldPowered = this.powered;
        this.powered = data.readBoolean();
        return changed || oldPowered != this.powered;
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.powered);
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

    public boolean isPowered() {
        return this.powered;
    }

    private void updatePowerState(boolean syncToClient) {
        boolean newState = false;

        var grid = this.getMainNode().getGrid();
        if (grid != null) {
            newState = this.getMainNode().isPowered()
                    && grid.getEnergyService().extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.0001;
        }

        if (newState != this.powered) {
            this.powered = newState;
            if (syncToClient) {
                this.markForUpdate();
            }
        }
    }

    private final class VirtualPatternProviderLogic extends PatternProviderLogic {
        private VirtualPatternProviderLogic(IManagedGridNode mainNode, AbstractPatternProvider host,
                                            int slots) {
            super(mainNode, host, slots);

            if (this.getPatternInv() instanceof AppEngInternalInventory patternInventory) {
                patternInventory.setFilter(new IAEItemFilter() {
                    @Override
                    public boolean allowInsert(appeng.api.inventories.InternalInventory inv, int slot, ItemStack stack) {
                        return AbstractPatternProvider.this.isPatternItemValid(stack);
                    }
                });
            }
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
        public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
            return AbstractPatternProvider.this.pushPattern(patternDetails, inputHolder);
        }

        @Override
        public boolean isBusy() {
            return AbstractPatternProvider.this.isBusyForPush();
        }
    }
}
