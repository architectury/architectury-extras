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

import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.gui.NetworkCraftingInventory;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ContainerNetworkCraftingTable extends ContainerNetwork {
    
    private final TileRequest tileRequest;
    
    public ContainerNetworkCraftingTable(int windowId, Level world, BlockPos pos, Inventory playerInv, Player player) {
        super(SsnRegistry.REQUESTCONTAINER.get(), windowId);
        tileRequest = (TileRequest) world.getBlockEntity(pos);
        matrix = new NetworkCraftingInventory(this, tileRequest.matrix);
        this.playerInv = playerInv;
        SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(this, playerInv.player, matrix, resultInventory, 0, 101, 128);
        slotCraftOutput.setTileMain(getTileMain());
        addSlot(slotCraftOutput);
        bindGrid();
        bindPlayerInvo(this.playerInv);
        bindHotbar();
        slotsChanged(matrix);
    }
    
    @Override
    public boolean isCrafting() {
        return true;
    }
    
    @Override
    public void slotChanged() {
        //parent is abstract
        //seems to not happen from -shiftclick- crafting
        for (int i = 0; i < matrix.getContainerSize(); i++) {
            getTileRequest().matrix.put(i, matrix.getItem(i));
        }
    }
    
    @Override
    public boolean stillValid(Player playerIn) {
        //    TileMain main = getTileMain();
        TileRequest table = getTileRequest();
        BlockPos pos = table.getBlockPos();
        return playerIn.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
    
    @Override
    public TileMain getTileMain() {
        if (getTileRequest() == null || getTileRequest().getMain() == null) {
            return null;
        }
        return getTileRequest().getMain().getTileEntity(TileMain.class);
    }
    
    public TileRequest getTileRequest() {
        return tileRequest;
    }
}
