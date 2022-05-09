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

package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClearRecipeMessage {
    
    public static void handle(ClearRecipeMessage message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getPlayer();
            ClearRecipeMessage.clearContainerRecipe(player, true);
        });
    }
    
    public static ClearRecipeMessage decode(FriendlyByteBuf buf) {
        return new ClearRecipeMessage();
    }
    
    public static void encode(ClearRecipeMessage msg, FriendlyByteBuf buf) {
    }
    
    /**
     * Should be in a public util.
     * <p>
     * Clears recipe and puts ingredients back in the network. If possible.
     * <p>
     * May stop partway and leave items in if network is disconnected.
     *
     * @param player
     * @param doRefresh
     */
    static void clearContainerRecipe(ServerPlayer player, boolean doRefresh) {
        if (player.containerMenu instanceof ContainerNetwork) {
            ContainerNetwork container = (ContainerNetwork) player.containerMenu;
            CraftingContainer craftMatrix = container.getCraftMatrix();
            TileMain root = container.getTileMain();
            for (int i = 0; i < 9; i++) {
                if (root == null) {
                    break;
                }
                ItemStack stackInSlot = craftMatrix.getItem(i);
                if (stackInSlot.isEmpty()) {
                    continue;
                }
                int numBeforeInsert = stackInSlot.getCount();
                int remainingAfter = root.insertStack(stackInSlot.copy(), false);
                if (numBeforeInsert == remainingAfter) {
                    continue;
                }
                if (remainingAfter == 0) {
                    craftMatrix.setItem(i, ItemStack.EMPTY);
                } else {
                    craftMatrix.setItem(i, ItemStackHooks.copyWithCount(stackInSlot, remainingAfter));
                }
            }
            if (doRefresh) {
                List<ItemStack> list = root.getStacks();
                PacketRegistry.INSTANCE.sendToPlayer(player, new StackRefreshClientMessage(list, new ArrayList<>()));
                container.broadcastChanges();
            }
        }
    }
}
