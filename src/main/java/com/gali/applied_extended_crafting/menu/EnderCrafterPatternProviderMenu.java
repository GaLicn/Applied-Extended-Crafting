package com.gali.applied_extended_crafting.menu;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.menu.slot.FakeSlot;
import appeng.util.ConfigInventory;
import com.gali.applied_extended_crafting.Applied_extended_crafting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class EnderCrafterPatternProviderMenu extends PatternProviderMenu {
    private static final int PREVIEW_GRID_SIZE = 9;
    private static final String ACTION_SELECT_PATTERN_SLOT = "selectPatternSlot";

    public static final SlotSemantic PREVIEW_CRAFTING_GRID = SlotSemantics.register(
            "APPLIED_EXTENDED_CRAFTING_PREVIEW_CRAFTING_GRID",
            false
    );
    public static final SlotSemantic PREVIEW_RESULT = SlotSemantics.register(
            "APPLIED_EXTENDED_CRAFTING_PREVIEW_RESULT",
            false
    );

    public static final MenuType<EnderCrafterPatternProviderMenu> TYPE = MenuTypeBuilder
            .create(EnderCrafterPatternProviderMenu::new, PatternProviderLogicHost.class)
            .withMenuTitle(EnderCrafterPatternProviderMenu::getMenuTitle)
            .buildUnregistered(ResourceLocation.fromNamespaceAndPath(
                    Applied_extended_crafting.MODID,
                    "table_pattern_provider"
            ));

    private final ConfigInventory previewGridInv = ConfigInventory.configStacks(PREVIEW_GRID_SIZE)
            .allowOverstacking(true)
            .build();
    private final ConfigInventory previewResultInv = ConfigInventory.configStacks(1)
            .allowOverstacking(true)
            .build();

    private final FakeSlot[] previewGridSlots = new FakeSlot[PREVIEW_GRID_SIZE];

    @GuiSync(100)
    public int selectedPatternSlot = -1;

    public EnderCrafterPatternProviderMenu(MenuType<? extends EnderCrafterPatternProviderMenu> menuType, int id,
                                           Inventory playerInventory, PatternProviderLogicHost host) {
        super(menuType, id, playerInventory, host);

        var previewGrid = this.previewGridInv.createMenuWrapper();
        var previewResult = this.previewResultInv.createMenuWrapper();

        for (int i = 0; i < PREVIEW_GRID_SIZE; i++) {
            var slot = new FakeSlot(previewGrid, i);
            slot.setHideAmount(true);
            this.addSlot(this.previewGridSlots[i] = slot, PREVIEW_CRAFTING_GRID);
        }

        var previewResultSlot = new FakeSlot(previewResult, 0);
        this.addSlot(previewResultSlot, PREVIEW_RESULT);

        this.registerClientAction(ACTION_SELECT_PATTERN_SLOT, Integer.class, this::setSelectedPatternSlotInternal);
        this.updatePreviewFromPattern();
    }

    @Override
    public void broadcastChanges() {
        if (this.isServerSide()) {
            this.updatePreviewFromPattern();
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

    private void updatePreviewFromPattern() {
        this.selectedPatternSlot = this.resolveSelectedPatternSlot();
        this.clearPreview();

        if (this.selectedPatternSlot < 0) {
            return;
        }

        var patternStack = this.logic.getPatternInv().getStackInSlot(this.selectedPatternSlot);
        if (patternStack.isEmpty()) {
            return;
        }

        var details = PatternDetailsHelper.decodePattern(patternStack, this.getPlayer().level());
        if (details == null) {
            return;
        }

        var inputs = details.getInputs();
        for (int i = 0; i < Math.min(inputs.length, PREVIEW_GRID_SIZE); i++) {
            var possibleInputs = inputs[i].getPossibleInputs();
            if (possibleInputs.length == 0) {
                continue;
            }

            var previewStack = possibleInputs[0];
            if (AEItemKey.is(previewStack.what())) {
                this.previewGridInv.setStack(i, new GenericStack(previewStack.what(), Math.max(1, previewStack.amount())));
            }
        }

        for (var output : details.getOutputs()) {
            if (AEItemKey.is(output.what())) {
                this.previewResultInv.setStack(0, new GenericStack(output.what(), Math.max(1, output.amount())));
                break;
            }
        }
    }

    private void clearPreview() {
        this.previewGridInv.clear();
        this.previewResultInv.clear();
    }

    private int resolveSelectedPatternSlot() {
        if (this.isPatternSlotIndexValid(this.selectedPatternSlot)
                && !this.logic.getPatternInv().getStackInSlot(this.selectedPatternSlot).isEmpty()) {
            return this.selectedPatternSlot;
        }

        for (int i = 0; i < Math.min(PREVIEW_GRID_SIZE, this.logic.getPatternInv().size()); i++) {
            if (!this.logic.getPatternInv().getStackInSlot(i).isEmpty()) {
                return i;
            }
        }

        return -1;
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
