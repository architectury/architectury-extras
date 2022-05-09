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

import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import dev.architectury.registry.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UtilTileEntity {
    
    private static final Map<Item, String> modNamesForIds = new HashMap<>();
    public static final int MOUSE_BTN_LEFT = 0;
    public static final int MOUSE_BTN_RIGHT = 1;
    public static final int MOUSE_BTN_MIDDLE_CLICK = 2;
    
    public static void playSoundFromServer(ServerPlayer entityIn, SoundEvent soundIn, float volume) {
        if (soundIn == null || entityIn == null) {
            return;
        }
        entityIn.connection.send(new ClientboundSoundPacket(soundIn, SoundSource.PLAYERS, entityIn.xOld, entityIn.yOld, entityIn.zOld, volume, 1.0F));
    }
    
    public static void chatMessage(Player player, String message) {
        if (player.level.isClientSide) {
            player.sendMessage(new TranslatableComponent(message), player.getUUID());
        }
    }
    
    public static void statusMessage(Player player, BlockState bs) {
        if (player.level.isClientSide) {
            player.displayClientMessage(new TranslatableComponent(bs.getBlock().getName().getString()), true);
        }
    }
    
    public static void statusMessage(Player player, String message) {
        if (player.level.isClientSide) {
            player.displayClientMessage(new TranslatableComponent(message), true);
        }
    }
    
    public static String lang(String message) {
        TranslatableComponent t = new TranslatableComponent(message);
        return t.getContents();
    }
    
    /**
     * This can only be called on the server side! It returns the Main tile entity for the given connectable.
     *
     * @param connectable
     * @return
     */
    public static TileMain getTileMainForConnectable(IConnectable connectable) {
        if (connectable == null || connectable.getMainPos() == null) {
            return null;
        }
        return connectable.getMainPos().getTileEntity(TileMain.class);
    }
    
    /**
     * Get mod id for item, but use cache to save time just in case it helps
     *
     * @param theitem
     * @return
     */
    public static String getModNameForItem(Item theitem) {
        if (modNamesForIds.containsKey(theitem)) {
            return modNamesForIds.get(theitem);
        }
        String modId = Registries.getId(theitem, Registry.ITEM_REGISTRY).getNamespace();
        String lowercaseModId = modId.toLowerCase(Locale.ENGLISH);
        modNamesForIds.put(theitem, lowercaseModId);
        return lowercaseModId;
    }
}
