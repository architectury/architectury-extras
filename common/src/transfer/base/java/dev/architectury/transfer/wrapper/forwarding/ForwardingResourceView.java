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

import java.util.function.Predicate;

public interface ForwardingResourceView<T> extends ResourceView<T>, ForwardingTransferView<T> {
    @Override
    ResourceView<T> forwardingTo();
    
    @Override
    default T getResource() {
        return forwardingTo().getResource();
    }
    
    @Override
    default long getCapacity(T resource) {
        return forwardingTo().getCapacity(resource);
    }
    
    @Override
    default T extract(Predicate<T> toExtract, long maxAmount, TransferAction action) {
        return ResourceView.super.extract(toExtract, maxAmount, action);
    }
}
