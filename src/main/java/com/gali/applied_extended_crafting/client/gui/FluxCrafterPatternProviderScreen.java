package com.gali.applied_extended_crafting.client.gui;

import appeng.api.config.LockCraftingMode;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.menu.FluxCrafterPatternProviderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class FluxCrafterPatternProviderScreen extends AEBaseScreen<FluxCrafterPatternProviderMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            Applied_extended_crafting.MODID,
            "textures/gui/flux_crafter_pattern_provider.png"
    );

    private static final int PROGRESS_X = 94;
    private static final int PROGRESS_Y = 35;
    private static final int PROGRESS_HEIGHT = 15;
    private static final int PROGRESS_TEXTURE_X = 176;
    private static final int PROGRESS_TEXTURE_Y = 0;

    private static final int ENERGY_FILL_X = 10;
    private static final int ENERGY_FILL_Y = 21;
    private static final int ENERGY_WIDTH = 12;
    private static final int ENERGY_HEIGHT = 40;
    private static final int ENERGY_TEXTURE_X = 176;
    private static final int ENERGY_TEXTURE_Y = 16;

    private static final int EMPTY_PATTERN_SLOT_TEXTURE_X = 200;
    private static final int EMPTY_PATTERN_SLOT_TEXTURE_Y = 0;
    private static final int EMPTY_PATTERN_SLOT_SIZE = 16;
    private final FluxCrafterPatternProviderLockReason lockReason;

    public FluxCrafterPatternProviderScreen(FluxCrafterPatternProviderMenu menu, Inventory playerInventory,
                                            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.setTextContent(TEXT_ID_DIALOG_TITLE, title);

        this.lockReason = new FluxCrafterPatternProviderLockReason(this);
        this.widgets.add("lockReason", this.lockReason);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.setSlotsHidden(SlotSemantics.STORAGE, true);
        this.lockReason.setVisible(this.menu.getLockCraftingMode() != LockCraftingMode.NONE);
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.renderEmptyPatternSlotBackgrounds(guiGraphics, offsetX, offsetY);
        this.renderEnergyBar(guiGraphics, offsetX, offsetY);

        int progress = this.getProgressWidth();
        if (progress > 0) {
            guiGraphics.blit(
                    BACKGROUND,
                    offsetX + PROGRESS_X,
                    offsetY + PROGRESS_Y,
                    PROGRESS_TEXTURE_X,
                    PROGRESS_TEXTURE_Y,
                    progress,
                    PROGRESS_HEIGHT
            );
        }

        this.renderSelectedPatternHighlight(guiGraphics, offsetX, offsetY);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        int stored = this.menu.getEnergyStored();
        int capacity = this.menu.getEnergyCapacity();
        if (stored <= 0 || capacity <= 0) {
            return;
        }

        int height = Math.max(1, Math.min(ENERGY_HEIGHT, stored * ENERGY_HEIGHT / capacity));
        int offset = ENERGY_HEIGHT - height;
        guiGraphics.blit(
                BACKGROUND,
                offsetX + ENERGY_FILL_X,
                offsetY + ENERGY_FILL_Y + offset,
                ENERGY_TEXTURE_X,
                ENERGY_TEXTURE_Y + offset,
                ENERGY_WIDTH,
                height
        );
    }

    private void renderEmptyPatternSlotBackgrounds(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        for (Slot slot : this.menu.getSlots(SlotSemantics.ENCODED_PATTERN)) {
            if (!slot.getItem().isEmpty()) {
                continue;
            }

            guiGraphics.blit(
                    BACKGROUND,
                    offsetX + slot.x,
                    offsetY + slot.y,
                    EMPTY_PATTERN_SLOT_TEXTURE_X,
                    EMPTY_PATTERN_SLOT_TEXTURE_Y,
                    EMPTY_PATTERN_SLOT_SIZE,
                    EMPTY_PATTERN_SLOT_SIZE
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Slot clickedSlot = null;
        for (Slot slot : this.menu.slots) {
            if (this.isHovering(slot, mouseX, mouseY)) {
                clickedSlot = slot;
                break;
            }
        }

        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        if (clickedSlot != null) {
            List<Slot> patternSlots = this.menu.getSlots(SlotSemantics.ENCODED_PATTERN);
            int selectedIndex = patternSlots.indexOf(clickedSlot);
            if (selectedIndex >= 0) {
                this.menu.selectPatternSlot(selectedIndex);
                return true;
            }
        }

        return handled;
    }

    private void renderSelectedPatternHighlight(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        int selectedSlot = this.menu.getSelectedPatternSlot();
        if (selectedSlot < 0) {
            return;
        }

        var patternSlots = this.menu.getSlots(SlotSemantics.ENCODED_PATTERN);
        if (selectedSlot >= patternSlots.size()) {
            return;
        }

        var slot = patternSlots.get(selectedSlot);
        int left = offsetX + slot.x;
        int top = offsetY + slot.y;
        guiGraphics.fill(left, top, left + 16, top + 16, 0x669cd3ff);
        guiGraphics.hLine(left, left + 15, top, 0xFFDAFFFF);
        guiGraphics.hLine(left, left + 15, top + 15, 0xFFDAFFFF);
        guiGraphics.vLine(left, top, top + 15, 0xFFDAFFFF);
        guiGraphics.vLine(left + 15, top, top + 15, 0xFFDAFFFF);
    }

    private int getProgressWidth() {
        if (!this.menu.isProcessing()) {
            return 0;
        }

        return this.menu.getProcessingProgress();
    }
}
