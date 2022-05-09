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

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
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
import java.util.Set;

class SimpleBlockLookupRegistration<T, Context> implements BlockLookupRegistration<T, Context>, BlockLookup<T, Context> {
    private final Multimap<Block, BlockAccessProvider<T, @Nullable BlockEntity, Context>> blocksMap = Multimaps.newMultimap(new Reference2ObjectOpenHashMap<>(), ArrayList::new);
    private final List<BlockAccessProvider<T, @Nullable BlockEntity, Context>> fallback = new ArrayList<>();
    
    public static <T, Context> SimpleBlockLookupRegistration<T, Context> create() {
        return new SimpleBlockLookupRegistration<>();
    }
    
    private SimpleBlockLookupRegistration() {
    }
    
    @Override
    public boolean register(ResourceLocation id, BlockAccessProvider<T, @Nullable BlockEntity, Context> provider) {
        fallback.add(provider);
        return true;
    }
    
    @Override
    public boolean registerForBlocks(ResourceLocation id, BlockAccessProvider<T, @Nullable BlockEntity, Context> provider, Block... blocks) {
        for (Block block : blocks) {
            blocksMap.put(block, provider);
        }
        return true;
    }
    
    @Override
    public <B extends BlockEntity> boolean registerForBlockEntities(ResourceLocation id, BlockAccessProvider<T, B, Context> provider, BlockEntityType<B>... blockEntityTypes) {
        Set<BlockEntityType<B>> set = ReferenceOpenHashSet.of(blockEntityTypes);
        return register(id, (level, pos, state, blockEntity, context) -> {
            if (blockEntity != null && set.contains(blockEntity.getType())) {
                return provider.get(level, pos, state, (B) blockEntity, context);
            }
            
            return null;
        });
    }
    
    @Override
    public <B extends BlockEntity> boolean registerForSelf(ResourceLocation id, BlockEntityType<B>... blockEntityTypes) {
        return registerForBlockEntities(id, (level, pos, state, blockEntity, context) -> {
            return blockEntity == null ? null : (T) blockEntity;
        }, blockEntityTypes);
    }
    
    @Override
    @Nullable
    public T get(Level level, BlockPos pos, Context context) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = null;
        if (state.hasBlockEntity()) {
            blockEntity = level.getBlockEntity(pos);
        }
        return get(level, pos, state, blockEntity, context);
    }
    
    @Override
    @Nullable
    public T get(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Context context) {
        if (blockEntity == null) blockEntity = level.getBlockEntity(pos);
        for (BlockAccessProvider<T, @Nullable BlockEntity, Context> provider : blocksMap.get(state.getBlock())) {
            T result = provider.get(level, pos, state, blockEntity, context);
            if (result != null) {
                return result;
            }
        }
        for (BlockAccessProvider<T, BlockEntity, Context> provider : fallback) {
            T result = provider.get(level, pos, state, blockEntity, context);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
