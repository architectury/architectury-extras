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

package com.lothrazar.storagenetwork.block.request;

import com.lothrazar.storagenetwork.block.BaseBlock;
import com.lothrazar.storagenetwork.network.SortClientMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class BlockRequest extends BaseBlock {
    
    public BlockRequest() {
        super(Material.METAL);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState bs) {
        return RenderShape.MODEL;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileRequest(pos, state);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockentity = worldIn.getBlockEntity(pos);
            if (blockentity instanceof Container) {
                Containers.dropContents(worldIn, pos, (Container) blockentity);
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof TileRequest) {
                TileRequest tile = (TileRequest) tileentity;
                for (ItemStack entry : tile.matrix.values()) {
                    if (!entry.isEmpty()) {
                        Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), entry);
                    }
                }
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }
    
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!world.isClientSide) {
            TileRequest tile = (TileRequest) world.getBlockEntity(pos);
            if (tile.getMain() == null || tile.getMain().getBlockPos() == null) {
                return InteractionResult.PASS;
            }
            //sync
            ServerPlayer sp = (ServerPlayer) player;
            PacketRegistry.INSTANCE.sendToPlayer(sp, new SortClientMessage(pos, tile.isDownwards(), tile.getSort()));
            //end sync
            if (tile instanceof MenuProvider) {
                MenuRegistry.openExtendedMenu(sp, tile, buf -> buf.writeBlockPos(tile.getBlockPos()));
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
        }
        return InteractionResult.SUCCESS;
    }
}
