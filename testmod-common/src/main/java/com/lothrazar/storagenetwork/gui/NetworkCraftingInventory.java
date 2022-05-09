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

package com.lothrazar.storagenetwork.gui;

import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class NetworkCraftingInventory extends CraftingContainer {
    
    /**
     * stupid thing is private with no getter so overwrite
     */
    private final NonNullList<ItemStack> stackList;
    private final AbstractContainerMenu eventHandler;
    public boolean skipEvents;
    
    private NetworkCraftingInventory(AbstractContainerMenu eventHandlerIn, int width, int height) {
        super(eventHandlerIn, width, height);
        eventHandler = eventHandlerIn;
        stackList = NonNullList.withSize(3 * 3, ItemStack.EMPTY);
    }
    
    public NetworkCraftingInventory(AbstractContainerMenu eventHandlerIn, Map<Integer, ItemStack> matrix) {
        this(eventHandlerIn, 3, 3);
        skipEvents = true;
        for (int i = 0; i < 9; i++) {
            if (matrix.get(i) != null && matrix.get(i).isEmpty() == false) {
                setItem(i, matrix.get(i));
            }
        }
        skipEvents = false;
    }
    
    public NetworkCraftingInventory(AbstractContainerMenu eventHandlerIn, List<ItemStack> matrix) {
        this(eventHandlerIn, 3, 3);
        skipEvents = true;
        for (int i = 0; i < 9; i++) {
            if (matrix.get(i) != null && matrix.get(i).isEmpty() == false) {
                setItem(i, matrix.get(i));
            }
        }
        skipEvents = false;
    }
    
    @Override
    public void setItem(int index, ItemStack stack) {
        stackList.set(index, stack);
        if (skipEvents == false) {
            eventHandler.slotsChanged(this);
        }
    }
    
    @Override
    public int getContainerSize() {
        return stackList.size();
    }
    
    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : stackList) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public ItemStack getItem(int index) {
        return index >= getContainerSize() ? ItemStack.EMPTY : stackList.get(index);
    }
    
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(stackList, index);
    }
    
    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack itemstack = ContainerHelper.removeItem(stackList, index, count);
        if (!itemstack.isEmpty()) {
            eventHandler.slotsChanged(this);
        }
        return itemstack;
    }
    
    @Override
    public void clearContent() {
        stackList.clear();
    }
    
    @Override
    public void fillStackedContents(StackedContents helper) {
        for (ItemStack itemstack : stackList) {
            helper.accountStack(itemstack);
        }
    }
}
