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
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkCraftingRemote;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkRemote;
import com.lothrazar.storagenetwork.item.remote.ItemStorageCraftingRemote;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public class SettingsSyncMessage {
    
    private BlockPos pos;
    private boolean direction;
    private EnumSortType sort;
    private boolean targetTileEntity;
    private boolean jeiSync;
    private boolean autoFocus;
    
    private SettingsSyncMessage() {
    }
    
    public SettingsSyncMessage(BlockPos pos, boolean direction, EnumSortType sort, boolean jeiSync, boolean autoFocus) {
        this.pos = pos;
        this.direction = direction;
        this.sort = sort;
        this.jeiSync = jeiSync;
        this.autoFocus = autoFocus;
    }
    
    public static void handle(SettingsSyncMessage message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getPlayer();
            if (message.targetTileEntity) {
                BlockEntity tileEntity = player.level.getBlockEntity(message.pos);
                if (tileEntity instanceof ITileNetworkSync) {
                    ITileNetworkSync tile = (ITileNetworkSync) tileEntity;
                    tile.setSort(message.sort);
                    tile.setDownwards(message.direction);
                    tile.setJeiSearchSynced(message.jeiSync);
                    tile.setAutoFocus(message.autoFocus);
                    tileEntity.setChanged();
                }
            } else if (player.containerMenu instanceof ContainerNetworkCraftingRemote remoteContainer) {
                ItemStack stackPlayerHeld = remoteContainer.getRemote();
                if (stackPlayerHeld.getItem() instanceof ItemStorageCraftingRemote) {
                    ItemStorageCraftingRemote.setSort(stackPlayerHeld, message.sort);
                    ItemStorageCraftingRemote.setDownwards(stackPlayerHeld, message.direction);
                    ItemStorageCraftingRemote.setJeiSearchSynced(stackPlayerHeld, message.jeiSync);
                    ItemStorageCraftingRemote.setAutoFocus(stackPlayerHeld, message.autoFocus);
                }
            } else if (player.containerMenu instanceof ContainerNetworkRemote rcc) { //TODO eww this bufix is ugly for both remotes, shared getremote or shared sync
                ItemStack stackPlayerHeld = rcc.getRemote();
                if (stackPlayerHeld.getItem() instanceof ItemStorageCraftingRemote) {
                    ItemStorageCraftingRemote.setSort(stackPlayerHeld, message.sort);
                    ItemStorageCraftingRemote.setDownwards(stackPlayerHeld, message.direction);
                    ItemStorageCraftingRemote.setJeiSearchSynced(stackPlayerHeld, message.jeiSync);
                    System.out.println("Save autofocus for non crafting remote " + message.autoFocus + " " + rcc.getRemote());
                    ItemStorageCraftingRemote.setAutoFocus(stackPlayerHeld, message.autoFocus);
                }
            }
        });
    }
    
    public static SettingsSyncMessage decode(FriendlyByteBuf buf) {
        SettingsSyncMessage message = new SettingsSyncMessage();
        message.direction = buf.readBoolean();
        int sort = buf.readInt();
        message.sort = EnumSortType.values()[sort];
        message.targetTileEntity = buf.readBoolean();
        message.pos = buf.readBlockPos();
        message.jeiSync = buf.readBoolean();
        message.autoFocus = buf.readBoolean();
        return message;
    }
    
    public static void encode(SettingsSyncMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.direction);
        buf.writeInt(msg.sort.ordinal());
        if (msg.pos != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(msg.pos);
        } else { // to avoid null values // inconsistent buffer size
            buf.writeBoolean(false);
            buf.writeBlockPos(BlockPos.ZERO);
        }
        buf.writeBoolean(msg.jeiSync);
        buf.writeBoolean(msg.autoFocus);
    }
}
