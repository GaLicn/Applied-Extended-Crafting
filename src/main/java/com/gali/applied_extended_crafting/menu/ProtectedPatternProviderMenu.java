package com.gali.applied_extended_crafting.menu;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public abstract class ProtectedPatternProviderMenu extends PatternProviderMenu {
    private static final int PHASE_BASE = 0;
    private static final int PHASE_CUSTOM = 1;
    private static final int PHASE_LOCKED = 2;

    private static final int BASE_PLAYER_HOTBAR_SLOTS = Inventory.getSelectionSize();
    private static final int BASE_PLAYER_INVENTORY_SLOTS = 27;
    private static final int BASE_PATTERN_SLOTS = 9;
    private static final int BASE_RETURN_SLOTS = PatternProviderReturnInventory.NUMBER_OF_SLOTS;

    private int aec$slotPlanPhase;
    private int aec$baseHotbarProgress;
    private int aec$baseInventoryProgress;
    private int aec$basePatternProgress;
    private int aec$baseStorageProgress;
    private int aec$customPlanIndex;
    private int aec$customPlanProgress;
    private boolean aec$allowPlainSlotAddition;

    protected ProtectedPatternProviderMenu(
            MenuType<? extends PatternProviderMenu> menuType,
            int id,
            Inventory playerInventory,
            PatternProviderLogicHost host) {
        super(menuType, id, playerInventory, host);
        this.aec$slotPlanPhase = PHASE_CUSTOM;
    }

    protected final void sealProtectedSlotPlan() {
        this.aec$slotPlanPhase = PHASE_LOCKED;
    }

    @Override
    protected Slot addSlot(Slot slot, SlotSemantic semantic) {
        if (!this.aec$canAcceptSemantic(semantic)) {
            return slot;
        }

        this.aec$allowPlainSlotAddition = true;
        try {
            slot = super.addSlot(slot, semantic);
        } finally {
            this.aec$allowPlainSlotAddition = false;
        }

        this.aec$markAcceptedSemantic();
        return slot;
    }

    @Override
    protected Slot addSlot(Slot newSlot) {
        if (!this.aec$allowPlainSlotAddition) {
            return newSlot;
        }

        return super.addSlot(newSlot);
    }

    private boolean aec$canAcceptSemantic(SlotSemantic semantic) {
        return switch (this.aec$slotPlanPhase) {
            case PHASE_BASE -> this.aec$matchesBasePlan(semantic);
            case PHASE_CUSTOM -> this.aec$matchesCustomPlan(semantic);
            default -> false;
        };
    }

    private boolean aec$matchesBasePlan(SlotSemantic semantic) {
        if (this.aec$baseHotbarProgress < BASE_PLAYER_HOTBAR_SLOTS) {
            return semantic == SlotSemantics.PLAYER_HOTBAR;
        }

        if (this.aec$baseInventoryProgress < BASE_PLAYER_INVENTORY_SLOTS) {
            return semantic == SlotSemantics.PLAYER_INVENTORY;
        }

        if (this.aec$basePatternProgress < BASE_PATTERN_SLOTS) {
            return semantic == SlotSemantics.ENCODED_PATTERN;
        }

        if (this.aec$baseStorageProgress < BASE_RETURN_SLOTS) {
            return semantic == SlotSemantics.STORAGE;
        }

        return false;
    }

    private boolean aec$matchesCustomPlan(SlotSemantic semantic) {
        var plan = this.aec$getCustomPlan();
        while (this.aec$customPlanIndex < plan.size()) {
            var step = plan.get(this.aec$customPlanIndex);
            if (this.aec$customPlanProgress < step.count()) {
                return semantic == step.semantic();
            }

            this.aec$customPlanIndex++;
            this.aec$customPlanProgress = 0;
        }

        return false;
    }

    private void aec$markAcceptedSemantic() {
        if (this.aec$slotPlanPhase == PHASE_BASE) {
            if (this.aec$baseHotbarProgress < BASE_PLAYER_HOTBAR_SLOTS) {
                this.aec$baseHotbarProgress++;
                return;
            }

            if (this.aec$baseInventoryProgress < BASE_PLAYER_INVENTORY_SLOTS) {
                this.aec$baseInventoryProgress++;
                return;
            }

            if (this.aec$basePatternProgress < BASE_PATTERN_SLOTS) {
                this.aec$basePatternProgress++;
                return;
            }

            if (this.aec$baseStorageProgress < BASE_RETURN_SLOTS) {
                this.aec$baseStorageProgress++;
            }
            return;
        }

        if (this.aec$slotPlanPhase == PHASE_CUSTOM) {
            this.aec$customPlanProgress++;
        }
    }

    private List<SlotPlanStep> aec$getCustomPlan() {
        var menuClass = this.getClass();
        if (menuClass == EnderCrafterPatternProviderMenu.class) {
            return List.of(
                    new SlotPlanStep(EnderCrafterPatternProviderMenu.PREVIEW_CRAFTING_GRID, 9),
                    new SlotPlanStep(EnderCrafterPatternProviderMenu.PREVIEW_RESULT, 1)
            );
        }
        if (menuClass == FluxCrafterPatternProviderMenu.class) {
            return List.of(
                    new SlotPlanStep(FluxCrafterPatternProviderMenu.PREVIEW_CRAFTING_GRID, 9),
                    new SlotPlanStep(FluxCrafterPatternProviderMenu.PREVIEW_RESULT, 1)
            );
        }
        if (menuClass == CrafterCorePatternProviderMenu.class) {
            return List.of(
                    new SlotPlanStep(CrafterCorePatternProviderMenu.PREVIEW_CRAFTING_GRID, 49),
                    new SlotPlanStep(CrafterCorePatternProviderMenu.PREVIEW_RESULT, 1),
                    new SlotPlanStep(CrafterCorePatternProviderMenu.PEDESTAL, 1),
                    new SlotPlanStep(SlotSemantics.UPGRADE, 6)
            );
        }
        return List.of();
    }

    private record SlotPlanStep(SlotSemantic semantic, int count) {
    }
}
