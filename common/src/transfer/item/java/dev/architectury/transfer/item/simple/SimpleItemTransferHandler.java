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

package dev.architectury.transfer.item.simple;

import dev.architectury.transfer.TagSerializable;
import dev.architectury.transfer.item.wrapper.ContainerTransferHandler;
import dev.architectury.transfer.wrapper.single.BaseSingleTransferHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class SimpleItemTransferHandler extends ContainerTransferHandler<SimpleContainer, BaseSingleTransferHandler<ItemStack>> implements TagSerializable<CompoundTag> {
    public SimpleItemTransferHandler(int size) {
        this(new SimpleContainer(size));
    }
    
    public SimpleItemTransferHandler(SimpleContainer container) {
        super(container);
    }
    
    public void set(int index, ItemStack stack) {
        getContent(index).setResource(stack);
    }
    
    public void clear() {
        for (BaseSingleTransferHandler<ItemStack> content : getContents()) {
            content.setResource(ItemStack.EMPTY);
        }
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("Items", container.createTag());
        return tag;
    }
    
    @Override
    public void load(CompoundTag tag) {
        container.fromTag(tag.getList("Items", Tag.TAG_COMPOUND));
    }
    
    @Override
    protected BaseSingleTransferHandler<ItemStack> asTransfer(int index) {
        return new SlotTransferHandler(container, index);
    }
}
