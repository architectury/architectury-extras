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

import dev.architectury.transfer.ResourceView;
import dev.architectury.transfer.TransferAction;
import dev.architectury.transfer.wrapper.single.SingleTransferHandler;

import java.util.Iterator;
import java.util.function.Predicate;

public interface ForwardingSingleTransferHandler<T> extends SingleTransferHandler<T>, ForwardingTransferHandler<T>, ResourceView<T> {
    @Override
    SingleTransferHandler<T> forwardingTo();
    
    @Override
    default Iterator<ResourceView<T>> iterator() {
        return forwardingTo().iterator();
    }
    
    @Override
    default int size() {
        return forwardingTo().size();
    }
    
    @Override
    default ResourceView<T> get(int index) {
        return forwardingTo().get(index);
    }
    
    @Override
    default T extract(Predicate<T> toExtract, long maxAmount, TransferAction action) {
        return ForwardingTransferHandler.super.extract(toExtract, maxAmount, action);
    }
    
    @Override
    default long getAmount(T resource) {
        return forwardingTo().getAmount(resource);
    }
    
    @Override
    default boolean isSameVariant(T first, T second) {
        return forwardingTo().isSameVariant(first, second);
    }
    
    @Override
    default T getResource() {
        return forwardingTo().getResource();
    }
    
    @Override
    default long getCapacity(T resource) {
        return forwardingTo().getCapacity(resource);
    }
}
