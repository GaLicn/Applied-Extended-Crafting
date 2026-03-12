package com.gali.applied_extended_crafting.recipe;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.init.ModRecipeTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableRecipeMatcher implements IRecipeMatcher<ITableRecipe> {
    private final int tier;
    private RecipeManager cachedRecipeManager;
    private List<RecipeHolder<ITableRecipe>> cachedRecipes = List.of();

    public TableRecipeMatcher(int tier) {
        this.tier = tier;
    }

    @Override
    public RecipeType<ITableRecipe> getRecipeType() {
        return ModRecipeTypes.TABLE.get();
    }

    @Override
    public boolean matchesRecipe(Map<AEKey, Long> patternInputs, Map<AEKey, Long> patternOutputs, Level level) {
        for (var holder : this.getRecipes(level)) {
            var recipe = holder.value();
            if (recipe.hasRequiredTier()) {
                if (recipe.getTier() != this.tier) {
                    continue;
                }
            } else if (recipe.getTier() > this.tier) {
                continue;
            }

            if (!this.matchesOutput(patternOutputs, recipe, level)) {
                continue;
            }

            if (this.matchesInputs(patternInputs, recipe.getIngredients())) {
                return true;
            }
        }

        return false;
    }

    private List<RecipeHolder<ITableRecipe>> getRecipes(Level level) {
        var recipeManager = level.getRecipeManager();
        if (this.cachedRecipeManager != recipeManager) {
            this.cachedRecipeManager = recipeManager;
            this.cachedRecipes = recipeManager.getAllRecipesFor(this.getRecipeType());
        }

        return this.cachedRecipes;
    }

    private boolean matchesOutput(Map<AEKey, Long> patternOutputs, ITableRecipe recipe, Level level) {
        var recipeOutput = recipe.getResultItem(level.registryAccess());
        if (recipeOutput.isEmpty()) {
            return patternOutputs.isEmpty();
        }

        if (patternOutputs.size() != 1) {
            return false;
        }

        var entry = patternOutputs.entrySet().iterator().next();
        if (!(entry.getKey() instanceof AEItemKey itemKey)) {
            return false;
        }

        return entry.getValue() == recipeOutput.getCount() && itemKey.matches(recipeOutput);
    }

    private boolean matchesInputs(Map<AEKey, Long> patternInputs, List<Ingredient> recipeIngredients) {
        var requiredIngredients = recipeIngredients.stream()
                .filter(ingredient -> !ingredient.isEmpty())
                .toList();
        var availableInputs = this.expandInputs(patternInputs);

        if (availableInputs == null || availableInputs.size() != requiredIngredients.size()) {
            return false;
        }

        return this.matchIngredients(requiredIngredients, availableInputs, new boolean[availableInputs.size()], 0);
    }

    private List<AEItemKey> expandInputs(Map<AEKey, Long> patternInputs) {
        var expanded = new ArrayList<AEItemKey>();

        for (var entry : patternInputs.entrySet()) {
            if (!(entry.getKey() instanceof AEItemKey itemKey)) {
                return null;
            }

            long amount = entry.getValue();
            if (amount <= 0) {
                continue;
            }

            for (long index = 0; index < amount; index++) {
                expanded.add(itemKey);
            }
        }

        return expanded;
    }

    private boolean matchIngredients(List<Ingredient> ingredients, List<AEItemKey> inputs, boolean[] used, int ingredientIndex) {
        if (ingredientIndex >= ingredients.size()) {
            return true;
        }

        var ingredient = ingredients.get(ingredientIndex);
        for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
            if (used[inputIndex]) {
                continue;
            }

            ItemStack stack = inputs.get(inputIndex).toStack(1);
            if (!ingredient.test(stack)) {
                continue;
            }

            used[inputIndex] = true;
            if (this.matchIngredients(ingredients, inputs, used, ingredientIndex + 1)) {
                return true;
            }
            used[inputIndex] = false;
        }

        return false;
    }
}
