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

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Used by InsertMessage and RequestMessage as a response back to the client
 */
public class StackResponseClientMessage {
    
    private ItemStack stack;
    
    private StackResponseClientMessage() {
    }
    
    StackResponseClientMessage(ItemStack a) {
        stack = a;
    }
    
    public static void handle(StackResponseClientMessage message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            Minecraft.getInstance().player.containerMenu.setCarried(message.stack); // .setCarried(message.stack);
        });
    }
    
    public static StackResponseClientMessage decode(FriendlyByteBuf buf) {
        StackResponseClientMessage message = new StackResponseClientMessage();
        message.stack = ItemStack.of(buf.readNbt());
        return message;
    }
    
    public static void encode(StackResponseClientMessage msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.stack.save(new CompoundTag()));
    }
}
