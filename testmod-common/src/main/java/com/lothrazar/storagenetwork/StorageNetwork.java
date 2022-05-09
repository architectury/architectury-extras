/*
 * This file is part of architectury, licensed under the MIT License.
 * Copyright (c) 2017 - Original Author is MrRiegel as seen here https://github.com/MrRiegel
 * - Secondary author  Sam Bassett aka Lothrazar https://minecraft.curseforge.com/members/Lothrazar
 * Copyright (c) 2020, 2021, 2022 architectury
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.lothrazar.storagenetwork;

import com.lothrazar.storagenetwork.block.cable.export.GuiCableExportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.GuiCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.linkfilter.GuiCableFilter;
import com.lothrazar.storagenetwork.block.collection.GuiCollectionFilter;
import com.lothrazar.storagenetwork.block.inventory.GuiNetworkInventory;
import com.lothrazar.storagenetwork.block.request.GuiNetworkTable;
import com.lothrazar.storagenetwork.item.remote.GuiNetworkCraftingRemote;
import com.lothrazar.storagenetwork.item.remote.GuiNetworkRemote;
import com.lothrazar.storagenetwork.registry.*;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageNetwork {
    
    public static final String MODID = "storagenetwork";
    public static final Logger LOGGER = LogManager.getLogger();
    public static ConfigRegistry CONFIG;
    
    public StorageNetwork() {
        LifecycleEvent.SETUP.register(StorageNetwork::setup);
        ClientLifecycleEvent.CLIENT_SETUP.register(this::setupClient);
        SsnRegistry.RegistryEvents.init();
        SsnEvents.init();
    }
    
    private static void setup() {
        PacketRegistry.init();
        CONFIG = new ConfigRegistry(Platform.getConfigFolder().resolve(MODID + ".toml"));
        // InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("charm").size(2).build());
    }
    
    @Environment(EnvType.CLIENT)
    private void setupClient(Minecraft mc) {
        MenuRegistry.registerScreenFactory(SsnRegistry.REQUESTCONTAINER.get(), GuiNetworkTable::new);
        MenuRegistry.registerScreenFactory(SsnRegistry.FILTERCONTAINER.get(), GuiCableFilter::new);
        MenuRegistry.registerScreenFactory(SsnRegistry.FILTERIMPORTCONTAINER.get(), GuiCableImportFilter::new);
        MenuRegistry.registerScreenFactory(SsnRegistry.FILTEREXPORTCONTAINER.get(), GuiCableExportFilter::new);
        MenuRegistry.registerScreenFactory(SsnRegistry.REMOTE.get(), GuiNetworkRemote::new);
        MenuRegistry.registerScreenFactory(SsnRegistry.CRAFTINGREMOTE.get(), GuiNetworkCraftingRemote::new);
        MenuRegistry.registerScreenFactory(SsnRegistry.INVENTORYCONTAINER.get(), GuiNetworkInventory::new);
        MenuRegistry.registerScreenFactory(SsnRegistry.COLLECTORCTR.get(), GuiCollectionFilter::new);
        KeyMappingRegistry.register(ClientEventRegistry.INVENTORY_KEY);
    }
    
    public static void log(String s) {
        if (CONFIG.logspam()) {
            LOGGER.info(s);
        }
    }
}
