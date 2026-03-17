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
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigInventory;
import com.gali.applied_extended_crafting.blockentity.CrafterCorePatternProviderMenuHost;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class CrafterCorePatternProviderMenu extends ProtectedPatternProviderMenu {
    private static final int PATTERN_SLOT_COUNT = 9;
    private static final int UPGRADE_SLOT_COUNT = 6;
    private static final int PREVIEW_GRID_COLUMNS = 7;
    private static final int PREVIEW_GRID_SIZE = PREVIEW_GRID_COLUMNS * PREVIEW_GRID_COLUMNS;
    private static final int CENTER_SLOT_INDEX = PREVIEW_GRID_SIZE / 2;
    private static final int MAX_UNLOCKABLE_SLOTS = PREVIEW_GRID_SIZE - 1;
    private static final int PROCESS_BAR_WIDTH = 21;
    private static final String ACTION_SELECT_PATTERN_SLOT = "selectPatternSlot";
    private static final int[] UNLOCK_ORDER = createUnlockOrder();
    private static final int[] UNLOCK_ORDER_INDEX = createUnlockOrderIndex();

    public static final SlotSemantic PREVIEW_CRAFTING_GRID = SlotSemantics.register(
            "APPLIED_EXTENDED_CRAFTING_CRAFTER_CORE_PREVIEW_CRAFTING_GRID",
            false
    );
    public static final SlotSemantic PREVIEW_RESULT = SlotSemantics.register(
            "APPLIED_EXTENDED_CRAFTING_CRAFTER_CORE_PREVIEW_RESULT",
            false
    );
    public static final SlotSemantic PEDESTAL = SlotSemantics.register(
            "APPLIED_EXTENDED_CRAFTING_CRAFTER_CORE_PEDESTAL",
            false
    );

    public static final MenuType<CrafterCorePatternProviderMenu> TYPE = MenuTypeBuilder
            .create(CrafterCorePatternProviderMenu::new, PatternProviderLogicHost.class)
            .withMenuTitle(CrafterCorePatternProviderMenu::getMenuTitle)
            .build("crafter_core_pattern_provider");

    private final ConfigInventory previewGridInv = ConfigInventory.configStacks(null, PREVIEW_GRID_SIZE, null, true);
    private final ConfigInventory previewResultInv = ConfigInventory.configStacks(null, 1, null, true);

    @GuiSync(100)
    public int selectedPatternSlot = -1;
    @GuiSync(101)
    public int processingProgress;
    @GuiSync(102)
    public boolean processing;
    @GuiSync(103)
    public int pedestalCount;

    public CrafterCorePatternProviderMenu(MenuType<? extends CrafterCorePatternProviderMenu> menuType, int id,
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

        var previewResultSlot = new FakeSlot(previewResult, 0);
        this.addSlot(previewResultSlot, PREVIEW_RESULT);

        if (host instanceof CrafterCorePatternProviderMenuHost patternProviderHost) {
            this.addSlot(new AppEngSlot(patternProviderHost.getPedestalInventory(), 0), PEDESTAL);
            for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
                this.addSlot(new RestrictedInputSlot(
                        RestrictedInputSlot.PlacableItemType.UPGRADES,
                        patternProviderHost.getUpgradeInventory(),
                        i
                ).setStackLimit(1), SlotSemantics.UPGRADE);
            }
        } else {
            var pedestal = ConfigInventory.configStacks(null, 1, null, true).createMenuWrapper();
            var upgrades = ConfigInventory.configStacks(null, UPGRADE_SLOT_COUNT, null, true).createMenuWrapper();
            this.addSlot(new FakeSlot(pedestal, 0), PEDESTAL);
            for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
                this.addSlot(new RestrictedInputSlot(
                        RestrictedInputSlot.PlacableItemType.UPGRADES,
                        upgrades,
                        i
                ).setStackLimit(1), SlotSemantics.UPGRADE);
            }
        }

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

    public int getUnlockedPreviewSlotCount() {
        return Math.min(MAX_UNLOCKABLE_SLOTS, Math.max(0, this.pedestalCount));
    }

    public boolean isPreviewSlotUnlocked(int slotIndex) {
        if (slotIndex == CENTER_SLOT_INDEX) {
            return true;
        }

        if (slotIndex < 0 || slotIndex >= PREVIEW_GRID_SIZE) {
            return false;
        }

        int unlockIndex = UNLOCK_ORDER_INDEX[slotIndex];
        return unlockIndex >= 0 && unlockIndex < this.getUnlockedPreviewSlotCount();
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
        this.updatePedestalState();
        this.updatePreviewFromPattern();
    }

    private void updatePreviewFromPattern() {
        if (!this.isPatternSlotIndexValid(this.selectedPatternSlot)
                || this.logic.getPatternInv().getStackInSlot(this.selectedPatternSlot).isEmpty()) {
            this.selectedPatternSlot = -1;
        }

        this.clearPreview();

        var blockEntity = this.getBlockEntity();
        if (blockEntity instanceof CrafterCorePatternProviderMenuHost patternProvider
                && patternProvider.hasProcessingPreview()) {
            this.updatePreviewFromProcessing(patternProvider);
        }
    }

    private void updatePreviewFromProcessing(CrafterCorePatternProviderMenuHost patternProvider) {
        var centerInput = this.normalizeStack(patternProvider.getDisplayedCenterInput());
        if (centerInput != null) {
            this.previewGridInv.setStack(CENTER_SLOT_INDEX, centerInput);
        }

        int unlockedSlots = this.getUnlockedPreviewSlotCount();
        int previewIndex = 0;
        for (var input : patternProvider.getDisplayedPedestalInputs()) {
            if (previewIndex >= unlockedSlots) {
                break;
            }

            var normalizedInput = this.normalizeStack(input);
            if (normalizedInput == null) {
                continue;
            }

            this.previewGridInv.setStack(UNLOCK_ORDER[previewIndex++], normalizedInput);
        }

        var result = this.normalizeStack(patternProvider.getDisplayedResult());
        if (result != null) {
            this.previewResultInv.setStack(0, result);
        }
    }

    private void updateProcessingState() {
        var blockEntity = this.getBlockEntity();
        if (blockEntity instanceof CrafterCorePatternProviderMenuHost patternProvider) {
            this.processing = patternProvider.isProcessing();
            this.processingProgress = patternProvider.getProcessingProgressScaled(PROCESS_BAR_WIDTH);
            return;
        }

        this.processing = false;
        this.processingProgress = 0;
    }

    private void updatePedestalState() {
        this.pedestalCount = 0;

        var blockEntity = this.getBlockEntity();
        if (blockEntity instanceof CrafterCorePatternProviderMenuHost patternProvider) {
            this.pedestalCount = Math.max(0, patternProvider.getPedestalCount());
        }
    }

    private void clearPreview() {
        this.previewGridInv.clear();
        this.previewResultInv.clear();
    }

    private boolean isPatternSlotIndexValid(int slot) {
        return slot >= 0 && slot < Math.min(PATTERN_SLOT_COUNT, this.logic.getPatternInv().size());
    }

    @Nullable
    private GenericStack normalizeStack(@Nullable GenericStack stack) {
        if (stack == null || !AEItemKey.is(stack.what())) {
            return null;
        }

        return new GenericStack(stack.what(), Math.max(1, stack.amount()));
    }

    private static int[] createUnlockOrder() {
        int[] unlockOrder = new int[MAX_UNLOCKABLE_SLOTS];
        int cursor = 0;
        int center = PREVIEW_GRID_COLUMNS / 2;

        for (int radius = 1; radius <= center; radius++) {
            int min = center - radius;
            int max = center + radius;

            for (int column = min; column <= max; column++) {
                unlockOrder[cursor++] = getPreviewSlotIndex(column, min);
            }
            for (int row = min + 1; row <= max; row++) {
                unlockOrder[cursor++] = getPreviewSlotIndex(max, row);
            }
            for (int column = max - 1; column >= min; column--) {
                unlockOrder[cursor++] = getPreviewSlotIndex(column, max);
            }
            for (int row = max - 1; row > min; row--) {
                unlockOrder[cursor++] = getPreviewSlotIndex(min, row);
            }
        }

        return unlockOrder;
    }

    private static int[] createUnlockOrderIndex() {
        int[] unlockOrderIndex = new int[PREVIEW_GRID_SIZE];
        Arrays.fill(unlockOrderIndex, -1);

        for (int i = 0; i < UNLOCK_ORDER.length; i++) {
            unlockOrderIndex[UNLOCK_ORDER[i]] = i;
        }

        return unlockOrderIndex;
    }

    private static int getPreviewSlotIndex(int column, int row) {
        return row * PREVIEW_GRID_COLUMNS + column;
    }

    private static Component getMenuTitle(PatternProviderLogicHost host) {
        var blockEntity = host.getBlockEntity();
        if (blockEntity == null) {
            return Component.empty();
        }

        return new ItemStack(blockEntity.getBlockState().getBlock()).getHoverName();
    }
}
