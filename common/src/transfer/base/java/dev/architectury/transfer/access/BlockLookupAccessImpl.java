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

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class BlockLookupAccessImpl<T, Context> implements BlockLookupAccess<T, Context> {
    private final List<BlockLookup<T, Context>> lookups = new ArrayList<>();
    private final List<BlockLookupRegistration<T, Context>> registrationHandlers = new ArrayList<>();
    
    @Override
    public void addQueryHandler(BlockLookup<T, Context> handler) {
        this.lookups.add(0, handler);
    }
    
    @Override
    public void addRegistrationHandler(BlockLookupRegistration<T, Context> registration) {
        this.registrationHandlers.add(0, registration);
    }
    
    @Override
    @Nullable
    public T get(Level level, BlockPos pos, Context context) {
        for (BlockLookup<T, Context> handler : lookups) {
            T result = handler.get(level, pos, context);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
    
    @Override
    @Nullable
    public T get(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Context context) {
        if (state.hasBlockEntity()) {
            if (blockEntity == null) {
                blockEntity = level.getBlockEntity(pos);
            }
        }
        
        for (BlockLookup<T, Context> handler : lookups) {
            T result = handler.get(level, pos, state, blockEntity, context);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
    
    @Override
    public boolean register(ResourceLocation id, BlockAccessProvider<T, @Nullable BlockEntity, Context> provider) {
        for (BlockLookupRegistration<T, Context> handler : registrationHandlers) {
            if (handler.register(id, provider)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean registerForBlocks(ResourceLocation id, BlockAccessProvider<T, @Nullable BlockEntity, Context> provider, Block... blocks) {
        for (BlockLookupRegistration<T, Context> handler : registrationHandlers) {
            if (handler.registerForBlocks(id, provider, blocks)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public <B extends BlockEntity> boolean registerForBlockEntities(ResourceLocation id, BlockAccessProvider<T, B, Context> provider, BlockEntityType<B>... blockEntityTypes) {
        for (BlockLookupRegistration<T, Context> handler : registrationHandlers) {
            if (handler.registerForBlockEntities(id, provider, blockEntityTypes)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public <B extends BlockEntity> boolean registerForSelf(ResourceLocation id, BlockEntityType<B>... blockEntityTypes) {
        for (BlockLookupRegistration<T, Context> handler : registrationHandlers) {
            if (handler.registerForSelf(id, blockEntityTypes)) {
                return true;
            }
        }
        
        return false;
    }
}
