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

import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.api.IGuiNetwork;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/**
 * Refresh the current screen with large data set of stacks.
 * <p>
 * Used by Containers displaying network inventory as well as most other packets that perform small actions
 */
public class StackRefreshClientMessage {
    
    private final int size;
    private final int csize;
    private final List<ItemStack> stacks;
    private final List<ItemStack> craftableStacks;
    
    public StackRefreshClientMessage(List<ItemStack> stacks, List<ItemStack> craftableStacks) {
        super();
        this.stacks = stacks;
        this.craftableStacks = craftableStacks;
        size = stacks.size();
        csize = craftableStacks.size();
    }
    
    public static void handle(StackRefreshClientMessage message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof IGuiNetwork) {
                IGuiNetwork gui = (IGuiNetwork) mc.screen;
                gui.setStacks(message.stacks);
            }
        });
    }
    
    public static void encode(StackRefreshClientMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.size);
        buf.writeInt(msg.csize);
        for (ItemStack stack : msg.stacks) {
            buf.writeNbt(stack.save(new CompoundTag()));
            buf.writeInt(stack.getCount());
        }
        for (ItemStack stack : msg.craftableStacks) {
            buf.writeNbt(stack.save(new CompoundTag()));
            buf.writeInt(stack.getCount());
        }
    }
    
    public static StackRefreshClientMessage decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        int csize = buf.readInt();
        List<ItemStack> stacks = Lists.newArrayList();
        for (int i = 0; i < size; i++) {
            CompoundTag stacktag = buf.readNbt();
            ItemStack stack = ItemStack.of(stacktag);
            stack.setCount(buf.readInt());
            stacks.add(stack);
        }
        List<ItemStack> craftableStacks = Lists.newArrayList();
        for (int i = 0; i < csize; i++) {
            ItemStack stack = ItemStack.of(buf.readNbt());
            stack.setCount(buf.readInt());
            craftableStacks.add(stack);
        }
        return new StackRefreshClientMessage(stacks, craftableStacks);
    }
}
