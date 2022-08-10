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

import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.ITileNetworkSync;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.request.TileRequest;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import dev.architectury.registry.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class TileInventory extends TileConnectable implements MenuProvider, ITileNetworkSync {
    
    public static final String NBT_JEI = TileRequest.NBT_JEI;
    private boolean downwards;
    private EnumSortType sort = EnumSortType.NAME;
    private boolean isJeiSearchSynced;
    private boolean autoFocus = true;
    
    public TileInventory(BlockPos pos, BlockState state) {
        super(SsnRegistry.INVENTORYTILE.get(), pos, state);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable(Registries.getId(getType(), Registry.BLOCK_ENTITY_TYPE_REGISTRY).getPath());
    }
    
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ContainerNetworkInventory(i, level, worldPosition, playerInventory, playerEntity);
    }
    
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        autoFocus = compound.getBoolean("autoFocus");
        setDownwards(compound.getBoolean("dir"));
        setSort(EnumSortType.values()[compound.getInt("sort")]);
        if (compound.contains(NBT_JEI)) {
            this.setJeiSearchSynced(compound.getBoolean(NBT_JEI));
        }
    }
    
    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putBoolean("dir", isDownwards());
        compound.putInt("sort", getSort().ordinal());
        compound.putBoolean("autoFocus", autoFocus);
        compound.putBoolean(NBT_JEI, this.isJeiSearchSynced());
    }
    
    @Override
    public boolean isDownwards() {
        return downwards;
    }
    
    @Override
    public void setDownwards(boolean downwards) {
        this.downwards = downwards;
    }
    
    @Override
    public EnumSortType getSort() {
        return sort;
    }
    
    @Override
    public void setSort(EnumSortType sort) {
        this.sort = sort;
    }
    
    public boolean isJeiSearchSynced() {
        return isJeiSearchSynced;
    }
    
    @Override
    public void setJeiSearchSynced(boolean val) {
        isJeiSearchSynced = val;
    }
    
    public boolean getAutoFocus() {
        return autoFocus;
    }
    
    @Override
    public void setAutoFocus(boolean autoFocus) {
        this.autoFocus = autoFocus;
    }
}
