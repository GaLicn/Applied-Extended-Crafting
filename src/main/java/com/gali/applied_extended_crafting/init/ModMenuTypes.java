package com.gali.applied_extended_crafting.init;

import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.menu.EnderCrafterPatternProviderMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(
            Registries.MENU,
            Applied_extended_crafting.MODID
    );

    public static final DeferredHolder<MenuType<?>, MenuType<EnderCrafterPatternProviderMenu>> TABLE_PATTERN_PROVIDER =
            MENU_TYPES.register("table_pattern_provider", () -> EnderCrafterPatternProviderMenu.TYPE);

    private ModMenuTypes() {
    }

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
