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
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ItemBuilder extends Item {
    
    public static final String NBTBLOCKSTATE = "blockstate";
    
    public ItemBuilder(Properties properties) {
        super(properties.stacksTo(1));
    }
    
    public static void setBlockState(ItemStack wand, BlockState target) {
        CompoundTag encoded = NbtUtils.writeBlockState(target);
        wand.getOrCreateTag().put(NBTBLOCKSTATE, encoded);
    }
    
    public static BlockState getBlockState(ItemStack wand) {
        if (!wand.getOrCreateTag().contains(NBTBLOCKSTATE)) {
            return null;
        }
        return NbtUtils.readBlockState(wand.getOrCreateTag().getCompound(NBTBLOCKSTATE));
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionHand hand = context.getHand();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockPos buildAt = pos.relative(context.getClickedFace());
        if (world.getBlockEntity(pos) instanceof TileMain) {
            ItemStack stack = player.getItemInHand(hand);
            CompoundTag tag = stack.getOrCreateTag();
            DimPos.putPos(stack, pos, world);
            stack.setTag(tag);
            UtilTileEntity.statusMessage(player, "item.remote.connected");
            return InteractionResult.SUCCESS;
        } else if (world.isEmptyBlock(buildAt) || world.getBlockState(buildAt).getMaterial().isLiquid()) {
            player.swing(hand);
            ItemStack stack = player.getItemInHand(hand);
            //succeed or fail
            DimPos dp = DimPos.getPosStored(stack);
            if (dp != null && hand == InteractionHand.MAIN_HAND && !world.isClientSide) {
                ServerLevel serverTargetWorld = DimPos.stringDimensionLookup(dp.getDimension(), world.getServer());
                if (serverTargetWorld == null) {
                    StorageNetwork.LOGGER.error("Missing dimension key " + dp.getDimension());
                    return InteractionResult.PASS;
                }
                BlockEntity tile = serverTargetWorld.getBlockEntity(dp.getBlockPos());
                BlockState targetState = ItemBuilder.getBlockState(stack);
                if (tile instanceof TileMain && targetState != null) {
                    TileMain network = (TileMain) tile;
                    ItemStackMatcher matcher = new ItemStackMatcher(new ItemStack(targetState.getBlock()), false, false);
                    ItemStack found = network.request(matcher, 1, true);
                    //SIMULATED, see if materials are available
                    if (!found.isEmpty()) {
                        // yes materials are available
                        boolean success = placeStateSafe(world, player, buildAt, targetState);
                        if (success) {
                            network.request(matcher, 1, false);
                            //NOT SIMULATED, extract item from network
                        }
                    } else {
                        player.displayClientMessage(Component.translatable("item.remote.notfound.item"), true);
                    }
                } else {
                    player.displayClientMessage(Component.translatable("item.remote.notfound"), true);
                }
            }
        }
        //else something non-air and non-liquid is in the way, flower etc
        return InteractionResult.PASS;
    }
    
    private boolean placeStateSafe(Level world, Player player, BlockPos placePos, BlockState placeState) {
        BlockState stateHere = world.getBlockState(placePos);
        if (stateHere.getBlock() == Blocks.AIR || stateHere.getMaterial().isLiquid()) {
            return world.setBlock(placePos, placeState, 3);
        }
        return false;
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
            } // block state?
            BlockState target = ItemBuilder.getBlockState(stack);
            if (target != null) {
                String block = target.getBlock().getDescriptionId();
                t = Component.translatable(block);
                t.withStyle(ChatFormatting.AQUA);
                tooltip.add(t);
            } else {
                //if it has a network connection but no blockstate saved, then
                t = Component.translatable(getDescriptionId() + ".blockstate");
                t.withStyle(ChatFormatting.AQUA);
                tooltip.add(t);
            }
        }
    }
    
    public static EventResult onLeftClickBlock(Player player, InteractionHand hand, BlockPos blockPos, Direction direction) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() == SsnRegistry.BUILDER_REMOTE.get()) {
            Level world = player.getCommandSenderWorld();
            BlockState target = world.getBlockState(blockPos);
            ItemBuilder.setBlockState(held, target);
            UtilTileEntity.statusMessage(player, target);
            return EventResult.interruptFalse();
        }
        return EventResult.pass();
    }
}
