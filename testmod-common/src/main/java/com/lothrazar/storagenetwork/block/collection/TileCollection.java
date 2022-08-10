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

import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import dev.architectury.registry.registries.Registries;
import dev.architectury.transfer.TransferHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileCollection extends TileConnectable implements MenuProvider {
    
    private CollectionItemStackHandler itemHandler;
    
    public TileCollection(BlockPos pos, BlockState state) {
        super(SsnRegistry.COLLECTORTILE.get(), pos, state);
        itemHandler = new CollectionItemStackHandler();
        itemHandler.tile = this;
    }
    
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
    }
    
    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
    }
    
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ContainerCollectionFilter(i, level, worldPosition, playerInventory, playerEntity);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable(Registries.getId(getType(), Registry.BLOCK_ENTITY_TYPE_REGISTRY).getPath());
    }
    
    @Override
    @Nullable
    public TransferHandler<ItemStack> getItemTransfer() {
        IConnectable capabilityConnectable = getConnectable();
        DimPos m = getMain();
        if (capabilityConnectable != null && m != null) {
            TileMain tileMain = m.getTileEntity(TileMain.class);
            itemHandler.setMain(tileMain);
        }
        //      else {
        //        StorageNetwork.log("Found NO CAP!!! for collector ");
        //      }
        return itemHandler;
    }
}
