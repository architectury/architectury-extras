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

package dev.architectury.transfer.fabric;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.architectury.transfer.ResourceView;
import dev.architectury.transfer.TransferAction;
import dev.architectury.transfer.TransferHandler;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class FabricStorageTransferHandler<F, S> implements TransferHandler<S> {
    private final Storage<F> storage;
    @Nullable
    private final Transaction transaction;
    private final TypeAdapter<F, S> typeAdapter;
    
    public FabricStorageTransferHandler(Storage<F> storage, @Nullable Transaction transaction, TypeAdapter<F, S> typeAdapter) {
        this.storage = storage;
        this.transaction = transaction;
        this.typeAdapter = typeAdapter;
    }
    
    public Storage<F> getStorage() {
        return storage;
    }
    
    public static <T> T firstNonNull(T first, T second) {
        if (first != null) {
            return first;
        }
        return second;
    }
    
    @Override
    public Stream<ResourceView<S>> streamContents() {
        Transaction transaction = Transaction.openNested(firstNonNull(this.transaction, Transaction.getCurrentUnsafe()));
        return Streams.stream(storage.iterable(transaction))
                .<ResourceView<S>>map(view -> new FabricStorageResourceView(view, transaction, false))
                .onClose(transaction::commit);
    }
    
    @Override
    public int getContentsSize() {
        if (storage instanceof InventoryStorage) {
            return ((InventoryStorage) storage).getSlots().size();
        }
        try (Transaction transaction = Transaction.openNested(firstNonNull(this.transaction, Transaction.getCurrentUnsafe()))) {
            return Iterables.size(storage.iterable(transaction));
        }
    }
    
    @Override
    public ResourceView<S> getContent(int index) {
        if (storage instanceof InventoryStorage) {
            return new FabricStorageResourceView((StorageView<F>) ((InventoryStorage) storage).getSlots().get(index));
        }
        Transaction transaction = Transaction.openNested(firstNonNull(this.transaction, Transaction.getCurrentUnsafe()));
        return new FabricStorageResourceView(Iterables.get(storage.iterable(transaction), index), transaction, true);
    }
    
    @Override
    public long insert(S toInsert, TransferAction action) {
        long inserted;
        
        try (Transaction nested = Transaction.openNested(firstNonNull(this.transaction, Transaction.getCurrentUnsafe()))) {
            inserted = this.storage.insert(toFabric(toInsert), getAmount(toInsert), nested);
            
            if (action == TransferAction.ACT) {
                nested.commit();
            }
        }
        
        return inserted;
    }
    
    @Override
    public S extract(S toExtract, TransferAction action) {
        if (isEmpty(toExtract)) return blank();
        long extracted;
        
        try (Transaction nested = Transaction.openNested(firstNonNull(this.transaction, Transaction.getCurrentUnsafe()))) {
            extracted = this.storage.extract(toFabric(toExtract), getAmount(toExtract), nested);
            
            if (action == TransferAction.ACT) {
                nested.commit();
            }
        }
        
        return copyWithAmount(toExtract, extracted);
    }
    
    @Override
    public S extract(Predicate<S> toExtract, long maxAmount, TransferAction action) {
        try (Transaction nested = Transaction.openNested(firstNonNull(this.transaction, Transaction.getCurrentUnsafe()))) {
            for (StorageView<F> view : this.storage.iterable(nested)) {
                if (!view.isResourceBlank() && toExtract.test(fromFabric(view))) {
                    long extracted = view.extract(view.getResource(), maxAmount, nested);
                    
                    if (action == TransferAction.ACT) {
                        nested.commit();
                    }
                    
                    return fromFabric(view.getResource(), extracted);
                }
            }
        }
        
        return blank();
    }
    
    @Override
    public S blank() {
        return typeAdapter.blank.get();
    }
    
    @Override
    public S copyWithAmount(S stack, long amount) {
        return typeAdapter.copyWithAmount.apply(stack, amount);
    }
    
    @Override
    public Object saveState() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void loadState(Object state) {
        throw new UnsupportedOperationException();
    }
    
    private boolean isEmpty(S stack) {
        return typeAdapter.isEmpty.test(stack);
    }
    
    private long getAmount(S stack) {
        return typeAdapter.toAmount.applyAsLong(stack);
    }
    
    private F toFabric(S stack) {
        return typeAdapter.toFabric.apply(stack);
    }
    
    private S fromFabric(StorageView<F> view) {
        return fromFabric(view.getResource(), view.getAmount());
    }
    
    private S fromFabric(F variant, long amount) {
        return typeAdapter.fromFabric.apply(variant, amount);
    }
    
    public interface FunctionWithAmount<F, S> {
        S apply(F variant, long amount);
    }
    
    private class FabricStorageResourceView implements ResourceView<S> {
        private final StorageView<F> view;
        @Nullable
        private final Transaction transaction;
        private final boolean transactionClosable;
        
        private FabricStorageResourceView(StorageView<F> view) {
            this(view, null, false);
        }
        
        private FabricStorageResourceView(StorageView<F> view, @Nullable Transaction transaction, boolean transactionClosable) {
            this.view = view;
            this.transaction = transaction;
            this.transactionClosable = transactionClosable;
        }
        
        @Override
        public S getResource() {
            return fromFabric(view.getResource(), view.getAmount());
        }
        
        @Override
        public long getCapacity() {
            return view.getCapacity();
        }
        
        @Override
        public S copyWithAmount(S resource, long amount) {
            return FabricStorageTransferHandler.this.copyWithAmount(resource, amount);
        }
        
        @Override
        public long insert(S toInsert, TransferAction action) {
            if (view instanceof Storage) {
                long inserted;
                
                try (Transaction nested = Transaction.openNested(firstNonNull(this.transaction, Transaction.getCurrentUnsafe()))) {
                    inserted = ((Storage<F>) this.view).insert(toFabric(toInsert), getAmount(toInsert), nested);
                    
                    if (action == TransferAction.ACT) {
                        nested.commit();
                    }
                }
                
                return inserted;
            }
            
            // impossible to insert to a fabric storage view with an index
            return 0;
        }
        
        @Override
        public S extract(S toExtract, TransferAction action) {
            if (isEmpty(toExtract)) return blank();
            long extracted;
            
            try (Transaction nested = Transaction.openNested(firstNonNull(this.transaction, Transaction.getCurrentUnsafe()))) {
                extracted = this.view.extract(toFabric(toExtract), getAmount(toExtract), nested);
                
                if (action == TransferAction.ACT) {
                    nested.commit();
                }
            }
            
            return copyWithAmount(toExtract, extracted);
        }
        
        @Override
        public S blank() {
            return null;
        }
        
        @Override
        public Object saveState() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void loadState(Object state) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void close() {
            if (this.transaction != null && transactionClosable) {
                this.transaction.commit();
            }
        }
    }
    
    public static class TypeAdapter<F, S> {
        final Function<S, F> toFabric;
        final FunctionWithAmount<F, S> fromFabric;
        final FunctionWithAmount<S, S> copyWithAmount;
        final Supplier<S> blank;
        final Predicate<S> isEmpty;
        final ToLongFunction<S> toAmount;
        
        public TypeAdapter(Function<S, F> toFabric, FunctionWithAmount<F, S> fromFabric, FunctionWithAmount<S, S> copyWithAmount, Supplier<S> blank, Predicate<S> isEmpty, ToLongFunction<S> toAmount) {
            this.toFabric = toFabric;
            this.fromFabric = fromFabric;
            this.copyWithAmount = copyWithAmount;
            this.blank = blank;
            this.isEmpty = isEmpty;
            this.toAmount = toAmount;
        }
    }
}
