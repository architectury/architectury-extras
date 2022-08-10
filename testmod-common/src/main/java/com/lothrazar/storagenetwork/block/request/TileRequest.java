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

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.ITileNetworkSync;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import dev.architectury.registry.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class TileRequest extends TileConnectable implements MenuProvider, ITileNetworkSync {
    
    public static final String NBT_JEI = StorageNetwork.MODID + "jei";
    private static final String NBT_DIR = StorageNetwork.MODID + "dir";
    private static final String NBT_SORT = StorageNetwork.MODID + "sort";
    public Map<Integer, ItemStack> matrix = new HashMap<>();
    private boolean downwards;
    private EnumSortType sort = EnumSortType.NAME;
    private boolean isJeiSearchSynced;
    private boolean autoFocus = true;
    
    public TileRequest(BlockPos pos, BlockState state) {
        super(SsnRegistry.REQUESTTILE.get(), pos, state);
    }
    
    @Override
    public void load(CompoundTag compound) {
        autoFocus = compound.getBoolean("autoFocus");
        setDownwards(compound.getBoolean(NBT_DIR));
        if (compound.contains(NBT_SORT)) {
            setSort(EnumSortType.values()[compound.getInt(NBT_SORT)]);
        }
        if (compound.contains(NBT_JEI)) {
            this.setJeiSearchSynced(compound.getBoolean(NBT_JEI));
        }
        ListTag invList = compound.getList("matrix", Tag.TAG_COMPOUND);
        matrix = new HashMap<>();
        for (int i = 0; i < invList.size(); i++) {
            CompoundTag stackTag = invList.getCompound(i);
            int slot = stackTag.getByte("Slot");
            ItemStack s = ItemStack.of(stackTag);
            matrix.put(slot, s);
        }
        super.load(compound);
    }
    
    @Override
    public void saveAdditional(CompoundTag compound) {
        compound.putBoolean("autoFocus", autoFocus);
        compound.putBoolean(NBT_DIR, isDownwards());
        compound.putInt(NBT_SORT, getSort().ordinal());
        compound.putBoolean(NBT_JEI, this.isJeiSearchSynced());
        ListTag invList = new ListTag();
        for (int i = 0; i < 9; i++) {
            if (matrix.get(i) != null && matrix.get(i).isEmpty() == false) {
                CompoundTag stackTag = new CompoundTag();
                stackTag.putByte("Slot", (byte) i);
                matrix.get(i).save(stackTag);
                invList.add(stackTag);
            }
        }
        compound.put("matrix", invList);
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
    
    @Override
    public Component getDisplayName() {
        return Component.translatable(Registries.getId(getType(), Registry.BLOCK_ENTITY_TYPE_REGISTRY).getPath());
    }
    
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ContainerNetworkCraftingTable(i, level, worldPosition, playerInventory, playerEntity);
    }
    
    public boolean isJeiSearchSynced() {
        return isJeiSearchSynced;
    }
    
    @Override
    public void setJeiSearchSynced(boolean val) {
        isJeiSearchSynced = val;
    }
    
    public boolean getAutoFocus() {
        return this.autoFocus;
    }
    
    @Override
    public void setAutoFocus(boolean autoFocus) {
        this.autoFocus = autoFocus;
    }
}
