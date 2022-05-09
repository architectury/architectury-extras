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

package dev.architectury.transfer.wrapper.combined;

import com.google.common.collect.Iterables;
import dev.architectury.transfer.ResourceView;
import dev.architectury.transfer.TransferHandler;
import dev.architectury.transfer.wrapper.single.SingleTransferHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A {@link TransferHandler} that combines multiple {@link SingleTransferHandler}s.<br>
 * This is faster than using {@link CombinedTransferHandler} directly, as the size of
 * each {@link SingleTransferHandler} is known in advance.
 *
 * @param <T> the type of resource
 */
public interface CombinedSingleTransferHandler<T, P extends SingleTransferHandler<T>> extends CombinedTransferHandler<T>, Iterable<T> {
    @Override
    default Iterable<TransferHandler<T>> getHandlers() {
        return (Iterable<TransferHandler<T>>) (Iterable<? super P>) getContents();
    }
    
    List<P> getContents();
    
    @Override
    default Stream<ResourceView<T>> streamContents() {
        return (Stream<ResourceView<T>>) (Stream<? super P>) getContents().stream();
    }
    
    @Override
    default void withContents(Consumer<Iterable<ResourceView<T>>> consumer) {
        consumer.accept((Iterable<ResourceView<T>>) (Iterable<? super P>) getContents());
    }
    
    @Override
    default int getContentsSize() {
        return getContents().size();
    }
    
    @Override
    default P getContent(int index) {
        return getContents().get(index);
    }
    
    default T get(int index) {
        return getContent(index).getResource();
    }
    
    @NotNull
    @Override
    default Iterator<T> iterator() {
        return Iterables.transform(getContents(), P::getResource).iterator();
    }
    
    default int size() {
        return getContentsSize();
    }
}
