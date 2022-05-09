package com.lothrazar.storagenetwork.forge;

import com.lothrazar.storagenetwork.StorageNetwork;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(StorageNetwork.MODID)
public class StorageNetworkForge {
    public StorageNetworkForge() {
        EventBuses.registerModEventBus(StorageNetwork.MODID, FMLJavaModLoadingContext.get().getModEventBus());
        new StorageNetwork();
    }
}
