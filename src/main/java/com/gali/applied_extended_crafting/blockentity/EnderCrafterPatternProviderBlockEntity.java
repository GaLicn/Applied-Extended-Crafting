package com.gali.applied_extended_crafting.blockentity;

import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import com.gali.applied_extended_crafting.menu.EnderCrafterPatternProviderMenu;
import com.gali.applied_extended_crafting.recipe.EnderCrafterRecipeMatcher;
import com.gali.applied_extended_crafting.recipe.IRecipeMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class EnderCrafterPatternProviderBlockEntity extends AbstractPatternProvider {
    private final EnderCrafterRecipeMatcher recipeMatcher = new EnderCrafterRecipeMatcher();

    public EnderCrafterPatternProviderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENDER_CRAFTER_PATTERN_PROVIDER.get(), pos, state);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(EnderCrafterPatternProviderMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(EnderCrafterPatternProviderMenu.TYPE, player, subMenu.getLocator());
    }

    @Override
    protected IRecipeMatcher<?> getRecipeMatcher() {
        return this.recipeMatcher;
    }

    @Override
    protected int getPatternSlotCount() {
        return 9;
    }
}
