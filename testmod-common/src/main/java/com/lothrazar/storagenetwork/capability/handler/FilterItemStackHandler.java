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
import com.lothrazar.storagenetwork.util.UtilInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class FilterItemStackHandler extends ItemStackHandlerEx {
    
    public static final int FILTER_SIZE = 18;
    public boolean tags = false;
    public boolean nbt = false;
    public boolean isAllowList = true;
    
    public FilterItemStackHandler() {
        super(new SimpleContainer(FILTER_SIZE) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }
    
    public void setMatchOreDict(boolean ores) {
        this.tags = ores;
    }
    
    public void setMatchNbt(boolean nbt) {
        this.nbt = nbt;
    }
    
    public void setIsAllowlist(boolean is) {
        isAllowList = is;
    }
    
    public List<IItemStackMatcher> getStackMatchers() {
        return getStacks().stream().map(stack -> new ItemStackMatcher(stack, tags, nbt)).collect(Collectors.toList());
    }
    
    public boolean exactStackAlreadyInList(ItemStack stack) {
        // Should we want not to use the configured rules for nbt, oredict and meta, we can use this line instead, which really matches for the exact stack:
        //return getStacks().stream().map(filteredStack -> new ItemStackMatcher(filteredStack, true, false, true)).anyMatch(matcher -> matcher.match(stack));
        return getStackMatchers().stream().anyMatch(matcher -> matcher.match(stack));
    }
    
    public IItemStackMatcher getFirstMatcher(ItemStack stack) {
        for (IItemStackMatcher m : getStackMatchers()) {
            if (m.match(stack)) {
                return m;
            }
        }
        return null;
    }
    
    public boolean isStackFiltered(ItemStack stack) {
        if (isAllowList) {
            return getStackMatchers().stream().noneMatch(matcher -> matcher.match(stack));
        }
        return getStackMatchers().stream().anyMatch(matcher -> matcher.match(stack));
    }
    
    public boolean allAreEmpty() {
        for (ItemStack stack : this) {
            if (!stack.isEmpty()) {
                //found something not empty. so allAreEmpty is false
                return false;
            }
        }
        //none found that were !empty. so allempty true
        return true;
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        CompoundTag rulesTag = tag.getCompound("rules");
        tags = rulesTag.getBoolean("tags");
        this.nbt = rulesTag.getBoolean("nbt");
        isAllowList = rulesTag.getBoolean("whitelist");
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag rulesTag = new CompoundTag();
        rulesTag.putBoolean("tags", tags);
        rulesTag.putBoolean("nbt", nbt);
        rulesTag.putBoolean("whitelist", isAllowList);
        tag.put("rules", rulesTag);
        return super.save(tag);
    }
    
    public int getStackCount(ItemStack stackCurrent) {
        int s = 0;
        for (IItemStackMatcher m : getStackMatchers()) {
            if (UtilInventory.canStack(stackCurrent, m.getStack())) {
                return s += m.getStack().getCount();
            }
        }
        return s;
    }
}
