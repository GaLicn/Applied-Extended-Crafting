package com.gali.applied_extended_crafting;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Applied_extended_crafting.MODID)
public class Applied_extended_crafting {
    public static final String MODID = "applied_extended_crafting";

    public Applied_extended_crafting(IEventBus modEventBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            new ClientModEvents(modEventBus);
        }
    }
}
