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

package com.lothrazar.storagenetwork.util;

import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import dev.architectury.transfer.ResourceView;
import dev.architectury.transfer.TransferAction;
import dev.architectury.transfer.TransferHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Objects;

public class UtilInventory {
    
    /**
     * First check curios. Then ender chest, Then player inventory. Then left/right hands
     *
     * @param player
     * @param remote
     * @return
     */
    public static Triple<String, Integer, ItemStack> getCurioRemote(Player player, Item remote) {
        Triple<String, Integer, ItemStack> stackFound = Triple.of("", -1, ItemStack.EMPTY);
//        if (Platform.isModLoaded("curios")) {
//            //check curios slots
//            final ImmutableTriple<String, Integer, ItemStack> equipped = CuriosApi.getCuriosHelper().findEquippedCurio(remote, player).orElse(null);
//            if (equipped != null && isRemoteWithData(equipped.right, remote)) {
//                //success: try to insert items to network thru this remote 
//                return Triple.of("curios", equipped.middle, equipped.right);
//            }
//        }
        //not curios, check others
        for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
            ItemStack temp = player.getEnderChestInventory().getItem(i);
            if (isRemoteWithData(temp, remote)) {
                return Triple.of("ender", i, temp);
            }
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack temp = player.getInventory().getItem(i);
            if (isRemoteWithData(temp, remote)) {
                return Triple.of("player", i, temp);
            }
        }
        //default
        if (isRemoteWithData(player.getOffhandItem(), remote)) {
            return Triple.of("offhand", -1, player.getOffhandItem());
        }
        if (isRemoteWithData(player.getMainHandItem(), remote)) {
            return Triple.of("hand", -1, player.getMainHandItem());
        }
        return stackFound;
    }
    
    private static boolean isRemoteWithData(ItemStack stack, Item remote) {
        //if it has a tag, assume pos to network is valid
        return stack.getItem() == remote && stack.hasTag();
    }
    
    public static String formatLargeNumber(int size) {
        if (size < Math.pow(10, 3)) {
            return size + "";
        } else if (size < Math.pow(10, 6)) {
            //      float r = (size) / 1000.0F;
            int rounded = Math.round(size / 1000.0F); //so 1600 => 1.6 and then rounded to become 2.
            return rounded + "K";
        } else if (size < Math.pow(10, 9)) {
            int rounded = Math.round(size / (float) Math.pow(10, 6));
            return rounded + "M";
        } else if (size < Math.pow(10, 12)) {
            int rounded = Math.round(size / (float) Math.pow(10, 9));
            return rounded + "B";
        }
        return size + "";
    }
    
    public static int countHowMany(TransferHandler<ItemStack> inv, ItemStack stackIn) {
        return inv.getWithContents(resourceViews -> {
            int found = 0;
            for (ResourceView<ItemStack> view : resourceViews) {
                if (UtilInventory.canStack(view.getResource(), stackIn)) {
                    found += view.getResource().getCount();
                }
            }
            return found;
        });
    }
    
    public static int containsAtLeastHowManyNeeded(TransferHandler<ItemStack> inv, ItemStack stackIn, int minimumCount) {
        int found = countHowMany(inv, stackIn);
        //do you have all 4? or do you need 2 still
        if (found >= minimumCount) {
            return 0;
        }
        return minimumCount - found;
    }
    
    public static ItemStack extractItem(TransferHandler<ItemStack> inv, ItemStackMatcher fil, int num, boolean simulate) {
        if (inv == null || fil == null) {
            return ItemStack.EMPTY;
        }
        return inv.extract(fil::match, num, simulate ? TransferAction.SIMULATE : TransferAction.ACT);
//        int extracted = 0;
//        for (int i = 0; i < inv.getSlots(); i++) {
//            ItemStack slot = inv.getStackInSlot(i);
//            if (fil.match(slot)) {
//                ItemStack ex = inv.extractItem(i, 1, simulate);
//                if (!ex.isEmpty()) {
//                    extracted++;
//                    if (extracted == num) {
//                        return ItemStackHooks.copyWithCount(slot, num);
//                    } else {
//                        i--;
//                    }
//                }
//            }
//        }
//        return ItemStack.EMPTY;
    }
    
    public static void dropItem(Level world, BlockPos pos, ItemStack stack) {
        if (pos == null || world.isClientSide || stack.isEmpty()) {
            return;
        }
        world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack));
    }
    
    public static boolean canStack(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) {
            return stack1.isEmpty() && stack2.isEmpty();
        }
        return stack1.getItem() == stack2.getItem() && Objects.equals(stack1.getTag(), stack2.getTag());
    }
}
