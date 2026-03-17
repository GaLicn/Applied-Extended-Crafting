package com.gali.applied_extended_crafting.menu;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import appeng.util.ConfigInventory;
import com.gali.applied_extended_crafting.blockentity.FluxCrafterPatternProviderMenuHost;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class FluxCrafterPatternProviderMenu extends ProtectedPatternProviderMenu {
    private static final int PREVIEW_GRID_SIZE = 9;
    private static final int PROCESS_BAR_WIDTH = 22;
    private static final String ACTION_SELECT_PATTERN_SLOT = "selectPatternSlot";

    public static final SlotSemantic PREVIEW_CRAFTING_GRID = SlotSemantics.register(
            "APPLIED_EXTENDED_CRAFTING_FLUX_PREVIEW_CRAFTING_GRID",
            false
    );
    public static final SlotSemantic PREVIEW_RESULT = SlotSemantics.register(
            "APPLIED_EXTENDED_CRAFTING_FLUX_PREVIEW_RESULT",
            false
    );

    public static final MenuType<FluxCrafterPatternProviderMenu> TYPE = MenuTypeBuilder
            .create(FluxCrafterPatternProviderMenu::new, PatternProviderLogicHost.class)
            .withMenuTitle(FluxCrafterPatternProviderMenu::getMenuTitle)
            .build("flux_crafter_pattern_provider");

    private final ConfigInventory previewGridInv = ConfigInventory.configStacks(null, PREVIEW_GRID_SIZE, null, true);
    private final ConfigInventory previewResultInv = ConfigInventory.configStacks(null, 1, null, true);

    @GuiSync(100)
    public int selectedPatternSlot = -1;
    @GuiSync(101)
    public int processingProgress;
    @GuiSync(102)
    public boolean processing;
    @GuiSync(103)
    public int energyStored;
    @GuiSync(104)
    public int energyCapacity;

    public FluxCrafterPatternProviderMenu(MenuType<? extends FluxCrafterPatternProviderMenu> menuType, int id,
                                          Inventory playerInventory, PatternProviderLogicHost host) {
        super(menuType, id, playerInventory, host);

        this.getSlots(SlotSemantics.ENCODED_PATTERN).forEach(slot -> {
            if (slot instanceof AppEngSlot appEngSlot) {
                appEngSlot.setIcon(null);
            }
        });

        this.getSlots(SlotSemantics.STORAGE).forEach(slot -> {
            if (slot instanceof AppEngSlot appEngSlot) {
                appEngSlot.setSlotEnabled(false);
                appEngSlot.setActive(false);
            }
        });

        var previewGrid = this.previewGridInv.createMenuWrapper();
        var previewResult = this.previewResultInv.createMenuWrapper();

        for (int i = 0; i < PREVIEW_GRID_SIZE; i++) {
            var slot = new FakeSlot(previewGrid, i);
            slot.setHideAmount(true);
            this.addSlot(slot, PREVIEW_CRAFTING_GRID);
        }

        this.addSlot(new FakeSlot(previewResult, 0), PREVIEW_RESULT);

        this.registerClientAction(ACTION_SELECT_PATTERN_SLOT, Integer.class, this::setSelectedPatternSlotInternal);
        this.updateMenuState();
        this.sealProtectedSlotPlan();
    }

    @Override
    public void broadcastChanges() {
        if (this.isServerSide()) {
            this.updateMenuState();
        }

        super.broadcastChanges();
    }

    public void selectPatternSlot(int slot) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SELECT_PATTERN_SLOT, slot);
        } else {
            this.setSelectedPatternSlotInternal(slot);
        }
    }

    public int getSelectedPatternSlot() {
        return this.selectedPatternSlot;
    }

    public int getProcessingProgress() {
        return this.processingProgress;
    }

    public boolean isProcessing() {
        return this.processing;
    }

    public int getEnergyStored() {
        return this.energyStored;
    }

    public int getEnergyCapacity() {
        return this.energyCapacity;
    }

    private void setSelectedPatternSlotInternal(Integer slot) {
        if (slot == null) {
            return;
        }

        if (this.isPatternSlotIndexValid(slot)) {
            this.selectedPatternSlot = slot;
        } else {
            this.selectedPatternSlot = -1;
        }

        this.updatePreviewFromPattern();
    }

    private void updateMenuState() {
        this.updateProcessingState();
        this.updateEnergyState();
        this.updatePreviewFromPattern();
    }

    private void updatePreviewFromPattern() {
        if (!this.isPatternSlotIndexValid(this.selectedPatternSlot)
                || this.logic.getPatternInv().getStackInSlot(this.selectedPatternSlot).isEmpty()) {
            this.selectedPatternSlot = -1;
        }

        this.clearPreview();

        var blockEntity = this.getBlockEntity();
        if (blockEntity instanceof FluxCrafterPatternProviderMenuHost patternProvider
                && patternProvider.hasProcessingPreview()) {
            this.updatePreviewFromProcessing(patternProvider);
        }
    }

    private void updatePreviewFromProcessing(FluxCrafterPatternProviderMenuHost patternProvider) {
        int previewIndex = 0;
        for (var input : patternProvider.getDisplayedInputs()) {
            if (previewIndex >= PREVIEW_GRID_SIZE) {
                break;
            }

            if (AEItemKey.is(input.what())) {
                this.previewGridInv.setStack(previewIndex++, new GenericStack(input.what(), Math.max(1, input.amount())));
            }
        }

        var result = patternProvider.getDisplayedResult();
        if (result != null && AEItemKey.is(result.what())) {
            this.previewResultInv.setStack(0, new GenericStack(result.what(), Math.max(1, result.amount())));
        }
    }

    private void updateProcessingState() {
        var blockEntity = this.getBlockEntity();
        if (blockEntity instanceof FluxCrafterPatternProviderMenuHost patternProvider) {
            this.processing = patternProvider.isProcessing();
            this.processingProgress = patternProvider.getProcessingProgressScaled(PROCESS_BAR_WIDTH);
            return;
        }

        this.processing = false;
        this.processingProgress = 0;
    }

    private void updateEnergyState() {
        var blockEntity = this.getBlockEntity();
        if (blockEntity instanceof FluxCrafterPatternProviderMenuHost patternProvider) {
            this.energyCapacity = Math.max(0, patternProvider.getMenuEnergyCapacity());
            this.energyStored = Math.max(0, patternProvider.getMenuEnergyStored());
            if (this.energyCapacity > 0) {
                this.energyStored = Math.min(this.energyStored, this.energyCapacity);
            }
            return;
        }

        this.energyStored = 0;
        this.energyCapacity = 0;
    }

    private void clearPreview() {
        this.previewGridInv.clear();
        this.previewResultInv.clear();
    }

    private boolean isPatternSlotIndexValid(int slot) {
        return slot >= 0 && slot < Math.min(PREVIEW_GRID_SIZE, this.logic.getPatternInv().size());
    }

    private static Component getMenuTitle(PatternProviderLogicHost host) {
        var blockEntity = host.getBlockEntity();
        if (blockEntity == null) {
            return Component.empty();
        }

        return new ItemStack(blockEntity.getBlockState().getBlock()).getHoverName();
    }
}
