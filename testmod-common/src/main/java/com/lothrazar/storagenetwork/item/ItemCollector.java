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
import com.lothrazar.storagenetwork.util.UtilInventory;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public class ItemCollector extends Item {
    
    public static final String NBT_BOUND = "bound";
    
    public ItemCollector(Properties properties) {
        super(properties.stacksTo(1));
    }
    
    protected ItemStack findAmmo(Player player, Item item) {
        //is curios installed? doesnt matter this is safe
        Triple<String, Integer, ItemStack> remote = UtilInventory.getCurioRemote(player, item);
        return remote.getRight();
    }
    
    // not subscribe, called from SsnEvents.java 
    public void onEntityItemPickupEvent(Player player, ItemEntity entity, ItemStack stack) {
        if (entity != null && !stack.isEmpty()) {
            Level world = player.level;
            DimPos dp = DimPos.getPosStored(this.findAmmo(player, this));
            if (dp != null && !world.isClientSide) {
                ServerLevel serverTargetWorld = DimPos.stringDimensionLookup(dp.getDimension(), world.getServer());
                if (serverTargetWorld == null) {
                    StorageNetwork.LOGGER.error("Missing dimension key " + dp.getDimension());
                    return;
                }
                BlockEntity tile = serverTargetWorld.getBlockEntity(dp.getBlockPos());
                if (tile instanceof TileMain) {
                    TileMain network = (TileMain) tile;
                    // Create a new reference to the stack, try to insert that into the
                    // network, then change the original stack size so the player picks up
                    // only what remains, if anything.
                    int countUnmoved = network.insertStack(stack.copy(), false);
                    stack.setCount(countUnmoved);
                    // We still want to play the pickup sound, even if Minecraft silently
                    // deletes the stack we just emptied.
                    if (countUnmoved == 0) {
                        UtilTileEntity.playSoundFromServer((ServerPlayer) player, SoundEvents.ITEM_PICKUP, 0.2F);
                    }
                } else {
                    StorageNetwork.LOGGER.error("item.remote.notfound");
                }
            }
        }
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
        }
        return InteractionResult.PASS;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        TranslatableComponent t = new TranslatableComponent(getDescriptionId() + ".tooltip");
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
