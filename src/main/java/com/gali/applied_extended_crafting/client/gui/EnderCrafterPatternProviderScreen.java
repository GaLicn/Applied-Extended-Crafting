package com.gali.applied_extended_crafting.client.gui;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.menu.EnderCrafterPatternProviderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class EnderCrafterPatternProviderScreen extends PatternProviderScreen<EnderCrafterPatternProviderMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            Applied_extended_crafting.MODID,
            "textures/gui/ender_crafter_pattern_provider.png"
    );

    private static final int PROGRESS_X = 94;
    private static final int PROGRESS_Y = 35;
    private static final int PROGRESS_HEIGHT = 15;
    private static final int PROGRESS_TEXTURE_X = 176;
    private static final int PROGRESS_TEXTURE_Y = 0;
    private static final int EMPTY_PATTERN_SLOT_TEXTURE_X = 200;
    private static final int EMPTY_PATTERN_SLOT_TEXTURE_Y = 0;
    private static final int EMPTY_PATTERN_SLOT_SIZE = 16;

    public EnderCrafterPatternProviderScreen(EnderCrafterPatternProviderMenu menu, Inventory playerInventory,
                                             Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.setTextContent(TEXT_ID_DIALOG_TITLE, title);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.setSlotsHidden(SlotSemantics.STORAGE, true);
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.renderEmptyPatternSlotBackgrounds(guiGraphics, offsetX, offsetY);

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
