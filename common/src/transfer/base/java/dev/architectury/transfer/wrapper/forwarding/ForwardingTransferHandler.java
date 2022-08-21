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

package dev.architectury.transfer.wrapper.forwarding;

import com.google.common.collect.Iterators;
import dev.architectury.transfer.ResourceView;
import dev.architectury.transfer.TransferHandler;

import java.util.Iterator;

public interface ForwardingTransferHandler<T> extends TransferHandler<T>, ForwardingTransferView<T> {
    @Override
    TransferHandler<T> forwardingTo();
    
    default ResourceView<T> forwardResource(ResourceView<T> resource) {
        return resource;
    }
    
    @Override
    default Iterator<ResourceView<T>> iterator() {
        return Iterators.transform(forwardingTo().iterator(), this::forwardResource);
    }
    
    @Override
    @Deprecated
    default int size() {
        return forwardingTo().size();
    }
    
    @Override
    @Deprecated
    default ResourceView<T> get(int index) {
        return forwardResource(forwardingTo().get(index));
    }
}
