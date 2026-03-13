package com.gali.applied_extended_crafting.client.gui;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import com.gali.applied_extended_crafting.menu.TablePatternProviderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TablePatternProviderScreen extends PatternProviderScreen<TablePatternProviderMenu> {
    public TablePatternProviderScreen(TablePatternProviderMenu menu, Inventory playerInventory, Component title,
                                      ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.setTextContent(TEXT_ID_DIALOG_TITLE, title);
    }
}
