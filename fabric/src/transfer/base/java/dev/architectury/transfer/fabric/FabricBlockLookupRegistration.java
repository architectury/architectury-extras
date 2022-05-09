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

import dev.architectury.transfer.access.BlockLookupRegistration;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class FabricBlockLookupRegistration<T, A, Context> implements BlockLookupRegistration<T, Context> {
    private final BiFunction<T, Context, A> unwrapper;
    private final BlockApiLookup<A, Context> lookup;
    
    public static <T, A, Context> FabricBlockLookupRegistration<T, A, Context> create(BlockApiLookup<A, Context> lookup, BiFunction<T, Context, A> unwrapper) {
        return new FabricBlockLookupRegistration<>(unwrapper, lookup);
    }
    
    private FabricBlockLookupRegistration(BiFunction<T, Context, A> unwrapper, BlockApiLookup<A, Context> lookup) {
        this.unwrapper = unwrapper;
        this.lookup = lookup;
    }
    
    public BlockApiLookup.BlockApiProvider<A, Context> provider(BlockAccessProvider<T, BlockEntity, Context> provider) {
        return (level, pos, state, blockEntity, context) -> {
            T value = provider.get(level, pos, state, blockEntity, context);
            if (value != null) {
                return unwrapper.apply(value, context);
            }
            
            return null;
            
        };
    }
    
    public <B extends BlockEntity> BlockApiLookup.BlockEntityApiProvider<A, Context> provider(Predicate<@Nullable BlockEntityType<?>> beTypePredicate, BlockAccessProvider<T, B, Context> provider) {
        return (blockEntity, context) -> {
            if (!beTypePredicate.test(blockEntity == null ? null : blockEntity.getType())) {
                return null;
            }
            
            T value = provider.get(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), (B) blockEntity, context);
            if (value != null) {
                return unwrapper.apply(value, context);
            }
            
            return null;
        };
    }
    
    @Override
    public boolean register(ResourceLocation id, BlockAccessProvider<T, @Nullable BlockEntity, Context> provider) {
        lookup.registerFallback(provider(provider));
        return true;
    }
    
    @Override
    public boolean registerForBlocks(ResourceLocation id, BlockAccessProvider<T, @Nullable BlockEntity, Context> provider, Block... blocks) {
        lookup.registerForBlocks(provider(provider), blocks);
        return true;
    }
    
    @Override
    public <B extends BlockEntity> boolean registerForBlockEntities(ResourceLocation id, BlockAccessProvider<T, B, Context> provider, BlockEntityType<B>... blockEntityTypes) {
        lookup.registerForBlockEntities(provider(type -> true, provider), blockEntityTypes);
        return true;
    }
    
    @Override
    public <B extends BlockEntity> boolean registerForSelf(ResourceLocation id, BlockEntityType<B>... blockEntityTypes) {
        lookup.registerSelf(blockEntityTypes);
        return false;
    }
}
