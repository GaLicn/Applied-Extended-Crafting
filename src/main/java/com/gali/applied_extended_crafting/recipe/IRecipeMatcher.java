package com.gali.applied_extended_crafting.recipe;

import appeng.api.stacks.AEKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Map;

public interface IRecipeMatcher<T extends Recipe<?>> {
    RecipeType<T> getRecipeType();

    boolean matchesRecipe(Map<AEKey, Long> patternInputs, Map<AEKey, Long> patternOutputs, Level level);

    default Component getDisplayName() {
        return Component.literal(this.getClass().getSimpleName());
    }
}
