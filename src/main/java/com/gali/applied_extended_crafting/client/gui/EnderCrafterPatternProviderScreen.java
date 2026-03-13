package com.gali.applied_extended_crafting.client.gui;

import appeng.api.config.LockCraftingMode;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.blockentity.AbstractPatternProvider;
import com.gali.applied_extended_crafting.menu.EnderCrafterPatternProviderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class EnderCrafterPatternProviderScreen extends AEBaseScreen<EnderCrafterPatternProviderMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            Applied_extended_crafting.MODID,
            "textures/gui/ender_crafter_pattern_provider.png"
    );

    private static final int PROGRESS_X = 94;
    private static final int PROGRESS_Y = 35;
    private static final int PROGRESS_WIDTH = 22;
    private static final int PROGRESS_HEIGHT = 15;
    private static final int PROGRESS_TEXTURE_X = 176;
    private static final int PROGRESS_TEXTURE_Y = 0;

    private final EnderCrafterPatternProviderLockReason lockReason;

    public EnderCrafterPatternProviderScreen(EnderCrafterPatternProviderMenu menu, Inventory playerInventory,
                                             Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.setTextContent(TEXT_ID_DIALOG_TITLE, title);

        this.lockReason = new EnderCrafterPatternProviderLockReason(this);
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
        var blockEntity = this.menu.getBlockEntity();
        if (!(blockEntity instanceof AbstractPatternProvider patternProvider) || !patternProvider.isPowered()) {
            return 0;
        }

        var level = patternProvider.getLevel();
        if (level == null) {
            return 0;
        }

        long frame = (level.getGameTime() / 2L) % (PROGRESS_WIDTH + 1L);
        return frame <= 0 ? PROGRESS_WIDTH : (int) frame;
    }

}
