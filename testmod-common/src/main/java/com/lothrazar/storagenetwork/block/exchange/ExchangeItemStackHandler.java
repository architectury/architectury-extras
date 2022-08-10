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

package com.lothrazar.storagenetwork.block.exchange;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.handler.ItemStackHandlerEx;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import dev.architectury.transfer.TransferAction;
import dev.architectury.transfer.wrapper.single.SimpleSingleTransferHandler;
import net.minecraft.world.item.ItemStack;

/**
 * @author ajsnarr98 Created MasterItemStackHandler * ItemStackHandler used for interfacing with TileMain like a chest. https://github.com/ajsnarr98/Storage-Network/tree/ajsnarr98-inventory
 */
public class ExchangeItemStackHandler extends ItemStackHandlerEx {
    
    TileMain tileMain;
    
    public ExchangeItemStackHandler() {
        super(Math.min(5000, ConfigRegistry.EXCHANGEBUFFER));
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
        if (tileMain == null || tileMain.getLevel() == null) {
            return;
        }
        try {
            //      StorageNetwork.log("exchange update started");
            this.clear();
            int i = 0;
            for (ItemStack stack : tileMain.getStacks()) {
                if (i >= this.size()) {
                    break;
                }
                this.set(i, stack);
                i++;
            }
            //      StorageNetwork.log("exchange updated " + i);
        } catch (Exception e) {
            StorageNetwork.LOGGER.error("Exchange update error ", e);
        }
    }
    
    /**
     * @param slot may not end up in the exact slot specified
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack). May be the same as the input ItemStack if unchanged, otherwise a new
     * ItemStack. The returned ItemStack can be safely modified after.
     */
//    @Override
//    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
//        if (stack.isEmpty() || tileMain == null) {
//            return stack;
//        }
//        try {
//            int remaining = tileMain.insertStack(stack, simulate);
//            //  StorageNetwork.log("exchange: insertItem " + stack + " remain " + remaining);
//            if (remaining > 0) {
//                // if failed, refresh whole list
//                update();
//                return ItemStackHooks.copyWithCount(stack, remaining);
//            }
//        } catch (Exception e) {
//            StorageNetwork.LOGGER.error("insertStack error ", e);
//        }
//        update();
//        return ItemStack.EMPTY;
//    }
    @Override
    protected SimpleSingleTransferHandler<ItemStack> asTransfer(int index) {
        return new SlotTransferHandler(container, index) {
            @Override
            public long insert(ItemStack toInsert, TransferAction action) {
                if (toInsert.isEmpty() || tileMain == null) {
                    return 0;
                }
                try {
                    int remaining = tileMain.insertStack(toInsert, action == TransferAction.SIMULATE);
                    //  StorageNetwork.log("exchange: insertItem " + stack + " remain " + remaining);
                    if (remaining > 0) {
                        // if failed, refresh whole list
                        update();
                        // return the inserted
                        return toInsert.getCount() - remaining;
                    }
                } catch (Exception e) {
                    StorageNetwork.LOGGER.error("insertStack error ", e);
                }
                update();
                return toInsert.getCount();
            }
            
            @Override
            public ItemStack extract(ItemStack toExtract, TransferAction action) {
                if (tileMain == null) {
                    //            super.extractItem(slot, amount, simulate);
                    return ItemStack.EMPTY;
                }
                ItemStackMatcher matcher = new ItemStackMatcher(getResource());
                //    StorageNetwork.log("extractItem " + matcher.getStack());
                ItemStack stack = tileMain.request(matcher, toExtract.getCount(), action == TransferAction.SIMULATE); // Stackoverflow?
                update();
                // StorageNetwork.log("exchange: extractItem; after " + stack);
                return stack;
            }
        };
    }

//    @Override
//    public ItemStack extractItem(int slot, int amount, boolean simulate) {
//        if (tileMain == null) {
//            //            super.extractItem(slot, amount, simulate);
//            return ItemStack.EMPTY;
//        }
//        ItemStackMatcher matcher = new ItemStackMatcher(getStackInSlot(slot));
//        //    StorageNetwork.log("extractItem " + matcher.getStack());
//        ItemStack stack = tileMain.request(matcher, amount, simulate); // Stackoverflow?
//        update();
//        // StorageNetwork.log("exchange: extractItem; after " + stack);
//        return stack;
//    }
}
