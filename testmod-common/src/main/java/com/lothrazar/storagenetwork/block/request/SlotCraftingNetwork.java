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

package com.lothrazar.storagenetwork.block.request;

import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SlotCraftingNetwork extends ResultSlot {
    
    private TileMain tileMain;
    private final ContainerNetwork parent;
    
    public SlotCraftingNetwork(ContainerNetwork parent, Player player, CraftingContainer craftingInventory, Container inventoryIn, int slotIndex, int xPosition, int yPosition) {
        super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
        this.parent = parent;
    }
    
    @Override
    public void onTake(Player playerIn, ItemStack stack) {
        if (playerIn.level.isClientSide) {
            return;
        }
        List<ItemStack> lis = Lists.newArrayList();
        for (int i = 0; i < parent.matrix.getContainerSize(); i++) {
            lis.add(parent.matrix.getItem(i).copy());
        }
        super.onTake(playerIn, stack);
        parent.broadcastChanges();
        for (int i = 0; i < parent.matrix.getContainerSize(); i++) {
            if (parent.matrix.getItem(i).isEmpty() && getTileMain() != null) {
                ItemStack req = getTileMain().request(
                        !lis.get(i).isEmpty() ? new ItemStackMatcher(lis.get(i), false, false) : null, 1, false);
                if (!req.isEmpty()) {
                    parent.matrix.setItem(i, req);
                }
            }
        }
        parent.broadcastChanges();
        return;
    }
    
    public TileMain getTileMain() {
        return tileMain;
    }
    
    public void setTileMain(TileMain in) {
        this.tileMain = in;
    }
}
