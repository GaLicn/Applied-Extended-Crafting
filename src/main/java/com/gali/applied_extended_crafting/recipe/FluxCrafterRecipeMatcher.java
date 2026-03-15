package com.gali.applied_extended_crafting.recipe;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.blakebr0.extendedcrafting.api.crafting.IFluxCrafterRecipe;
import com.blakebr0.extendedcrafting.init.ModRecipeTypes;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FluxCrafterRecipeMatcher implements IRecipeMatcher<IFluxCrafterRecipe> {
    private RecipeManager cachedRecipeManager;
    private List<IFluxCrafterRecipe> cachedRecipes = List.of();

    @Override
    public RecipeType<IFluxCrafterRecipe> getRecipeType() {
        return ModRecipeTypes.FLUX_CRAFTER.get();
    }

    @Override
    public boolean matchesRecipe(Map<AEKey, Long> patternInputs, Map<AEKey, Long> patternOutputs, Level level) {
        return this.findRecipe(patternInputs, patternOutputs, level) != null;
    }

    @Nullable
    public IFluxCrafterRecipe findRecipe(Map<AEKey, Long> patternInputs, Map<AEKey, Long> patternOutputs, Level level) {
        for (var recipe : this.getRecipes(level)) {
            if (!this.matchesOutput(patternOutputs, recipe, level)) {
                continue;
            }

            if (this.matchesInputs(patternInputs, recipe.getIngredients())) {
                return recipe;
            }
        }

        return null;
    }

    private List<IFluxCrafterRecipe> getRecipes(Level level) {
        var recipeManager = level.getRecipeManager();
        if (this.cachedRecipeManager != recipeManager) {
            this.cachedRecipeManager = recipeManager;
            this.cachedRecipes = recipeManager.getAllRecipesFor(this.getRecipeType());
        }

        return this.cachedRecipes;
    }

    private boolean matchesOutput(Map<AEKey, Long> patternOutputs, IFluxCrafterRecipe recipe, Level level) {
        var recipeOutput = recipe.getResultItem(level.registryAccess());
        if (recipeOutput.isEmpty()) {
            return patternOutputs.isEmpty();
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
        var availableInputs = this.extractItemInputs(patternInputs);

        if (availableInputs == null) {
            return false;
        }

        long totalInputs = availableInputs.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        if (totalInputs != requiredIngredients.size()) {
            return false;
        }

        var groupedMatch = this.tryMatchIngredientsByGroups(requiredIngredients, availableInputs);
        if (groupedMatch != null) {
            return groupedMatch;
        }

        var expandedInputs = this.expandInputs(availableInputs);
        return this.matchIngredients(requiredIngredients, expandedInputs);
    }

    private Map<AEItemKey, Long> extractItemInputs(Map<AEKey, Long> patternInputs) {
        var itemInputs = new HashMap<AEItemKey, Long>();

        for (var entry : patternInputs.entrySet()) {
            if (!(entry.getKey() instanceof AEItemKey itemKey)) {
                return null;
            }

            long amount = entry.getValue();
            if (amount <= 0) {
                continue;
            }

            itemInputs.merge(itemKey, amount, Long::sum);
        }

        return itemInputs;
    }

    private List<AEItemKey> expandInputs(Map<AEItemKey, Long> patternInputs) {
        var expanded = new ArrayList<AEItemKey>();

        for (var entry : patternInputs.entrySet()) {
            long amount = entry.getValue();
            if (amount <= 0) {
                continue;
            }

            for (long index = 0; index < amount; index++) {
                expanded.add(entry.getKey());
            }
        }

        return expanded;
    }

    private Boolean tryMatchIngredientsByGroups(List<Ingredient> ingredients, Map<AEItemKey, Long> inputCounts) {
        var inputKeys = new ArrayList<>(inputCounts.keySet());
        var groups = new ArrayList<IngredientGroup>();

        for (var ingredient : ingredients) {
            var candidateKeys = this.findCandidateKeys(ingredient, inputKeys);
            if (candidateKeys.isEmpty()) {
                return false;
            }

            var existingGroup = this.findGroupByCandidates(groups, candidateKeys);
            if (existingGroup != null) {
                existingGroup.remaining++;
                continue;
            }

            if (this.hasPartialOverlap(groups, candidateKeys)) {
                return null;
            }

            groups.add(new IngredientGroup(candidateKeys));
        }

        for (var entry : inputCounts.entrySet()) {
            var group = this.findGroupContainingKey(groups, entry.getKey());
            if (group == null) {
                return false;
            }

            group.remaining -= entry.getValue();
            if (group.remaining < 0) {
                return false;
            }
        }

        for (var group : groups) {
            if (group.remaining != 0) {
                return false;
            }
        }

        return true;
    }

    private Set<AEItemKey> findCandidateKeys(Ingredient ingredient, List<AEItemKey> inputKeys) {
        var candidateKeys = new HashSet<AEItemKey>();

        for (var inputKey : inputKeys) {
            if (ingredient.test(inputKey.toStack(1))) {
                candidateKeys.add(inputKey);
            }
        }

        return Set.copyOf(candidateKeys);
    }

    private IngredientGroup findGroupByCandidates(List<IngredientGroup> groups, Set<AEItemKey> candidateKeys) {
        for (var group : groups) {
            if (group.candidateKeys.equals(candidateKeys)) {
                return group;
            }
        }

        return null;
    }

    private boolean hasPartialOverlap(List<IngredientGroup> groups, Set<AEItemKey> candidateKeys) {
        for (var group : groups) {
            boolean overlaps = false;
            for (var inputKey : candidateKeys) {
                if (group.candidateKeys.contains(inputKey)) {
                    overlaps = true;
                    break;
                }
            }

            if (overlaps && !group.candidateKeys.equals(candidateKeys)) {
                return true;
            }
        }

        return false;
    }

    private IngredientGroup findGroupContainingKey(List<IngredientGroup> groups, AEItemKey inputKey) {
        for (var group : groups) {
            if (group.candidateKeys.contains(inputKey)) {
                return group;
            }
        }

        return null;
    }

    private boolean matchIngredients(List<Ingredient> ingredients, List<AEItemKey> inputs) {
        var graph = this.buildMatchGraph(ingredients, inputs);
        if (graph == null) {
            return false;
        }

        var ingredientOrder = new ArrayList<Integer>();
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

    private static final class IngredientGroup {
        private final Set<AEItemKey> candidateKeys;
        private long remaining = 1;

        private IngredientGroup(Set<AEItemKey> candidateKeys) {
            this.candidateKeys = candidateKeys;
        }
    }
}
