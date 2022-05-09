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

package com.lothrazar.storagenetwork.item.remote;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.block.request.SlotCraftingNetwork;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.gui.NetworkCraftingInventory;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.util.UtilInventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Map;

public class ContainerNetworkCraftingRemote extends ContainerNetwork {
    
    Map<Integer, ItemStack> matrixStacks = new HashMap<>();
    private TileMain root;
    private ItemStack remote;
    
    public ContainerNetworkCraftingRemote(int id, Inventory pInv) {
        super(SsnRegistry.CRAFTINGREMOTE.get(), id);
        this.player = pInv.player;
        this.remote = pInv.player.getMainHandItem();
        if (this.remote.getItem() != SsnRegistry.CRAFTING_REMOTE.get()) {
            Triple<String, Integer, ItemStack> result = UtilInventory.getCurioRemote(player, SsnRegistry.CRAFTING_REMOTE.get());
            this.remote = result.getRight();
        }
        this.world = player.level;
        DimPos dp = DimPos.getPosStored(remote);
        if (dp == null) {
            StorageNetwork.LOGGER.error("Remote opening with null pos Stored {} ", remote);
        } else {
            this.root = dp.getTileEntity(TileMain.class, world);
        }
        matrix = new NetworkCraftingInventory(this, matrixStacks);
        this.playerInv = pInv;
        SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(this, playerInv.player, matrix, resultInventory, 0, 101, 128);
        slotCraftOutput.setTileMain(getTileMain());
        addSlot(slotCraftOutput);
        bindGrid();
        bindPlayerInvo(this.playerInv);
        bindHotbar();
        slotsChanged(matrix);
    }
    
    @Override
    public boolean stillValid(Player playerIn) {
        return !remote.isEmpty();
    }
    
    @Override
    public TileMain getTileMain() {
        if (root == null) {
            DimPos dp = DimPos.getPosStored(remote);
            if (dp != null) {
                root = dp.getTileEntity(TileMain.class, world);
            }
        }
        return root;
    }
    
    @Override
    public void slotsChanged(Container inventoryIn) {
        if (recipeLocked) {
            //      StorageNetwork.log("recipe locked so onCraftMatrixChanged cancelled");
            return;
        }
        //    findMatchingRecipe(matrix);
        super.slotsChanged(inventoryIn);
    }
    
    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        for (int i = 0; i < matrix.getContainerSize(); i++) {
            UtilInventory.dropItem(world, playerIn.blockPosition(), matrix.getItem(i));
        }
    }
    
    @Override
    public void slotChanged() {
    }
    
    @Override
    public boolean isCrafting() {
        return true;
    }
    
    public ItemStack getRemote() {
        return remote;
    }
}
