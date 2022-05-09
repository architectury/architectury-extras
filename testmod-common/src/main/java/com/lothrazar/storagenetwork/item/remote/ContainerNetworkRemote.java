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
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.util.UtilInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;

public class ContainerNetworkRemote extends ContainerNetwork {
    
    private TileMain root;
    private ItemStack remote;
    
    public ContainerNetworkRemote(int id, Inventory pInv) {
        super(SsnRegistry.REMOTE.get(), id);
        this.player = pInv.player;
        this.world = player.level;
        Triple<String, Integer, ItemStack> result = UtilInventory.getCurioRemote(pInv.player, SsnRegistry.INVENTORY_REMOTE.get());
        this.remote = result.getRight();
        DimPos dp = DimPos.getPosStored(remote);
        if (dp == null) {
            StorageNetwork.LOGGER.error(world.isClientSide + "=client||Remote opening with null pos Stored {} ", result);
        } else {
            this.root = dp.getTileEntity(TileMain.class, world);
        }
        if (root == null) {
            //maybe the table broke after doing this, rare case
            StorageNetwork.log("CONTAINER NETWORK REMOTE null tile");
        }
        this.playerInv = pInv;
        bindPlayerInvo(this.playerInv);
        bindHotbar();
    }
    
    @Override
    public boolean stillValid(Player playerIn) {
        //does not store itemstack inventory, and opens from curios so no security here. unless it dissapears
        return !getRemote().isEmpty();
    }
    
    public ItemStack getRemote() {
        return remote;
    }
    
    @Override
    public TileMain getTileMain() {
        return root;
    }
    
    @Override
    public void slotChanged() {
    }
    
    @Override
    public boolean isCrafting() {
        return false;
    }
}
