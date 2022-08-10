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

package com.lothrazar.storagenetwork.item;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ItemPicker extends Item {
    
    public static final String NBT_BOUND = "bound";
    
    public ItemPicker(Properties properties) {
        super(properties.stacksTo(1));
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionHand hand = context.getHand();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (world.getBlockEntity(pos) instanceof TileMain) {
            ItemStack stack = player.getItemInHand(hand);
            DimPos.putPos(stack, pos, world);
            UtilTileEntity.statusMessage(player, "item.remote.connected");
            return InteractionResult.SUCCESS;
        } else {
            ItemStack stack = player.getItemInHand(hand);
            DimPos dp = DimPos.getPosStored(stack);
            if (dp != null && hand == InteractionHand.MAIN_HAND && !world.isClientSide) {
                ServerLevel serverTargetWorld = DimPos.stringDimensionLookup(dp.getDimension(), world.getServer());
                if (serverTargetWorld == null) {
                    StorageNetwork.LOGGER.error("Missing dimension key " + dp.getDimension());
                    return InteractionResult.PASS;
                }
                BlockEntity tile = serverTargetWorld.getBlockEntity(dp.getBlockPos());
                if (tile instanceof TileMain) {
                    TileMain network = (TileMain) tile;
                    BlockState bs = world.getBlockState(pos);
                    ItemStackMatcher matcher = new ItemStackMatcher(new ItemStack(bs.getBlock()), false, false);
                    int size = player.isCrouching() ? 1 : 64;
                    ItemStack found = network.request(matcher, size, false);
                    if (!found.isEmpty()) {
                        player.displayClientMessage(Component.translatable("item.remote.found"), true);
                        //using add will bypass the collector so try if possible
                        if (!player.addItem(found)) {
                            player.spawnAtLocation(found);
                        }
                    } else {
                        player.displayClientMessage(Component.translatable("item.remote.notfound.item"), true);
                    }
                } else {
                    //no main
                    player.displayClientMessage(Component.translatable("item.remote.notfound"), true);
                }
            }
        }
        return InteractionResult.PASS;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        MutableComponent t;
        t = Component.translatable(getDescriptionId() + ".tooltip");
        t.withStyle(ChatFormatting.GRAY);
        tooltip.add(t);
        if (stack.hasTag()) {
            DimPos dp = DimPos.getPosStored(stack);
            if (dp != null) {
                tooltip.add(dp.makeTooltip());
            }
        }
    }
}
