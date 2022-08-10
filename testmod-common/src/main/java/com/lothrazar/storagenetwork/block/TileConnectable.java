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

import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.capability.CapabilityConnectable;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapsProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class for Cable, Control, Request
 */
public class TileConnectable extends BlockEntity implements StorageNetworkCapsProvider {
    
    private final CapabilityConnectable connectable;
    
    public TileConnectable(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
        connectable = new CapabilityConnectable();
    }
    
    @Override
    public void setChanged() {
        super.setChanged();
        // super.setPosition(posIn);
        //   StorageNetwork.log("TILE CONNECTABLE :: SET POS on the capability" + posIn + "?" + world);
        connectable.setPos(new DimPos(level, worldPosition));
    }
    
    @Override
    public void load(CompoundTag compound) {
        if (compound.contains("connectable")) {
            connectable.deserializeNBT(compound.getCompound("connectable"));
        }
        super.load(compound);
    }
    
    @Override
    public void saveAdditional(CompoundTag compound) {
        compound.put("connectable", connectable.serializeNBT());
        super.saveAdditional(compound);
    }
    
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this); //new ClientboundBlockEntityDataPacket(worldPosition, 0, syncData);
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = new CompoundTag();
        this.saveAdditional(updateTag);
        return updateTag;
    }
    
//    @Override
//    public void onChunkUnloaded() {
//        super.onChunkUnloaded();
//        if (StorageNetwork.CONFIG.doReloadOnChunk() && connectable != null && connectable.getMainPos() != null) {
//            try {
//                TileMain maybe = UtilTileEntity.getTileMainForConnectable(connectable);
//                if (maybe != null) {
//                    maybe.refreshNetwork();
//                }
//            } catch (Exception e) {
//                StorageNetwork.LOGGER.info("Error on chunk unload " + e);
//            }
//        }
//    }
    
    @Override
    public CapabilityConnectable getConnectable() {
        return connectable;
    }
    
    public DimPos getMain() {
        if (connectable == null) {
            return null;
        }
        return connectable.getMainPos();
    }
}
