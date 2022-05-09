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

package com.lothrazar.storagenetwork.block.exchange;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import dev.architectury.transfer.TransferHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileExchange extends TileConnectable {
    
    private ExchangeItemStackHandler itemHandler;
    
    public TileExchange(BlockPos pos, BlockState state) {
        super(SsnRegistry.EXCHANGETILE.get(), pos, state);
        itemHandler = new ExchangeItemStackHandler();
    }
    
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
    }
    
    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
    }
    
    @Override
    @Nullable
    public TransferHandler<ItemStack> getItemTransfer() {
        try {
            IConnectable capabilityConnectable = getConnectable();
            DimPos m = getMain();
            if (capabilityConnectable != null && m != null
                    && itemHandler != null &&
                    itemHandler.tileMain == null) {
                TileMain tileMain = m.getTileEntity(TileMain.class);
                if (tileMain != null) {
                    itemHandler.setMain(tileMain);
                }
            }
            return itemHandler;
        } catch (Exception e) {
            StorageNetwork.LOGGER.error("Exchange caught error from a mod", e);
        }
        
        return null;
    }
    
    private void tick() {
        if (this.itemHandler != null && getLevel().getGameTime() % StorageNetwork.CONFIG.refreshTicks() == 0) {
            this.itemHandler.update();
        }
    }
    
    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileExchange tile) {
        tile.tick();
    }
    
    public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileExchange tile) {
        tile.tick();
    }
}
