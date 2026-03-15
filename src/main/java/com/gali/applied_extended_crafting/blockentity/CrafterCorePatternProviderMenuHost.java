package com.gali.applied_extended_crafting.blockentity;

import appeng.api.stacks.GenericStack;
import appeng.util.inv.AppEngInternalInventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CrafterCorePatternProviderMenuHost {
    AppEngInternalInventory getPedestalInventory();

    boolean isProcessing();

    int getProcessingProgressScaled(int width);

    boolean hasProcessingPreview();

    @Nullable
    GenericStack getDisplayedCenterInput();

    List<GenericStack> getDisplayedPedestalInputs();

    @Nullable
    GenericStack getDisplayedResult();

    @Nullable
    GenericStack getDisplayedPedestalStack();

    int getPedestalCount();
}
