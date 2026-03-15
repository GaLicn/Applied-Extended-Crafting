package com.gali.applied_extended_crafting.blockentity;

import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.util.inv.AppEngInternalInventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CrafterCorePatternProviderMenuHost {
    AppEngInternalInventory getPedestalInventory();

    IUpgradeInventory getUpgradeInventory();

    boolean isProcessing();

    int getProcessingProgressScaled(int width);

    boolean hasProcessingPreview();

    @Nullable
    GenericStack getDisplayedCenterInput();

    List<GenericStack> getDisplayedPedestalInputs();

    @Nullable
    GenericStack getDisplayedResult();

    int getPedestalCount();
}
