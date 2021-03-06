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

package dev.architectury.transfer.access;

/**
 * An API lookup for blocks.
 *
 * @param <T>       the type of the API
 * @param <Context> the type of the context
 */
public interface BlockLookupAccess<T, Context> extends ApiLookupAccess<T, BlockLookup<T, Context>, BlockLookupRegistration<T, Context>>, BlockLookup<T, Context>, BlockLookupRegistration<T, Context> {
    static <T, Context> BlockLookupAccess<T, Context> create() {
        return new BlockLookupAccessImpl<>();
    }
    
    default BlockLookupAccess<T, Context> attachSimpleLookup() {
        SimpleBlockLookupRegistration<T, Context> registration = SimpleBlockLookupRegistration.create();
        this.addRegistrationHandler(registration);
        this.addQueryHandler(registration);
        return this;
    }
}
