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
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.api.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import dev.architectury.transfer.access.BlockLookupAccess;
import dev.architectury.transfer.item.ItemTransfer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class StorageNetworkCapabilities {
    public static final BlockLookupAccess<IConnectable, @Nullable Direction> CONNECTABLE_CAPABILITY = BlockLookupAccess.create();
    public static final BlockLookupAccess<IConnectableLink, @Nullable Direction> CONNECTABLE_ITEM_STORAGE_CAPABILITY = BlockLookupAccess.create();
    public static final BlockLookupAccess<IConnectableItemAutoIO, @Nullable Direction> CONNECTABLE_AUTO_IO = BlockLookupAccess.create();
    
    static {
        CONNECTABLE_CAPABILITY.attachSimpleLookup();
        CONNECTABLE_ITEM_STORAGE_CAPABILITY.attachSimpleLookup();
        CONNECTABLE_AUTO_IO.attachSimpleLookup();
        CONNECTABLE_CAPABILITY.registerForBlockEntities(new ResourceLocation(StorageNetwork.MODID, "tile_connectable"), (level, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof StorageNetworkCapsProvider) {
                return ((StorageNetworkCapsProvider) blockEntity).getConnectable();
            }
            
            return null;
        }, SsnRegistry.BLOCK_ENTITY_TYPES.toArray(new BlockEntityType[0]));
        CONNECTABLE_ITEM_STORAGE_CAPABILITY.registerForBlockEntities(new ResourceLocation(StorageNetwork.MODID, "tile_connectable_link"), (level, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof StorageNetworkCapsProvider) {
                return ((StorageNetworkCapsProvider) blockEntity).getConnectableLink();
            }
            
            return null;
        }, SsnRegistry.BLOCK_ENTITY_TYPES.toArray(new BlockEntityType[0]));
        CONNECTABLE_AUTO_IO.registerForBlockEntities(new ResourceLocation(StorageNetwork.MODID, "tile_connectable_auto_io"), (level, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof StorageNetworkCapsProvider) {
                return ((StorageNetworkCapsProvider) blockEntity).getConnectableItemAutoIO();
            }
            
            return null;
        }, SsnRegistry.BLOCK_ENTITY_TYPES.toArray(new BlockEntityType[0]));
        ItemTransfer.BLOCK.registerForBlockEntities(new ResourceLocation(StorageNetwork.MODID, "tile_connectable_item"), (level, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof StorageNetworkCapsProvider) {
                return ((StorageNetworkCapsProvider) blockEntity).getItemTransfer();
            }
            
            return null;
        }, SsnRegistry.BLOCK_ENTITY_TYPES.toArray(new BlockEntityType[0]));
    }
}
