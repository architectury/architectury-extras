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

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RequestMessage {
    
    private int mouseButton = 0;
    private ItemStack stack = ItemStack.EMPTY;
    private boolean shift;
    private boolean ctrl;
    
    @Override
    public String toString() {
        return "RequestMessage [mouseButton=" + mouseButton + ", shift=" + shift + ", ctrl=" + ctrl + ", stack=" + stack.toString() + "]";
    }
    
    public RequestMessage() {
    }
    
    public RequestMessage(int id, ItemStack stackIn, boolean shift, boolean ctrl) {
        mouseButton = id;
        this.stack = stackIn.copy();
        if (this.stack.getCount() > 64) {
            this.stack.setCount(64); //important or it will be killed by a filter
        }
        this.shift = shift;
        this.ctrl = ctrl;
    }
    
    public static void handle(RequestMessage message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getPlayer();
            TileMain root = null;
            ContainerNetwork ctr = null;
            if (player.containerMenu instanceof ContainerNetwork) {
                ctr = (ContainerNetwork) player.containerMenu;
                root = ctr.getTileMain();
            } else {
                StorageNetwork.log("Bad container");
            }
            if (root == null) {
                //maybe the table broke after doing this, rare case
                StorageNetwork.log("Request message cancelled, null tile");
                return;
            }
            int in = root.getAmount(new ItemStackMatcher(message.stack, false, true));
            ItemStack stack;
            boolean isLeftClick = message.mouseButton == UtilTileEntity.MOUSE_BTN_LEFT;
            boolean isRightClick = message.mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT;
            int sizeRequested = 0;
            if (message.ctrl) {
                sizeRequested = 1;
            } else if (isLeftClick) {
                sizeRequested = message.stack.getMaxStackSize();
            } else if (isRightClick) {
                sizeRequested = Math.min(message.stack.getMaxStackSize() / 2, in / 2);
            }
            sizeRequested = Math.max(sizeRequested, 1);
            boolean ore = false;
            boolean nbt = true;
            //try NBT first
            stack = root.request(
                    new ItemStackMatcher(message.stack, ore, nbt),
                    sizeRequested, false);
            if (stack.isEmpty()) {
                //try again with NBT as false, ONLY if true didnt work
                nbt = false;
                stack = root.request(
                        new ItemStackMatcher(message.stack, ore, nbt),
                        sizeRequested, false);
            }
            if (!stack.isEmpty()) {
                if (message.shift) {
                    ItemStackHooks.giveItem(player, stack);
                } else {
                    //when player TAKES an item, go here
                    player.containerMenu.setCarried(stack);
                    PacketRegistry.INSTANCE.sendToPlayer(player, new StackResponseClientMessage(stack));
                }
            }
            List<ItemStack> list = root.getSortedStacks();
            PacketRegistry.INSTANCE.sendToPlayer(player, new StackRefreshClientMessage(list, new ArrayList<>()));
            player.containerMenu.broadcastChanges();
        });
    }
    
    public static RequestMessage decode(FriendlyByteBuf buf) {
        RequestMessage msg = new RequestMessage();
        msg.mouseButton = buf.readInt();
        msg.stack = ItemStack.of(buf.readNbt());
        msg.shift = buf.readBoolean();
        msg.ctrl = buf.readBoolean();
        return msg;
    }
    
    public static void encode(RequestMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.mouseButton);
        buf.writeNbt(msg.stack.save(new CompoundTag()));
        buf.writeBoolean(msg.shift);
        buf.writeBoolean(msg.ctrl);
    }
}
