package com.gali.applied_extended_crafting.blockentity;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface FluxCrafterPatternProviderMenuHost {
    boolean isProcessing();

    int getProcessingProgressScaled(int width);

    boolean hasProcessingPreview();

    List<GenericStack> getDisplayedInputs();

    @Nullable
    GenericStack getDisplayedResult();

    int getMenuEnergyStored();

    int getMenuEnergyCapacity();
}
