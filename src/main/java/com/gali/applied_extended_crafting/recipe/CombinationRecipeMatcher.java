package com.gali.applied_extended_crafting.recipe;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.blakebr0.extendedcrafting.api.crafting.ICombinationRecipe;
import com.blakebr0.extendedcrafting.init.ModRecipeTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CombinationRecipeMatcher implements IRecipeMatcher<ICombinationRecipe> {
    private RecipeManager cachedRecipeManager;
    private List<ICombinationRecipe> cachedRecipes = List.of();

    @Override
    public RecipeType<ICombinationRecipe> getRecipeType() {
        return ModRecipeTypes.COMBINATION.get();
    }

    @Override
    public boolean matchesRecipe(Map<AEKey, Long> patternInputs, Map<AEKey, Long> patternOutputs, Level level) {
        return this.findRecipe(patternInputs, patternOutputs, level) != null;
    }

    @Override
    @Nullable
    public ItemStack findRecipeOutput(Map<AEKey, Long> patternInputs, Map<AEKey, Long> patternOutputs, Level level) {
        var recipe = this.findRecipe(patternInputs, patternOutputs, level);
        return recipe == null ? null : recipe.getResultItem(level.registryAccess());
    }

    @Nullable
    public ICombinationRecipe findRecipe(Map<AEKey, Long> patternInputs, Map<AEKey, Long> patternOutputs,
                                         Level level) {
        for (var recipe : this.getRecipes(level)) {
            if (!this.matchesOutput(patternOutputs, recipe, level)) {
                continue;
            }

            if (this.matchesInputs(patternInputs, recipe)) {
                return recipe;
            }
        }

        return null;
    }

    private List<ICombinationRecipe> getRecipes(Level level) {
        var recipeManager = level.getRecipeManager();
        if (this.cachedRecipeManager != recipeManager) {
            this.cachedRecipeManager = recipeManager;
            this.cachedRecipes = recipeManager.getAllRecipesFor(this.getRecipeType());
        }

        return this.cachedRecipes;
    }

    private boolean matchesOutput(Map<AEKey, Long> patternOutputs, ICombinationRecipe recipe, Level level) {
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

        // 只比较物品类型和数量，配方输出不受样板 NBT 影响。
        return entry.getValue() == recipeOutput.getCount() && itemKey.getItem() == recipeOutput.getItem();
    }

    private boolean matchesInputs(Map<AEKey, Long> patternInputs, ICombinationRecipe recipe) {
        var expandedInputs = this.expandInputs(patternInputs);
        if (expandedInputs == null) {
            return false;
        }

        var centerIngredient = getCenterIngredient(recipe);
        if (centerIngredient.isEmpty()) {
            return false;
        }

        var pedestalIngredients = getPedestalIngredients(recipe);

        if (expandedInputs.size() != pedestalIngredients.size() + 1) {
            return false;
        }

        for (int i = 0; i < expandedInputs.size(); i++) {
            var centerCandidate = expandedInputs.get(i);
            if (!centerIngredient.test(centerCandidate.toStack(1))) {
                continue;
            }

            var remainingInputs = new ArrayList<>(expandedInputs);
            remainingInputs.remove(i);
            if (this.matchIngredients(pedestalIngredients, remainingInputs)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
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

            for (long i = 0; i < amount; i++) {
                expanded.add(itemKey);
            }
        }

        return expanded;
    }

    /**
     * 1.20.1 版 ExtendedCrafting 把中心输入放在配料表索引 0，其后才是基座输入
     * （见 CombinationRecipe.Serializer#fromJson 与 CombinationRecipe#matches）；
     * 该版本没有 getInput()/getCatalyst() 之类的访问器。
     */
    public static Ingredient getCenterIngredient(ICombinationRecipe recipe) {
        var ingredients = recipe.getIngredients();
        return ingredients.isEmpty() ? Ingredient.EMPTY : ingredients.get(0);
    }

    /**
     * 基座输入：跳过索引 0 的中心输入，并忽略空配料。
     */
    public static List<Ingredient> getPedestalIngredients(ICombinationRecipe recipe) {
        var ingredients = recipe.getIngredients();
        if (ingredients.size() <= 1) {
            return List.of();
        }

        return ingredients.subList(1, ingredients.size()).stream()
                .filter(ingredient -> !ingredient.isEmpty())
                .toList();
    }

    private boolean matchIngredients(List<Ingredient> ingredients, List<AEItemKey> inputs) {
        if (ingredients.size() != inputs.size()) {
            return false;
        }

        var graph = this.buildMatchGraph(ingredients, inputs);
        if (graph == null) {
            return false;
        }

        var ingredientOrder = new ArrayList<Integer>(ingredients.size());
        for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
            ingredientOrder.add(ingredientIndex);
        }
        ingredientOrder.sort((left, right) -> Integer.compare(graph.get(left).size(), graph.get(right).size()));

        int[] inputMatches = new int[inputs.size()];
        Arrays.fill(inputMatches, -1);

        for (var ingredientIndex : ingredientOrder) {
            boolean[] visitedInputs = new boolean[inputs.size()];
            if (!this.tryMatchIngredient(ingredientIndex, graph, inputMatches, visitedInputs)) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    private List<List<Integer>> buildMatchGraph(List<Ingredient> ingredients, List<AEItemKey> inputs) {
        var graph = new ArrayList<List<Integer>>(ingredients.size());

        for (var ingredient : ingredients) {
            var candidateInputs = new ArrayList<Integer>();

            for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
                if (ingredient.test(inputs.get(inputIndex).toStack(1))) {
                    candidateInputs.add(inputIndex);
                }
            }

            if (candidateInputs.isEmpty()) {
                return null;
            }

            graph.add(candidateInputs);
        }

        return graph;
    }

    private boolean tryMatchIngredient(int ingredientIndex, List<List<Integer>> graph, int[] inputMatches,
                                       boolean[] visitedInputs) {
        for (var inputIndex : graph.get(ingredientIndex)) {
            if (visitedInputs[inputIndex]) {
                continue;
            }

            visitedInputs[inputIndex] = true;

            int matchedIngredient = inputMatches[inputIndex];
            if (matchedIngredient == -1
                    || this.tryMatchIngredient(matchedIngredient, graph, inputMatches, visitedInputs)) {
                inputMatches[inputIndex] = ingredientIndex;
                return true;
            }
        }

        return false;
    }
}
