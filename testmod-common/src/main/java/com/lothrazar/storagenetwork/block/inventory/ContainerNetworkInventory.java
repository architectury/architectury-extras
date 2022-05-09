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

package com.lothrazar.storagenetwork.block.inventory;

import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ContainerNetworkInventory extends ContainerNetwork {
    
    final TileInventory tile;
    
    public ContainerNetworkInventory(int windowId, Level world, BlockPos pos, Inventory playerInv, Player player) {
        super(SsnRegistry.INVENTORYCONTAINER.get(), windowId);
        tile = (TileInventory) world.getBlockEntity(pos);
        this.playerInv = playerInv;
        bindPlayerInvo(this.playerInv);
        bindHotbar();
    }
    
    @Override
    public boolean isCrafting() {
        return false;
    }
    
    @Override
    public void slotChanged() {
    }
    
    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }
    
    @Override
    public TileMain getTileMain() {
        if (tile == null || tile.getMain() == null) {
            //refresh delay, new chunk load or block placement
            return null;
        }
        return tile.getMain().getTileEntity(TileMain.class);
    }
}
