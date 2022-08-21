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

package dev.architectury.transfer;

import com.google.common.base.Predicates;
import dev.architectury.transfer.util.SimpleList;
import dev.architectury.transfer.wrapper.filtering.FilteringTransferHandler;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * A handler for transferring resources.
 * This is wrapped around apis given by the platform,
 * <p>
 * <b>DO NOT</b> extend this interface, binary compatibility is not guaranteed
 * with future versions if you do.
 *
 * @param <T> the type of resource
 */
@ApiStatus.NonExtendable
public interface TransferHandler<T> extends TransferView<T>, SimpleList<ResourceView<T>> {
    /**
     * Returns the size of the handler.
     * This may be extremely expensive to compute, avoid if you can.
     * <p>
     * {@inheritDoc}
     *
     * @return the size of the handler
     */
    @Deprecated
    @Override
    int size();
    
    /**
     * Returns the resource in a particular index.
     * This may be extremely expensive to compute, avoid if you can.
     * <p>
     * {@inheritDoc}
     *
     * @param index the index of the resource
     * @return the resource in the given index
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    @Deprecated
    @Override
    ResourceView<T> get(int index);
    
    /**
     * Inserts the given resource into a given resource index, returning the amount that was inserted.
     *
     * @param index    the index of the resource
     * @param toInsert the resource to insert
     * @param action   whether to simulate or actually insert the resource
     * @return the amount that was inserted
     */
    default long insertAt(int index, T toInsert, TransferAction action) {
        return get(index).insert(toInsert, action);
    }
    
    /**
     * Extracts the given resource from a given resource index, returning the stack that was extracted.
     *
     * @param index     the index of the resource
     * @param toExtract the resource to extract
     * @param action    whether to simulate or actually extract the resource
     * @return the stack that was extracted
     */
    default T extractAt(int index, T toExtract, TransferAction action) {
        return get(index).extract(toExtract, action);
    }
    
    /**
     * Extracts the given resource from a given resource index, returning the stack that was extracted.
     *
     * @param index     the index of the resource
     * @param toExtract the predicates to use to filter the resources to extract
     * @param maxAmount the maximum amount of resources to extract
     * @param action    whether to simulate or actually extract the resource
     * @return the stack that was extracted
     */
    default T extractAt(int index, Predicate<T> toExtract, long maxAmount, TransferAction action) {
        return get(index).extract(toExtract, maxAmount, action);
    }
    
    /**
     * Extracts the any resource from a given resource index, returning the stack that was extracted.
     *
     * @param index     the index of the resource
     * @param maxAmount the maximum amount of resources to extract
     * @param action    whether to simulate or actually extract the resource
     * @return the stack that was extracted
     */
    default T extractAt(int index, long maxAmount, TransferAction action) {
        return get(index).extractAny(maxAmount, action);
    }
    
    @Override
    default TransferHandler<T> unmodifiable() {
        return filter(Predicates.alwaysFalse());
    }
    
    @Override
    default TransferHandler<T> onlyInsert() {
        return filter(Predicates.alwaysTrue(), Predicates.alwaysFalse());
    }
    
    @Override
    default TransferHandler<T> onlyExtract() {
        return filter(Predicates.alwaysFalse(), Predicates.alwaysTrue());
    }
    
    @Override
    default TransferHandler<T> filter(Predicate<T> filter) {
        return filter(filter, filter);
    }
    
    @Override
    default TransferHandler<T> filter(Predicate<T> insert, Predicate<T> extract) {
        return FilteringTransferHandler.of(this, insert, extract);
    }
}
