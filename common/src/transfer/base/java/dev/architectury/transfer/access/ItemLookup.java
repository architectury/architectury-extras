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

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ItemLookup<T, Context> {
    /**
     * Queries the api for the given item stack.
     *
     * @param stack   the item stack
     * @param context the context
     * @return the transfer handler, or null if none was found
     */
    @Nullable
    T get(ItemStack stack, Context context);
}