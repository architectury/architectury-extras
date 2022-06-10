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

package com.lothrazar.storagenetwork.block.cable.inputfilter;

import com.lothrazar.storagenetwork.block.cable.ContainerCable;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableAutoIO;
import com.lothrazar.storagenetwork.item.ItemUpgrade;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import dev.architectury.transfer.item.util.TransferHandlerSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContainerCableImportFilter extends ContainerCable {
    
    public final TileCableImportFilter tile;
    public CapabilityConnectableAutoIO cap;
    
    public ContainerCableImportFilter(int windowId, Level world, BlockPos pos, Inventory playerInv, Player player) {
        super(SsnRegistry.FILTERIMPORTCONTAINER.get(), windowId);
        tile = (TileCableImportFilter) world.getBlockEntity(pos);
        this.cap = tile.ioStorage;
        for (int i = 0; i < cap.upgrades.size(); i++) {
            int finalI = i;
            this.addSlot(new TransferHandlerSlot(() -> cap.upgrades.getContent(finalI), i, 98 + i * SQ, 6) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }
                
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof ItemUpgrade; // WAS no stock in import
                }
            });
        }
        this.bindPlayerInvo(playerInv);
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        //in range [4,39] means its coming FROM inventory
        // [0,3] is the filter list
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            if (stackInSlot.getItem() instanceof ItemUpgrade) {
                if (4 <= slotIndex && slotIndex <= 39) {
                    //FROM inventory to upgrade slots
                    if (!this.moveItemStackTo(stackInSlot, 0, 4, true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (0 <= slotIndex && slotIndex <= 3) {
                    //FROM upgrade slots TO inventory
                    if (!this.moveItemStackTo(stackInSlot, 0, 35, true)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }
}
