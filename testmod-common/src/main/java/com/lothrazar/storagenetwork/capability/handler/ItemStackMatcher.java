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

package com.lothrazar.storagenetwork.capability.handler;

import com.lothrazar.storagenetwork.api.IItemStackMatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemStackMatcher implements IItemStackMatcher {
    
    private ItemStack stack;
    private boolean ore;
    private boolean nbt;
    
    public ItemStackMatcher(ItemStack stack) {
        this(stack, false, false);
    }
    
    public ItemStackMatcher(ItemStack stack, boolean ore, boolean nbt) {
        this.stack = stack;
        this.ore = ore;
        this.nbt = nbt;
    }
    
    private ItemStackMatcher() {
    }
    
    public void readFromNBT(CompoundTag compound) {
        CompoundTag c = (CompoundTag) compound.get("stack");
        stack = ItemStack.of(c);
        ore = compound.getBoolean("ore");
        nbt = compound.getBoolean("nbt");
    }
    
    public CompoundTag writeToNBT(CompoundTag compound) {
        CompoundTag c = new CompoundTag();
        stack.save(c);
        compound.put("stack", c);
        compound.putBoolean("ore", ore);
        compound.putBoolean("nbt", nbt);
        return c;
    }
    
    @Override
    public String toString() {
        return "ItemStackMatcher [stack=" + stack + ", ore=" + ore + ", nbt=" + nbt + "]";
    }
    
    @Override
    public ItemStack getStack() {
        return stack;
    }
    
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }
    
    public boolean isOre() {
        return ore;
    }
    
    public void setOre(boolean ore) {
        this.ore = ore;
    }
    
    public boolean isNbt() {
        return nbt;
    }
    
    public void setNbt(boolean nbt) {
        this.nbt = nbt;
    }
    
    public static ItemStackMatcher loadFilterItemFromNBT(CompoundTag nbt) {
        ItemStackMatcher fil = new ItemStackMatcher();
        fil.readFromNBT(nbt);
        return fil.getStack() != null && fil.getStack().getItem() != null ? fil : null;
    }
    
    @Override
    public boolean match(ItemStack stackIn) {
        if (stackIn.isEmpty()) {
            return false;
        }
        if (nbt && !ItemStack.tagMatches(stack, stackIn)) {
            return false;
        }
        return stackIn.getItem() == stack.getItem();
    }
}
