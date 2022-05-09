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

package com.lothrazar.storagenetwork.capability;

import com.lothrazar.storagenetwork.api.DimPos;
import net.minecraft.nbt.CompoundTag;

public class CapabilityConnectable extends DefaultConnectable {
    
    public CapabilityConnectable() {
        filters.setIsAllowlist(true);
    }
    
    public CompoundTag serializeNBT() {
        CompoundTag result = new CompoundTag();
        if (getMainPos() == null) {
            return result;
        }
        result.put("master", getMainPos().serializeNBT());
        if (getPos() != null) {
            result.put("self", getPos().serializeNBT());
        }
        CompoundTag filters = this.filters.save(new CompoundTag());
        result.put("filters", filters);
        result.putBoolean("needsRedstone", this.needsRedstone());
        return result;
    }
    
    public void deserializeNBT(CompoundTag nbt) {
        setMainPos(new DimPos(nbt.getCompound("master")));
        if (nbt.contains("self")) {
            setPos(new DimPos(nbt.getCompound("self")));
        }
        if (nbt.contains("filters")) {
            CompoundTag filters = nbt.getCompound("filters");
            this.filters.load(filters);
        }
        this.needsRedstone(nbt.getBoolean("needsRedstone"));
    }
    //  public static class Storage implements Capability.IStorage<IConnectable> {
    //
    //    @Override
    //    public Tag writeNBT(Capability<IConnectable> capability, IConnectable instance, Direction side) {
    //      CapabilityConnectable i = (CapabilityConnectable) instance;
    //      return i.serializeNBT();
    //    }
    //
    //    @Override
    //    public void readNBT(Capability<IConnectable> capability, IConnectable instance, Direction side, Tag nbt) {
    //      CapabilityConnectable i = (CapabilityConnectable) instance;
    //      i.deserializeNBT((CompoundTag) nbt);
    //    }
    //  }
}
