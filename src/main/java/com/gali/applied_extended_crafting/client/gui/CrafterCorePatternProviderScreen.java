package com.gali.applied_extended_crafting.client.gui;

import appeng.api.config.LockCraftingMode;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.menu.CrafterCorePatternProviderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.lang.reflect.Field;
import java.util.List;

public class CrafterCorePatternProviderScreen extends AEBaseScreen<CrafterCorePatternProviderMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            Applied_extended_crafting.MODID,
            "textures/gui/crafter_core_pattern_provider.png"
    );

    private static final int PREVIEW_GRID_RENDER_OFFSET_X = 1;
    private static final int PREVIEW_GRID_RENDER_OFFSET_Y = 1;
    private static final int PREVIEW_GRID_COLUMNS = 7;
    private static final int PREVIEW_GRID_LEFT = 36;
    private static final int PREVIEW_GRID_TOP = 16;
    private static final int SLOT_SPACING = 18;
    private static final int OUTPUT_SLOT_X = 199;
    private static final int OUTPUT_SLOT_Y = 71;
    private static final int PEDESTAL_SLOT_X = 172;
    private static final int PEDESTAL_SLOT_Y = 101;
    private static final int UPGRADE_GRID_LEFT = 170;
    private static final int UPGRADE_GRID_TOP = 18;
    private static final int UPGRADE_GRID_COLUMNS = 3;
    private static final int UPGRADE_SLOT_SPACING = 21;

    private static final int PROGRESS_X = 167;
    private static final int PROGRESS_Y = 71;
    private static final int PROGRESS_WIDTH = 21;
    private static final int PROGRESS_HEIGHT = 15;
    private static final int PROGRESS_TEXTURE_X = 234;
    private static final int PROGRESS_TEXTURE_Y = 0;

    private static final int LOCKED_SLOT_TEXTURE_X = 238;
    private static final int LOCKED_SLOT_TEXTURE_Y = 18;
    private static final int EMPTY_PATTERN_SLOT_TEXTURE_X = 238;
    private static final int EMPTY_PATTERN_SLOT_TEXTURE_Y = 36;
    private static final int EMPTY_PEDESTAL_SLOT_TEXTURE_X = 237;
    private static final int EMPTY_PEDESTAL_SLOT_TEXTURE_Y = 54;
    private static final int SLOT_RENDER_SIZE = 16;
    private static final Field SLOT_X_FIELD = getSlotField("x", "f_40220_");
    private static final Field SLOT_Y_FIELD = getSlotField("y", "f_40221_");
    private final CrafterCorePatternProviderLockReason lockReason;

    public CrafterCorePatternProviderScreen(CrafterCorePatternProviderMenu menu, Inventory playerInventory,
                                            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.setTextContent(TEXT_ID_DIALOG_TITLE, title);

        this.lockReason = new CrafterCorePatternProviderLockReason(this);
        this.widgets.add("lockReason", this.lockReason);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.setSlotsHidden(SlotSemantics.STORAGE, true);
        this.lockReason.setVisible(this.menu.getLockCraftingMode() != LockCraftingMode.NONE);
        this.positionPreviewGridSlots();
        this.positionSingleSlot(this.menu.getSlots(CrafterCorePatternProviderMenu.PREVIEW_RESULT), OUTPUT_SLOT_X,
                OUTPUT_SLOT_Y);
        this.positionSingleSlot(this.menu.getSlots(CrafterCorePatternProviderMenu.PEDESTAL), PEDESTAL_SLOT_X,
                PEDESTAL_SLOT_Y);
        this.positionUpgradeSlots();
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.renderLockedPreviewSlotBackgrounds(guiGraphics, offsetX, offsetY);
        this.renderEmptyPatternSlotBackgrounds(guiGraphics, offsetX, offsetY);
        this.renderEmptyPedestalSlotBackground(guiGraphics, offsetX, offsetY);

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

    private void positionPreviewGridSlots() {
        var previewSlots = this.menu.getSlots(CrafterCorePatternProviderMenu.PREVIEW_CRAFTING_GRID);
        for (int i = 0; i < previewSlots.size(); i++) {
            this.setSlotPosition(
                    previewSlots.get(i),
                    PREVIEW_GRID_LEFT + PREVIEW_GRID_RENDER_OFFSET_X + (i % PREVIEW_GRID_COLUMNS) * SLOT_SPACING,
                    PREVIEW_GRID_TOP + PREVIEW_GRID_RENDER_OFFSET_Y + (i / PREVIEW_GRID_COLUMNS) * SLOT_SPACING
            );
        }
    }

    private void positionSingleSlot(List<Slot> slots, int x, int y) {
        if (slots.isEmpty()) {
            return;
        }

        this.setSlotPosition(slots.get(0), x, y);
    }

    private void positionUpgradeSlots() {
        var upgradeSlots = this.menu.getSlots(SlotSemantics.UPGRADE);
        for (int i = 0; i < upgradeSlots.size(); i++) {
            this.setSlotPosition(
                    upgradeSlots.get(i),
                    UPGRADE_GRID_LEFT + (i % UPGRADE_GRID_COLUMNS) * UPGRADE_SLOT_SPACING,
                    UPGRADE_GRID_TOP + (i / UPGRADE_GRID_COLUMNS) * UPGRADE_SLOT_SPACING
            );
        }
    }

    private void renderLockedPreviewSlotBackgrounds(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        var previewSlots = this.menu.getSlots(CrafterCorePatternProviderMenu.PREVIEW_CRAFTING_GRID);
        for (int i = 0; i < previewSlots.size(); i++) {
            if (this.menu.isPreviewSlotUnlocked(i)) {
                continue;
            }

            var slot = previewSlots.get(i);
            guiGraphics.blit(
                    BACKGROUND,
                    offsetX + slot.x,
                    offsetY + slot.y,
                    LOCKED_SLOT_TEXTURE_X,
                    LOCKED_SLOT_TEXTURE_Y,
                    SLOT_RENDER_SIZE,
                    SLOT_RENDER_SIZE
            );
        }
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
                    SLOT_RENDER_SIZE,
                    SLOT_RENDER_SIZE
            );
        }
    }

    private void renderEmptyPedestalSlotBackground(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        for (Slot slot : this.menu.getSlots(CrafterCorePatternProviderMenu.PEDESTAL)) {
            if (!slot.getItem().isEmpty()) {
                continue;
            }

            guiGraphics.blit(
                    BACKGROUND,
                    offsetX + slot.x,
                    offsetY + slot.y,
                    EMPTY_PEDESTAL_SLOT_TEXTURE_X,
                    EMPTY_PEDESTAL_SLOT_TEXTURE_Y,
                    SLOT_RENDER_SIZE,
                    SLOT_RENDER_SIZE
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

        return Math.min(PROGRESS_WIDTH, this.menu.getProcessingProgress());
    }

    private void setSlotPosition(Slot slot, int x, int y) {
        this.setSlotCoordinate(SLOT_X_FIELD, slot, x);
        this.setSlotCoordinate(SLOT_Y_FIELD, slot, y);
    }

    private void setSlotCoordinate(Field field, Slot slot, int value) {
        try {
            field.setInt(slot, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to update slot position for crafter core preview", e);
        }
    }

    /**
     * Resolves one of {@link Slot}'s coordinate fields in every mapping environment this mod is built for.
     * <p>
     * Development runs and loaders that keep the official Mojang names at runtime expose them as
     * {@code x}/{@code y}. A production Forge 1.20.1 jar is remapped to SRG names instead, where the very same
     * fields are called {@code f_40220_}/{@code f_40221_}. Only looking up the official name throws
     * {@link NoSuchFieldException} there, and because this happens from a static initializer the resulting
     * {@link ExceptionInInitializerError} aborts the whole screen, leaving the crafter core pattern provider
     * impossible to open. Both spellings are therefore tried.
     *
     * @param officialName the official Mojang name of the field, used in development and on loaders that run
     *                     with official mappings
     * @param srgName      the SRG name the field is remapped to in a production Forge jar
     * @return the resolved, accessible field
     */
    private static Field getSlotField(String officialName, String srgName) {
        for (String candidate : new String[] { officialName, srgName }) {
            try {
                Field field = Slot.class.getDeclaredField(candidate);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // Try the next known name for this field.
            }
        }
        throw new IllegalStateException("Missing Slot field: " + officialName + " (aka " + srgName + ")");
    }
}
