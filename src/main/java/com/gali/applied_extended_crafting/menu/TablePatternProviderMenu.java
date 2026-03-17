package com.gali.applied_extended_crafting.menu;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class TablePatternProviderMenu extends ProtectedPatternProviderMenu {
    public static final MenuType<TablePatternProviderMenu> TYPE = MenuTypeBuilder
            .create(TablePatternProviderMenu::new, PatternProviderLogicHost.class)
            .withMenuTitle(TablePatternProviderMenu::getMenuTitle)
            .build("table_pattern_provider");

    public TablePatternProviderMenu(MenuType<? extends TablePatternProviderMenu> menuType, int id,
                                    Inventory playerInventory, PatternProviderLogicHost host) {
        super(menuType, id, playerInventory, host);
        this.sealProtectedSlotPlan();
    }

    private static Component getMenuTitle(PatternProviderLogicHost host) {
        var blockEntity = host.getBlockEntity();
        if (blockEntity == null) {
            return Component.empty();
        }

        return new ItemStack(blockEntity.getBlockState().getBlock()).getHoverName();
    }
}
