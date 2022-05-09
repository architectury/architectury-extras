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

package com.lothrazar.storagenetwork.block.cable.input;

import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.block.cable.BlockCable;
import com.lothrazar.storagenetwork.block.cable.EnumConnectType;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableAutoIO;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileCableIO extends TileCableWithFacing {
    
    protected CapabilityConnectableAutoIO ioStorage;
    
    public TileCableIO(BlockPos pos, BlockState state) {
        super(SsnRegistry.IMPORTKABELTILE.get(), pos, state);
        this.ioStorage = new CapabilityConnectableAutoIO(this, EnumStorageDirection.IN);
    }
    
    @Override
    public void setDirection(Direction direction) {
        super.setDirection(direction);
        this.ioStorage.setInventoryFace(direction);
    }
    
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.ioStorage.deserializeNBT(compound.getCompound("ioStorage"));
    }
    
    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("ioStorage", this.ioStorage.serializeNBT());
        
    }
    
    @Override
    @Nullable
    public IConnectableItemAutoIO getConnectableItemAutoIO() {
        return ioStorage;
    }
    
    private void tick() {
        if (this.getDirection() == null && !level.isClientSide) {
            this.findNewDirection();
            if (getDirection() != null) {
                BlockState newState = BlockCable.cleanBlockState(this.getBlockState());
                newState = newState.setValue(BlockCable.FACING_TO_PROPERTY_MAP.get(getDirection()), EnumConnectType.CABLE);
                level.setBlockAndUpdate(worldPosition, newState);
            }
        }
    }
    
    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableIO tile) {
        tile.tick();
    }
    
    public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableIO tile) {
        tile.tick();
    }
}
