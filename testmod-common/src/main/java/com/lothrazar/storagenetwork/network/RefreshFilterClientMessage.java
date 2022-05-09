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

import com.lothrazar.storagenetwork.block.cable.export.GuiCableExportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.GuiCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.linkfilter.GuiCableFilter;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Refresh the current screen with large data set of stacks.
 * <p>
 * Used by Containers displaying network inventory as well as most other packets that perform small actions
 */
public class RefreshFilterClientMessage {
    
    private int size;
    private List<ItemStack> stacks;
    
    private RefreshFilterClientMessage() {
    }
    
    public RefreshFilterClientMessage(List<ItemStack> stacks) {
        super();
        this.stacks = stacks;
        size = stacks.size();
    }
    
    public static void handle(RefreshFilterClientMessage message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            //TODO: optimize with base class or interface
            if (Minecraft.getInstance().screen instanceof GuiCableFilter) {
                GuiCableFilter gui = (GuiCableFilter) Minecraft.getInstance().screen;
                gui.setFilterItems(message.stacks);
            }
            if (Minecraft.getInstance().screen instanceof GuiCableImportFilter) {
                GuiCableImportFilter gui = (GuiCableImportFilter) Minecraft.getInstance().screen;
                gui.setFilterItems(message.stacks);
            }
            if (Minecraft.getInstance().screen instanceof GuiCableExportFilter) {
                GuiCableExportFilter gui = (GuiCableExportFilter) Minecraft.getInstance().screen;
                gui.setFilterItems(message.stacks);
            }
        });
    }
    
    public static RefreshFilterClientMessage decode(FriendlyByteBuf buf) {
        RefreshFilterClientMessage message = new RefreshFilterClientMessage();
        message.size = buf.readInt();
        message.stacks = new ArrayList<>();
        for (int i = 0; i < message.size; i++) {
            ItemStack stack = ItemStack.of(buf.readNbt());
            stack.setCount(buf.readInt());
            message.stacks.add(stack);
        }
        return message;
    }
    
    public static void encode(RefreshFilterClientMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.size);
        for (ItemStack stack : msg.stacks) {
            buf.writeNbt(stack.save(new CompoundTag()));
            buf.writeInt(stack.getCount());
        }
    }
}
