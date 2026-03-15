package com.gali.applied_extended_crafting.init;

import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.menu.CrafterCorePatternProviderMenu;
import com.gali.applied_extended_crafting.menu.EnderCrafterPatternProviderMenu;
import com.gali.applied_extended_crafting.menu.FluxCrafterPatternProviderMenu;
import com.gali.applied_extended_crafting.menu.TablePatternProviderMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(
            ForgeRegistries.MENU_TYPES,
            Applied_extended_crafting.MODID
    );

    public static final RegistryObject<MenuType<TablePatternProviderMenu>> TABLE_PATTERN_PROVIDER =
            MENU_TYPES.register("table_pattern_provider", () -> TablePatternProviderMenu.TYPE);

    public static final RegistryObject<MenuType<EnderCrafterPatternProviderMenu>> ENDER_CRAFTER_PATTERN_PROVIDER =
            MENU_TYPES.register("ender_crafter_pattern_provider", () -> EnderCrafterPatternProviderMenu.TYPE);

    public static final RegistryObject<MenuType<CrafterCorePatternProviderMenu>> CRAFTER_CORE_PATTERN_PROVIDER =
            MENU_TYPES.register("crafter_core_pattern_provider", () -> CrafterCorePatternProviderMenu.TYPE);

    public static final RegistryObject<MenuType<FluxCrafterPatternProviderMenu>> FLUX_CRAFTER_PATTERN_PROVIDER =
            MENU_TYPES.register("flux_crafter_pattern_provider", () -> FluxCrafterPatternProviderMenu.TYPE);

    private ModMenuTypes() {
    }

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
