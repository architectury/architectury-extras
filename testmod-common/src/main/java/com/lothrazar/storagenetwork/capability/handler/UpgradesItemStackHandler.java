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

import com.lothrazar.storagenetwork.item.ItemUpgrade;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class UpgradesItemStackHandler extends ItemStackHandlerEx {
    
    public UpgradesItemStackHandler() {
        super(new SimpleContainer(SsnRegistry.UPGRADE_COUNT) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }
    
    public UpgradesItemStackHandler(int size) {
        this();
        //    super(SsnRegistry.UPGRADE_COUNT);
    }
    
//    @Override
//    protected void validateSlotIndex(int slot) {
//        if (stacks.size() == 1) {
//            this.setSize(SsnRegistry.UPGRADE_COUNT);
//        }
//        super.validateSlotIndex(slot);
//    }
    
    public boolean hasUpgradesOfType(ItemUpgrade upgradeType) {
        for (ItemStack stack : getStacks()) {
            if (stack.getItem() == upgradeType) {
                return true;
            }
        }
        return false;
    }
    
    public int getUpgradesOfType(ItemUpgrade upgradeType) {
        int res = 0;
        for (ItemStack stack : getStacks()) {
            if (stack.getItem() == upgradeType) {
                res += Math.max(stack.getCount(), 0);
            }
        }
        return res;
    }
}
