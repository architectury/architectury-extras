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

package dev.architectury.transfer.item.wrapper;

import dev.architectury.transfer.item.ItemTransferHandler;
import dev.architectury.transfer.item.simple.SimpleItemTransferHandler;
import dev.architectury.transfer.wrapper.combined.CombinedSingleTransferHandler;
import dev.architectury.transfer.wrapper.single.SimpleSingleTransferHandler;
import dev.architectury.transfer.wrapper.single.SingleTransferHandler;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.AbstractList;
import java.util.List;

public abstract class ContainerTransferHandler<C extends Container, P extends SingleTransferHandler<ItemStack>> implements CombinedItemTransferHandler, CombinedSingleTransferHandler<ItemStack, P> {
    public final C container;
    private List<P> handlers = null;
    
    public ContainerTransferHandler(C container) {
        this.container = container;
    }
    
    public static <C extends Container> ContainerTransferHandler<C, SimpleSingleTransferHandler<ItemStack>> of(C container) {
        return new ContainerTransferHandler<>(container) {
            @Override
            protected SimpleSingleTransferHandler<ItemStack> asTransfer(int index) {
                return new SlotTransferHandler(container, index);
            }
        };
    }
    
    public static SimpleItemTransferHandler simple(int size) {
        return new SimpleItemTransferHandler(size);
    }
    
    protected List<P> createHandlers() {
        return new Handlers();
    }
    
    @Override
    public List<P> getContents() {
        if (handlers == null) {
            handlers = createHandlers();
        }
        
        return handlers;
    }
    
    protected abstract P asTransfer(int index);
    
    protected class Handlers extends AbstractList<P> {
        @Override
        public P get(int index) {
            if (index < 0 || index >= size()) {
                throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for size " + size());
            }
            return asTransfer(index);
        }
        
        @Override
        public int size() {
            return container.getContainerSize();
        }
    }
    
    protected static class SlotTransferHandler implements SimpleSingleTransferHandler<ItemStack>, ItemTransferHandler {
        protected final Container container;
        protected final int index;
        
        public SlotTransferHandler(Container container, int index) {
            this.container = container;
            this.index = index;
        }
        
        @Override
        public void setResource(ItemStack resource) {
            container.setItem(index, resource);
        }
        
        @Override
        public ItemStack getResource() {
            return container.getItem(index);
        }
        
        @Override
        public long getCapacity(ItemStack resource) {
            return Math.min(container.getMaxStackSize(), resource.getMaxStackSize());
        }
    }
}
