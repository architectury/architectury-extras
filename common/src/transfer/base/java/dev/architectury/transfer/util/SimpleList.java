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

package dev.architectury.transfer.util;

import it.unimi.dsi.fastutil.objects.ObjectIterators;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface SimpleList<T> extends List<T> {
    @Override
    default boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    default boolean contains(Object o) {
        for (T t : this) {
            if (Objects.equals(t, o)) {
                return true;
            }
        }
        
        return false;
    }
    
    @NotNull
    @Override
    default Object[] toArray() {
        int size = this.size();
        Object[] ret = new Object[size];
        int i = 0;
        for (T t : this) {
            ret[i++] = t;
        }
        return ret;
    }
    
    @NotNull
    @Override
    default <T1> T1[] toArray(@NotNull T1[] a) {
        int size = this.size();
        if (a.length < size) {
            a = Arrays.copyOf(a, size);
        }
        
        int i = 0;
        for (T t : this) {
            a[i++] = (T1) t;
        }
        
        if (a.length > size) {
            a[size] = null;
        }
        
        return a;
    }
    
    @Override
    default boolean add(T t) {
        add(size(), t);
        return true;
    }
    
    @Override
    default boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    default boolean containsAll(@NotNull Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }
    
    @Override
    default boolean addAll(@NotNull Collection<? extends T> c) {
        boolean modified = false;
        for (T e : c) {
            if (add(e))
                modified = true;
        }
        return modified;
    }
    
    @Override
    default boolean addAll(int index, @NotNull Collection<? extends T> c) {
        boolean modified = false;
        for (T e : c) {
            add(index++, e);
            modified = true;
        }
        return modified;
    }
    
    @Override
    default boolean removeAll(@NotNull Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            if (remove(e))
                modified = true;
        }
        return modified;
    }
    
    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }
    
    @Override
    default void clear() {
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
    
    static <T> boolean equals(List<T> list, Object o) {
        if (o == list)
            return true;
        if (!(o instanceof List))
            return false;
        
        ListIterator<T> e1 = list.listIterator();
        ListIterator<?> e2 = ((List<?>) o).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            T o1 = e1.next();
            Object o2 = e2.next();
            if (!(Objects.equals(o1, o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }
    
    static <T> int hashCode(List<T> list) {
        int hashCode = 1;
        for (T e : list)
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        return hashCode;
    }
    
    @Override
    default T set(int index, T element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    default void add(int index, T element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    default T remove(int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    default int indexOf(Object o) {
        int index = 0;
        for (T e : this) {
            if (Objects.equals(e, o)) {
                return index;
            }
            index++;
        }
        return -1;
    }
    
    @Override
    default int lastIndexOf(Object o) {
        ListIterator<T> it = listIterator(size());
        while (it.hasPrevious())
            if (Objects.equals(it.previous(), o))
                return it.nextIndex();
        return -1;
    }
    
    @NotNull
    @Override
    default ListIterator<T> listIterator() {
        return listIterator(0);
    }
    
    @NotNull
    @Override
    default ListIterator<T> listIterator(int index) {
        return new ObjectIterators.AbstractIndexBasedListIterator<T>(0, index) {
            @Override
            protected T get(int i) {
                return SimpleList.this.get(i);
            }
            
            @Override
            protected void add(int i, T k) {
                SimpleList.this.add(i, k);
            }
            
            @Override
            protected void set(int i, T k) {
                SimpleList.this.set(i, k);
            }
            
            @Override
            protected void remove(int i) {
                SimpleList.this.remove(i);
            }
            
            @Override
            protected int getMaxPos() {
                return SimpleList.this.size();
            }
        };
    }
    
    @NotNull
    @Override
    default List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex)
            throw new IndexOutOfBoundsException();
        return new AbstractList<>() {
            @Override
            public int size() {
                return toIndex - fromIndex;
            }
            
            @Override
            public T get(int index) {
                return SimpleList.this.get(index + fromIndex);
            }
            
            @Override
            public T set(int index, T element) {
                return SimpleList.this.set(index + fromIndex, element);
            }
            
            @Override
            public void add(int index, T element) {
                SimpleList.this.add(index + fromIndex, element);
            }
            
            @Override
            public T remove(int index) {
                return SimpleList.this.remove(index + fromIndex);
            }
        };
    }
}
