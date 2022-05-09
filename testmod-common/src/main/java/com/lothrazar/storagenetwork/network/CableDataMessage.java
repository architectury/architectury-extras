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
import com.lothrazar.storagenetwork.block.cable.linkfilter.ContainerCableFilter;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableLink;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class CableDataMessage {
    
    public enum CableMessageType {
        SYNC_DATA, IMPORT_FILTER, SAVE_FITLER
    }
    
    private boolean isAllowlist;
    private final int id;
    private int value = 0;
    private ItemStack stack = ItemStack.EMPTY;
    
    public CableDataMessage(int id) {
        this.id = id;
    }
    
    public CableDataMessage(int id, int value, boolean is) {
        this(id);
        this.value = value;
        this.isAllowlist = is;
    }
    
    public CableDataMessage(int id, int value, ItemStack mystack) {
        this(id);
        this.value = value;
        stack = mystack;
    }
    
    @Override
    public String toString() {
        return "CableDataMessage{" +
                "isAllowlist=" + isAllowlist +
                ", id=" + id +
                ", value=" + value +
                ", stack=" + stack +
                '}';
    }
    
    public static void handle(CableDataMessage message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getPlayer();
            CapabilityConnectableLink link = null;
            ContainerCableFilter container = (ContainerCableFilter) player.containerMenu;
            if (container == null || container.cap == null) {
                return;
            }
            link = container.cap;
            TileMain root = UtilTileEntity.getTileMainForConnectable(link.connectable);
            CableMessageType type = CableMessageType.values()[message.id];
            switch (type) {
                case IMPORT_FILTER:
                    link.getFilter().clear();
                    int targetSlot = 0;
                    for (ItemStack filterSuggestion : link.getStoredStacks(false)) {
                        // Ignore stacks that are already filtered
                        if (link.getFilter().exactStackAlreadyInList(filterSuggestion)) {
                            continue;
                        }
                        //int over max
                        try {
                            link.getFilter().set(targetSlot, filterSuggestion.copy());
                            targetSlot++;
                            if (targetSlot >= link.getFilter().size()) {
                                continue;
                            }
                        } catch (Exception ex) {
                            StorageNetwork.LOGGER.error("Exception saving filter slot ", message);
                        }
                    }
                    PacketRegistry.INSTANCE.sendToPlayer(player, new RefreshFilterClientMessage(link.getFilter().getStacks()));
                    break;
                case SYNC_DATA:
                    link.setPriority(link.getPriority() + message.value);
                    link.getFilter().setIsAllowlist(message.isAllowlist);
                    if (root != null) {
                        root.clearCache();
                    }
                    break;
                case SAVE_FITLER:
                    link.setFilter(message.value, message.stack.copy());
                    break;
            }
            container.tile.setChanged();
            player.connection.send(container.tile.getUpdatePacket());
        });
    }
    
    public static void encode(CableDataMessage msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.id);
        buffer.writeInt(msg.value);
        buffer.writeBoolean(msg.isAllowlist);
        buffer.writeNbt(msg.stack.save(new CompoundTag()));
    }
    
    public static CableDataMessage decode(FriendlyByteBuf buffer) {
        CableDataMessage c = new CableDataMessage(buffer.readInt(), buffer.readInt(), buffer.readBoolean());
        c.stack = ItemStack.of(buffer.readNbt());
        return c;
    }
}
