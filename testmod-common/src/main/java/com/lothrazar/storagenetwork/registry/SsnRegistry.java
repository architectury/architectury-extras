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

package com.lothrazar.storagenetwork.registry;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.block.BaseBlock;
import com.lothrazar.storagenetwork.block.cable.BlockCable;
import com.lothrazar.storagenetwork.block.cable.TileCable;
import com.lothrazar.storagenetwork.block.cable.export.BlockCableExport;
import com.lothrazar.storagenetwork.block.cable.export.ContainerCableExportFilter;
import com.lothrazar.storagenetwork.block.cable.export.TileCableExport;
import com.lothrazar.storagenetwork.block.cable.input.BlockCableIO;
import com.lothrazar.storagenetwork.block.cable.input.TileCableIO;
import com.lothrazar.storagenetwork.block.cable.inputfilter.BlockCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.ContainerCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.TileCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.link.BlockCableLink;
import com.lothrazar.storagenetwork.block.cable.link.TileCableLink;
import com.lothrazar.storagenetwork.block.cable.linkfilter.BlockCableFilter;
import com.lothrazar.storagenetwork.block.cable.linkfilter.ContainerCableFilter;
import com.lothrazar.storagenetwork.block.cable.linkfilter.TileCableFilter;
import com.lothrazar.storagenetwork.block.collection.BlockCollection;
import com.lothrazar.storagenetwork.block.collection.ContainerCollectionFilter;
import com.lothrazar.storagenetwork.block.collection.TileCollection;
import com.lothrazar.storagenetwork.block.exchange.BlockExchange;
import com.lothrazar.storagenetwork.block.exchange.TileExchange;
import com.lothrazar.storagenetwork.block.inventory.BlockInventory;
import com.lothrazar.storagenetwork.block.inventory.ContainerNetworkInventory;
import com.lothrazar.storagenetwork.block.inventory.TileInventory;
import com.lothrazar.storagenetwork.block.main.BlockMain;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.block.request.BlockRequest;
import com.lothrazar.storagenetwork.block.request.ContainerNetworkCraftingTable;
import com.lothrazar.storagenetwork.block.request.TileRequest;
import com.lothrazar.storagenetwork.item.ItemBuilder;
import com.lothrazar.storagenetwork.item.ItemCollector;
import com.lothrazar.storagenetwork.item.ItemPicker;
import com.lothrazar.storagenetwork.item.ItemUpgrade;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkCraftingRemote;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkRemote;
import com.lothrazar.storagenetwork.item.remote.ItemStorageCraftingRemote;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SsnRegistry {
    
    public static final int UPGRADE_COUNT = 4;
    public static CreativeModeTab TAB = CreativeTabRegistry.create(new ResourceLocation(StorageNetwork.MODID, StorageNetwork.MODID), () ->
            new ItemStack(SsnRegistry.REQUEST.get()));
    
    private static <T, A extends T> Supplier<A> defer(ResourceKey<Registry<T>> registry, String string) {
        return () -> (A) Registries.get(StorageNetwork.MODID).get(registry).get(new ResourceLocation(string));
    }
    
    public static final List<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new ArrayList<>();
    
    public static Supplier<ItemBuilder> BUILDER_REMOTE = defer(Registry.ITEM_REGISTRY, StorageNetwork.MODID + ":builder_remote");
    public static Supplier<ItemStorageCraftingRemote> INVENTORY_REMOTE = defer(Registry.ITEM_REGISTRY, StorageNetwork.MODID + ":inventory_remote");
    public static Supplier<ItemStorageCraftingRemote> CRAFTING_REMOTE = defer(Registry.ITEM_REGISTRY, StorageNetwork.MODID + ":crafting_remote");
    public static Supplier<ItemCollector> COLLECTOR_REMOTE = defer(Registry.ITEM_REGISTRY, StorageNetwork.MODID + ":collector_remote");
    public static Supplier<ItemUpgrade> SLOW_UPGRADE = defer(Registry.ITEM_REGISTRY, StorageNetwork.MODID + ":slow_upgrade");
    public static Supplier<ItemUpgrade> SPEED_UPGRADE = defer(Registry.ITEM_REGISTRY, StorageNetwork.MODID + ":speed_upgrade");
    public static Supplier<ItemUpgrade> SINGLE_UPGRADE = defer(Registry.ITEM_REGISTRY, StorageNetwork.MODID + ":single_upgrade");
    public static Supplier<ItemUpgrade> STACK_UPGRADE = defer(Registry.ITEM_REGISTRY, StorageNetwork.MODID + ":stack_upgrade");
    public static Supplier<ItemUpgrade> STOCK_UPGRADE = defer(Registry.ITEM_REGISTRY, StorageNetwork.MODID + ":stock_upgrade");
    public static Supplier<ItemUpgrade> OP_UPGRADE = defer(Registry.ITEM_REGISTRY, StorageNetwork.MODID + ":operation_upgrade");
    public static Supplier<BlockEntityType<TileMain>> MAINTILEENTITY = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":master");
    public static Supplier<BlockMain> MAIN = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":master");
    public static Supplier<Block> INVENTORY = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":inventory");
    public static Supplier<BlockEntityType<TileInventory>> INVENTORYTILE = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":inventory");
    public static Supplier<MenuType<ContainerNetworkInventory>> INVENTORYCONTAINER = defer(Registry.MENU_REGISTRY, StorageNetwork.MODID + ":inventory");
    //request
    public static Supplier<BlockRequest> REQUEST = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":request");
    public static Supplier<BlockEntityType<TileRequest>> REQUESTTILE = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":request");
    public static Supplier<MenuType<ContainerNetworkCraftingTable>> REQUESTCONTAINER = defer(Registry.MENU_REGISTRY, StorageNetwork.MODID + ":request");
    public static Supplier<BlockCable> KABEL = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":kabel");
    public static Supplier<BlockEntityType<TileCable>> KABELTILE = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":kabel");
    public static Supplier<Block> EXCHANGE = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":exchange");
    public static Supplier<BlockEntityType<TileExchange>> EXCHANGETILE = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":exchange");
    public static Supplier<Block> COLLECTOR = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":collector");
    public static Supplier<BlockEntityType<TileCollection>> COLLECTORTILE = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":collector");
    public static Supplier<Block> STORAGEKABEL = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":storage_kabel");
    public static Supplier<BlockEntityType<TileCableLink>> STORAGEKABELTILE = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":storage_kabel");
    public static Supplier<Block> IMPORTKABEL = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":import_kabel");
    public static Supplier<BlockEntityType<TileCableIO>> IMPORTKABELTILE = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":import_kabel");
    public static Supplier<Block> FILTERKABEL = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":filter_kabel");
    public static Supplier<BlockEntityType<TileCableFilter>> FILTERKABELTILE = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":filter_kabel");
    public static Supplier<MenuType<ContainerCableFilter>> FILTERCONTAINER = defer(Registry.MENU_REGISTRY, StorageNetwork.MODID + ":filter_kabel");
    public static Supplier<Block> IMPORTFILTERKABEL = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":import_filter_kabel");
    public static Supplier<BlockEntityType<TileCableImportFilter>> FILTERIMPORTKABELTILE = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":import_filter_kabel");
    public static Supplier<MenuType<ContainerCableImportFilter>> FILTERIMPORTCONTAINER = defer(Registry.MENU_REGISTRY, StorageNetwork.MODID + ":import_filter_kabel");
    public static Supplier<Block> EXPORTKABEL = defer(Registry.BLOCK_REGISTRY, StorageNetwork.MODID + ":export_kabel");
    public static Supplier<BlockEntityType<TileCableExport>> EXPORTKABELTILE = defer(Registry.BLOCK_ENTITY_TYPE_REGISTRY, StorageNetwork.MODID + ":export_kabel");
    public static Supplier<MenuType<ContainerCableExportFilter>> FILTEREXPORTCONTAINER = defer(Registry.MENU_REGISTRY, StorageNetwork.MODID + ":export_kabel");
    public static Supplier<MenuType<ContainerNetworkRemote>> REMOTE = defer(Registry.MENU_REGISTRY, StorageNetwork.MODID + ":inventory_remote");
    public static Supplier<MenuType<ContainerNetworkCraftingRemote>> CRAFTINGREMOTE = defer(Registry.MENU_REGISTRY, StorageNetwork.MODID + ":crafting_remote");
    public static Supplier<MenuType<ContainerCollectionFilter>> COLLECTORCTR = defer(Registry.MENU_REGISTRY, StorageNetwork.MODID + ":collector");
    
    public static class RegistryEvents {
        public static void init() {
            Registries registries = Registries.get(StorageNetwork.MODID);
            registries.forRegistry(Registry.BLOCK_REGISTRY, registrar -> {
                onBlocksRegistry(baseBlock -> {
                    registrar.register(baseBlock.getRegistryName(), () -> baseBlock);
                });
            });
            onItemsRegistry(registries.get(Registry.ITEM_REGISTRY));
            onTileEntityRegistry(registries.get(Registry.BLOCK_ENTITY_TYPE_REGISTRY));
            onContainerRegistry(registries.get(Registry.MENU_REGISTRY));
        }
        
        public static void onBlocksRegistry(Consumer<BaseBlock> r) {
            r.accept(new BlockMain());
            r.accept(new BlockRequest());
            r.accept(new BlockCable("kabel"));
            r.accept(new BlockCableLink("storage_kabel"));
            r.accept(new BlockCableIO("import_kabel"));
            r.accept(new BlockCableImportFilter("import_filter_kabel"));
            r.accept(new BlockCableFilter("filter_kabel"));
            r.accept(new BlockCableExport("export_kabel"));
            r.accept(new BlockInventory("inventory"));
            r.accept(new BlockExchange());
            r.accept(new BlockCollection());
        }
        
        public static void onItemsRegistry(Registrar<Item> r) {
            Item.Properties properties = new Item.Properties().tab(SsnRegistry.TAB);
            r.register(new ResourceLocation(StorageNetwork.MODID, "inventory"), () -> new BlockItem(SsnRegistry.INVENTORY.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "master"), () -> new BlockItem(SsnRegistry.MAIN.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "request"), () -> new BlockItem(SsnRegistry.REQUEST.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "kabel"), () -> new BlockItem(SsnRegistry.KABEL.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "storage_kabel"), () -> new BlockItem(SsnRegistry.STORAGEKABEL.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "import_kabel"), () -> new BlockItem(SsnRegistry.IMPORTKABEL.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "import_filter_kabel"), () -> new BlockItem(SsnRegistry.IMPORTFILTERKABEL.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "filter_kabel"), () -> new BlockItem(SsnRegistry.FILTERKABEL.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "export_kabel"), () -> new BlockItem(SsnRegistry.EXPORTKABEL.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "exchange"), () -> new BlockItem(SsnRegistry.EXCHANGE.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "collector"), () -> new BlockItem(SsnRegistry.COLLECTOR.get(), properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "stack_upgrade"), () -> new ItemUpgrade(properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "speed_upgrade"), () -> new ItemUpgrade(properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "slow_upgrade"), () -> new ItemUpgrade(properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "stock_upgrade"), () -> new ItemUpgrade(properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "operation_upgrade"), () -> new ItemUpgrade(properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "single_upgrade"), () -> new ItemUpgrade(properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "inventory_remote"), () -> new ItemStorageCraftingRemote(properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "crafting_remote"), () -> new ItemStorageCraftingRemote(properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "picker_remote"), () -> new ItemPicker(properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "collector_remote"), () -> new ItemCollector(properties));
            r.register(new ResourceLocation(StorageNetwork.MODID, "builder_remote"), () -> new ItemBuilder(properties));
        }
        
        public static void onTileEntityRegistry(Registrar<BlockEntityType<?>> registrar) {
            BiConsumer<ResourceLocation, Supplier<BlockEntityType<?>>> r = (resourceLocation, builderSupplier) -> {
                registrar.register(resourceLocation, () -> {
                    BlockEntityType<?> type = builderSupplier.get();
                    SsnRegistry.BLOCK_ENTITY_TYPES.add(type);
                    return type;
                });
            };
            r.accept(new ResourceLocation(StorageNetwork.MODID, "master"), () -> BlockEntityType.Builder.of(TileMain::new, SsnRegistry.MAIN.get()).build(null));
            r.accept(new ResourceLocation(StorageNetwork.MODID, "inventory"), () -> BlockEntityType.Builder.of(TileInventory::new, SsnRegistry.INVENTORY.get()).build(null));
            r.accept(new ResourceLocation(StorageNetwork.MODID, "request"), () -> BlockEntityType.Builder.of(TileRequest::new, SsnRegistry.REQUEST.get()).build(null));
            r.accept(new ResourceLocation(StorageNetwork.MODID, "kabel"), () -> BlockEntityType.Builder.of(TileCable::new, SsnRegistry.KABEL.get()).build(null));
            r.accept(new ResourceLocation(StorageNetwork.MODID, "storage_kabel"), () -> BlockEntityType.Builder.of(TileCableLink::new, SsnRegistry.STORAGEKABEL.get()).build(null));
            r.accept(new ResourceLocation(StorageNetwork.MODID, "import_kabel"), () -> BlockEntityType.Builder.of(TileCableIO::new, SsnRegistry.IMPORTKABEL.get()).build(null));
            r.accept(new ResourceLocation(StorageNetwork.MODID, "import_filter_kabel"), () -> BlockEntityType.Builder.of(TileCableImportFilter::new, SsnRegistry.IMPORTFILTERKABEL.get()).build(null));
            r.accept(new ResourceLocation(StorageNetwork.MODID, "filter_kabel"), () -> BlockEntityType.Builder.of(TileCableFilter::new, SsnRegistry.FILTERKABEL.get()).build(null));
            r.accept(new ResourceLocation(StorageNetwork.MODID, "export_kabel"), () -> BlockEntityType.Builder.of(TileCableExport::new, SsnRegistry.EXPORTKABEL.get()).build(null));
            r.accept(new ResourceLocation(StorageNetwork.MODID, "exchange"), () -> BlockEntityType.Builder.of(TileExchange::new, SsnRegistry.EXCHANGE.get()).build(null));
            r.accept(new ResourceLocation(StorageNetwork.MODID, "collector"), () -> BlockEntityType.Builder.of(TileCollection::new, SsnRegistry.COLLECTOR.get()).build(null));
        }
        
        public static void onContainerRegistry(Registrar<MenuType<?>> r) {
            r.register(new ResourceLocation(StorageNetwork.MODID, "request"), () -> MenuRegistry.ofExtended((windowId, inv, data) -> {
                return new ContainerNetworkCraftingTable(windowId, inv.player.level, data.readBlockPos(), inv, inv.player);
            }));
            r.register(new ResourceLocation(StorageNetwork.MODID, "collector"), () -> MenuRegistry.ofExtended((windowId, inv, data) -> {
                return new ContainerCollectionFilter(windowId, inv.player.level, data.readBlockPos(), inv, inv.player);
            }));
            r.register(new ResourceLocation(StorageNetwork.MODID, "filter_kabel"), () -> MenuRegistry.ofExtended((windowId, inv, data) -> {
                return new ContainerCableFilter(windowId, inv.player.level, data.readBlockPos(), inv, inv.player);
            }));
            r.register(new ResourceLocation(StorageNetwork.MODID, "import_filter_kabel"), () -> MenuRegistry.ofExtended((windowId, inv, data) -> {
                return new ContainerCableImportFilter(windowId, inv.player.level, data.readBlockPos(), inv, inv.player);
            }));
            r.register(new ResourceLocation(StorageNetwork.MODID, "export_kabel"), () -> MenuRegistry.ofExtended((windowId, inv, data) -> {
                return new ContainerCableExportFilter(windowId, inv.player.level, data.readBlockPos(), inv, inv.player);
            }));
            r.register(new ResourceLocation(StorageNetwork.MODID, "inventory"), () -> MenuRegistry.ofExtended((windowId, inv, data) -> {
                return new ContainerNetworkInventory(windowId, inv.player.level, data.readBlockPos(), inv, inv.player);
            }));
            r.register(new ResourceLocation(StorageNetwork.MODID, "inventory_remote"), () -> MenuRegistry.of((windowId, inv) -> {
                return new ContainerNetworkRemote(windowId, inv.player.getInventory());
            }));
            r.register(new ResourceLocation(StorageNetwork.MODID, "crafting_remote"), () -> MenuRegistry.of((windowId, inv) -> {
                return new ContainerNetworkCraftingRemote(windowId, inv.player.getInventory());
            }));
        }
    }
}
