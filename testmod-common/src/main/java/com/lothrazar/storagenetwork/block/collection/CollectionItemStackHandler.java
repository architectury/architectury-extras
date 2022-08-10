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

package com.lothrazar.storagenetwork.block.collection;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.handler.FilterItemStackHandler;
import com.lothrazar.storagenetwork.capability.handler.ItemStackHandlerEx;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import dev.architectury.transfer.TransferAction;
import dev.architectury.transfer.wrapper.single.SimpleSingleTransferHandler;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * @author ajsnarr98 Created MasterItemStackHandler * ItemStackHandler used for interfacing with TileMain like a chest. https://github.com/ajsnarr98/Storage-Network/tree/ajsnarr98-inventory
 */
public class CollectionItemStackHandler extends ItemStackHandlerEx {
    
    private TileMain tileMain;
    TileConnectable tile;
    
    public CollectionItemStackHandler() {
        super(1);
        update();
    }
    
    public void setMain(TileMain main) {
        this.tileMain = main;
        update();
    }
    
    /**
     * Updates items in the handler based on outside storage.
     */
    public void update() {
        this.clear();
    }
    
    @Override
    protected SimpleSingleTransferHandler<ItemStack> asTransfer(int index) {
        return new SlotTransferHandler(container, index) {
            @Override
            public long insert(ItemStack toInsert, TransferAction action) {
                if (toInsert.isEmpty() || tileMain == null) {
                    return 0;
                }
                IConnectable cap = StorageNetworkCapabilities.CONNECTABLE_CAPABILITY.get(tile, null);
                //
                FilterItemStackHandler filter = cap.getFilter();
                if (filter != null
                        && !filter.allAreEmpty()
                        && filter.isStackFiltered(toInsert)) {
                    // filter is not empty, AND stack does not exist in filter
                    // so refuse this
                    //      StorageNetwork.log("refuse insertItem " + stack);
                    return 0;
                }
                try {
                    int remaining = tileMain.insertStack(toInsert, action == TransferAction.SIMULATE);
                    if (remaining > 0) {
                        // if failed, refresh whole list
                        update();
                        return toInsert.getCount() - remaining;
                    }
                } catch (Exception e) {
                    StorageNetwork.LOGGER.error("insertStack error ", e);
                }
                // if succesful, update internal list
                //      super.insertItem(slot, stack, simulate);
                update();
                return toInsert.getCount();
            }
        };
    }
    
    @Override
    public ItemStack extract(ItemStack toExtract, TransferAction action) {
        //disabled on this feature
        //    return super.extractItem(slot, 0, true);//disabled
        return ItemStack.EMPTY;
    }
    
    @Override
    public ItemStack extract(Predicate<ItemStack> toExtract, long maxAmount, TransferAction action) {
        //disabled on this feature
        //    return super.extractItem(slot, 0, true);//disabled
        return ItemStack.EMPTY;
    }
}
