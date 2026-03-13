package com.gali.applied_extended_crafting.init;

import com.gali.applied_extended_crafting.Applied_extended_crafting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            Applied_extended_crafting.MODID
    );

    public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Applied_extended_crafting.MODID))
                    .icon(() -> new ItemStack(ModItems.TABLE_BASIC_PATTERN_PROVIDER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.TABLE_BASIC_PATTERN_PROVIDER.get());
                        output.accept(ModItems.TABLE_ADVANCED_PATTERN_PROVIDER.get());
                        output.accept(ModItems.TABLE_ELITE_PATTERN_PROVIDER.get());
                        output.accept(ModItems.TABLE_ULTIMATE_PATTERN_PROVIDER.get());
                    })
                    .build()
    );

    private ModCreativeModeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
