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

package dev.architectury.transfer.fluid.simple;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import dev.architectury.transfer.TagSerializable;
import dev.architectury.transfer.fluid.FluidTransferHandler;
import dev.architectury.transfer.fluid.wrapper.CombinedFluidTransferHandler;
import dev.architectury.transfer.wrapper.combined.CombinedSingleTransferHandler;
import dev.architectury.transfer.wrapper.single.SimpleSingleTransferHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleFluidTransferHandler<P extends SimpleSingleTransferHandler<FluidStack>> implements CombinedFluidTransferHandler, CombinedSingleTransferHandler<FluidStack, P>,
        TagSerializable<CompoundTag> {
    private final List<P> handlers;
    
    public SimpleFluidTransferHandler(int size) {
        this.handlers = createHandlers(size);
    }
    
    public static SimpleFluidTransferHandler<SimpleSingleTransferHandler<FluidStack>> of(int size, long capacity) {
        return new SimpleFluidTransferHandler<>(size) {
            @Override
            protected SimpleSingleTransferHandler<FluidStack> asTransfer(int index, long capacity) {
                return new SlotTransferHandler(index, capacity);
            }
            
            @Override
            protected long getCapacity(int index) {
                return capacity;
            }
        };
    }
    
    public void set(int index, FluidStack stack) {
        getContent(index).setResource(stack);
    }
    
    public void clear() {
        for (SimpleSingleTransferHandler<FluidStack> content : getContents()) {
            content.setResource(FluidStack.empty());
        }
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (P content : getContents()) {
            CompoundTag compound = new CompoundTag();
            compound.put("Fluid", FluidStackHooks.write(content.getResource(), new CompoundTag()));
            compound.putLong("Capacity", content.getResourceCapacity());
            list.add(compound);
        }
        tag.put("Fluids", list);
        return tag;
    }
    
    @Override
    public void load(CompoundTag tag) {
        ListTag list = tag.getList("Fluids", Tag.TAG_COMPOUND);
        this.handlers.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag compound = list.getCompound(i);
            P handler = asTransfer(i, compound.getLong("Capacity"));
            handler.setResource(FluidStackHooks.read(compound.getCompound("Fluid")));
            this.handlers.add(handler);
        }
    }
    
    @Override
    public List<P> getContents() {
        return handlers;
    }
    
    protected List<P> createHandlers(int size) {
        List<P> handlers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            handlers.add(asTransfer(i, getCapacity(i)));
        }
        return handlers;
    }
    
    protected abstract P asTransfer(int index, long capacity);
    
    protected abstract long getCapacity(int index);
    
    protected static class SlotTransferHandler implements SimpleSingleTransferHandler<FluidStack>, FluidTransferHandler {
        protected final int index;
        protected FluidStack stack = FluidStack.empty();
        protected final long capacity;
        
        public SlotTransferHandler(int index, long capacity) {
            this.index = index;
            this.capacity = capacity;
        }
        
        @Override
        public void setResource(FluidStack resource) {
            this.stack = resource;
        }
        
        @Override
        public FluidStack getResource() {
            return this.stack;
        }
        
        @Override
        public long getCapacity(FluidStack resource) {
            return this.capacity;
        }
    }
}
