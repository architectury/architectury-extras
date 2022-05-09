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

import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.ITileNetworkSync;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public class SortClientMessage {
    
    //sync sort data TO client gui FROM server
    private BlockPos pos;
    private boolean direction;
    private EnumSortType sort;
    
    private SortClientMessage() {
    }
    
    public SortClientMessage(BlockPos pos, boolean direction, EnumSortType sort) {
        this.pos = pos;
        this.direction = direction;
        this.sort = sort;
    }
    
    public static void handle(SortClientMessage message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            Minecraft mc = Minecraft.getInstance();
            BlockEntity tileEntity = mc.level.getBlockEntity(message.pos);
            if (tileEntity instanceof ITileNetworkSync) {
                ITileNetworkSync ts = (ITileNetworkSync) tileEntity;
                ts.setDownwards(message.direction);
                ts.setSort(message.sort);
                tileEntity.setChanged();
            }
        });
    }
    
    public static SortClientMessage decode(FriendlyByteBuf buf) {
        SortClientMessage message = new SortClientMessage();
        message.direction = buf.readBoolean();
        int sort = buf.readInt();
        message.sort = EnumSortType.values()[sort];
        message.pos = buf.readBlockPos();
        return message;
    }
    
    public static void encode(SortClientMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.direction);
        buf.writeInt(msg.sort.ordinal());
        if (msg.pos != null) {
            buf.writeBlockPos(msg.pos);
        } else { // to avoid null values // inconsistent buffer size 
            buf.writeBlockPos(BlockPos.ZERO);
        }
    }
}
