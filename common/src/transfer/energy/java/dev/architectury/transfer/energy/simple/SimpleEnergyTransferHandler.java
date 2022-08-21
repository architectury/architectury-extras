/*
 * This file is part of architectury.
 * Copyright (C) 2020, 2021, 2022 architectury
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package dev.architectury.transfer.energy.simple;

import dev.architectury.transfer.TagSerializable;
import net.minecraft.nbt.CompoundTag;

public class SimpleEnergyTransferHandler implements dev.architectury.transfer.energy.wrapper.SimpleEnergyTransferHandler, TagSerializable<CompoundTag> {
    protected long stored;
    protected long capacity;
    
    public SimpleEnergyTransferHandler() {
        this(0, 0);
    }
    
    public SimpleEnergyTransferHandler(long stored, long capacity) {
        this.stored = stored;
        this.capacity = capacity;
    }
    
    @Override
    public long getCapacity(Long resource) {
        return this.capacity;
    }
    
    @Override
    public Long getResource() {
        return this.stored;
    }
    
    @Override
    public void setResource(long resource) {
        this.stored = resource;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("Energy", this.stored);
        tag.putLong("Capacity", this.capacity);
        return tag;
    }
    
    @Override
    public void load(CompoundTag tag) {
        this.stored = tag.getLong("Energy");
        this.capacity = tag.getLong("Capacity");
    }
}
