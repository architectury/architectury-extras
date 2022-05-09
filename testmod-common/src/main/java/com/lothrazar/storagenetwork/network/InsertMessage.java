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
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class InsertMessage {
    
    private int dim, mouseButton;
    
    public InsertMessage(int dim, int buttonID) {
        this.dim = dim;
        this.mouseButton = buttonID;
    }
    
    private InsertMessage() {
    }
    
    public static void handle(InsertMessage message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getPlayer();
            TileMain root = null;
            if (player.containerMenu instanceof ContainerNetwork) {
                root = ((ContainerNetwork) player.containerMenu).getTileMain();
            }
            int rest;
            ItemStack send = ItemStack.EMPTY;
            ItemStack stack = player.containerMenu.getCarried();
            if (message.mouseButton == UtilTileEntity.MOUSE_BTN_LEFT) {
                rest = root.insertStack(stack, false);
                if (rest != 0) {
                    send = ItemStackHooks.copyWithCount(stack, rest);
                }
            } else if (message.mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
                ItemStack stack1 = stack.copy();
                stack1.setCount(1);
                stack.shrink(1);
                rest = root.insertStack(stack1, false) + stack.getCount();
                if (rest != 0) {
                    send = ItemStackHooks.copyWithCount(stack, rest);
                }
            }
            player.containerMenu.setCarried(send);
            //
            PacketRegistry.INSTANCE.sendToPlayer(player, new StackResponseClientMessage(send));
            List<ItemStack> list = root.getStacks();
            PacketRegistry.INSTANCE.sendToPlayer(player, new StackRefreshClientMessage(list, new ArrayList<>()));
            player.containerMenu.broadcastChanges();
        });
    }
    
    public static InsertMessage decode(FriendlyByteBuf buf) {
        InsertMessage message = new InsertMessage();
        message.dim = buf.readInt();
        message.mouseButton = buf.readInt();
        return message;
    }
    
    public static void encode(InsertMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.dim);
        buf.writeInt(msg.mouseButton);
    }
}
