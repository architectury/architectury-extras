/*
 * This file is part of architectury, licensed under the MIT License.
 * Copyright (c) 2017 - Original Author is MrRiegel as seen here https://github.com/MrRiegel
 * - Secondary author  Sam Bassett aka Lothrazar https://minecraft.curseforge.com/members/Lothrazar
 * Copyright (c) 2020, 2021, 2022 architectury
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.lothrazar.storagenetwork.api;

import com.google.common.base.Objects;
import com.lothrazar.storagenetwork.StorageNetwork;
import dev.architectury.transfer.access.BlockLookupAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import javax.annotation.Nullable;

public class DimPos {
    
    private String dimension;
    private BlockPos pos = new BlockPos(0, 0, 0);
    private Level world;
    
    public DimPos(CompoundTag tag) {
        deserializeNBT(tag);
    }
    
    public DimPos(Level world, BlockPos pos) {
        this.pos = pos;
        this.setWorld(world);
        if (world != null) {
            dimension = dimensionToString(world);
        }
    }
    
    public static DimPos getPosStored(ItemStack itemStackIn) {
        if (itemStackIn.getTag() == null || !itemStackIn.getTag().getBoolean(NBT_BOUND)) {
            return null;
        }
        return new DimPos(itemStackIn.getTag());
    }
    
    public Level getWorld() {
        return world;
    }
    
    public BlockPos getBlockPos() {
        return pos;
    }
    
    public BlockState getBlockState() {
        return getWorld().getBlockState(getBlockPos());
    }
    
    public <V> V getTileEntity(Class<V> tileEntityClassOrInterface) {
        return getTileEntity(tileEntityClassOrInterface, getWorld());
    }
    
    public static String dimensionToString(Level w) {
        //example: returns "minecraft:overworld" resource location
        return w.dimension().location().toString();
    }
    
    public static final String NBT_Z = "Z";
    public static final String NBT_Y = "Y";
    public static final String NBT_X = "X";
    public static final String NBT_DIM = "dimension";
    public static final String NBT_BOUND = "bound";
    
    public static void putPos(ItemStack stack, BlockPos pos, Level world) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_X, pos.getX());
        tag.putInt(NBT_Y, pos.getY());
        tag.putInt(NBT_Z, pos.getZ());
        tag.putString(NBT_DIM, DimPos.dimensionToString(world));
        tag.putBoolean(NBT_BOUND, true);
    }
    
    public static String getDim(ItemStack stack) {
        return stack.getOrCreateTag().getString(NBT_DIM);
    }
    
    public static void putDim(ItemStack stack, Level world) {
        stack.getOrCreateTag().putString(NBT_DIM, DimPos.dimensionToString(world));
    }
    
    public static ServerLevel stringDimensionLookup(String s, MinecraftServer serv) {
        return stringDimensionLookup(ResourceLocation.tryParse(s), serv);
    }
    
    public static ServerLevel stringDimensionLookup(ResourceLocation s, MinecraftServer serv) {
        ResourceKey<Level> worldKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, s);
        if (worldKey == null) {
            return null;
        }
        return serv.getLevel(worldKey);
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public <V> V getTileEntity(Class<V> tileEntityClassOrInterface, Level world) {
        BlockPos tilePos = getBlockPos();
        if (world == null || tilePos == null) {
            return null;
        }
        //refresh server world 
        if (dimension != null && world.getServer() != null
                && dimension.isEmpty() == false) {
            ServerLevel dimWorld = stringDimensionLookup(this.dimension, world.getServer());
            //reach across to the other dimension
            if (dimWorld != null) {
                world = dimWorld.getLevel();
            } else {
                StorageNetwork.LOGGER.error(" Dimworld NOT FOUND for " + dimension);
            }
        }
        //end refresh srever world
        BlockEntity tileEntity = world.getBlockEntity(tilePos);
        if (tileEntity == null) {
            return null;
        }
        if (!tileEntityClassOrInterface.isAssignableFrom(tileEntity.getClass())) {
            return null;
        }
        return (V) tileEntity;
    }
    
    public <V> V getCapability(BlockLookupAccess<V, Direction> capability, Direction side) {
        Level world = getWorld();
        if (world == null || getBlockPos() == null) {
            return null;
        }
        return capability.get(world, getBlockPos(), side);
    }
    
    public boolean isLoaded() {
        return world != null && world.hasChunkAt(pos);
    }
    
    public boolean equals(Level world, BlockPos pos) {
        //    world.dimension
        //    return dimension == world.provider.getDimension() &&
        // TODO ^^
        return pos.equals(this.pos);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DimPos dimPos = (DimPos) o;
        return dimension.equals(dimPos.dimension) &&
                Objects.equal(pos, dimPos.pos);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(dimension, pos);
    }
    
    @Override
    public String toString() {
        return "[" +
                "dimension=" + dimension +
                ", pos=" + pos +
                ", world=" + getWorld() +
                ']';
    }
    
    public CompoundTag serializeNBT() {
        if (pos == null) {
            pos = new BlockPos(0, 0, 0);
        }
        CompoundTag result = NbtUtils.writeBlockPos(pos);
        result.putString(NBT_DIM, dimension);
        return result;
    }
    
    public void deserializeNBT(CompoundTag nbt) {
        pos = NbtUtils.readBlockPos(nbt);
        dimension = nbt.getString(NBT_DIM);
    }
    
    public DimPos offset(Direction direction) {
        if (pos == null || direction == null) {
            StorageNetwork.LOGGER.info("Error: null offset in DimPos " + direction);
            return null;
        }
        return new DimPos(world, pos.relative(direction));
    }
    
    public ChunkAccess getChunk() {
        return world == null ? null : world.getChunk(pos);
    }
    
    public void setWorld(Level world) {
        this.world = world;
    }
    
    public String getDimension() {
        return dimension;
    }
    
    public Component makeTooltip() {
        if (pos == null) {
            return null;
        }
        return new TranslatableComponent("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ", " + dimension + "]").withStyle(ChatFormatting.DARK_GRAY);
    }
}
