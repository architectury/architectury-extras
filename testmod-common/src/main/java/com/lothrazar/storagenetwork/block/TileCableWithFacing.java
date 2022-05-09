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

package com.lothrazar.storagenetwork.block;

import com.lothrazar.storagenetwork.block.cable.BlockCable;
import com.lothrazar.storagenetwork.block.cable.EnumConnectType;
import com.lothrazar.storagenetwork.block.main.TileMain;
import dev.architectury.transfer.item.ItemTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TileCableWithFacing extends TileConnectable {
    
    Direction direction = null;
    
    public TileCableWithFacing(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public BlockPos getFacingPosition() {
        return this.getBlockPos().relative(direction);
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    protected boolean isValidLinkNeighbor(Direction facing) {
        if (facing == null) {
            return false;
        }
        if (!TileMain.isTargetAllowed(level.getBlockState(worldPosition.relative(facing)))) {
            return false;
        }
        BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(facing));
        return neighbor != null && ItemTransfer.BLOCK.get(neighbor, facing.getOpposite()) != null;
    }
    
    public void findNewDirection() {
        //    if (isValidLinkNeighbor(direction)) {
        //      return;
        //    }
        for (Direction facing : Direction.values()) {
            if (isValidLinkNeighbor(facing)) {
                setDirection(facing);
                return;
            }
        }
        setDirection(null);
    }
    
    public void rotate() {
        Direction previous = direction;
        List<Direction> targetFaces = Arrays.asList(Direction.values());
        Collections.shuffle(targetFaces);
        for (Direction facing : Direction.values()) {
            if (previous == facing) {
                continue;
            }
            if (isValidLinkNeighbor(facing)) {
                setDirection(facing);
                this.setChanged();
                if (previous != direction) {
                    TileMain mainNode = getTileMain();
                    if (mainNode != null) {
                        mainNode.refreshNetwork();
                    }
                }
                return;
            }
        }
    }
    
    public void refreshDirection() {
        if (this.getDirection() == null && !level.isClientSide()) {
            this.findNewDirection();
            if (getDirection() != null) {
                BlockState newState = BlockCable.cleanBlockState(this.getBlockState());
                newState = newState.setValue(BlockCable.FACING_TO_PROPERTY_MAP.get(getDirection()), EnumConnectType.INVENTORY);
                level.setBlockAndUpdate(worldPosition, newState);
            }
        }
    }
    
    public TileMain getTileMain() {
        if (getMain() == null) {
            return null;
        }
        return getMain().getTileEntity(TileMain.class);
    }
    
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("direction")) {
            this.direction = Direction.values()[(compound.getInt("direction"))];
        } else {
            this.direction = null;
        }
    }
    
    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (direction != null) {
            compound.putInt("direction", this.direction.ordinal());
        }
    }
}
