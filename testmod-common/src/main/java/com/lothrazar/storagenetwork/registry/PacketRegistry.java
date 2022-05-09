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

package com.lothrazar.storagenetwork.registry;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.network.*;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.resources.ResourceLocation;

public class PacketRegistry {
    //??https://wiki.mcjty.eu/modding/index.php?title=Tut14_Ep10 
    public static final NetworkChannel INSTANCE =NetworkChannel.create(new ResourceLocation(StorageNetwork.MODID, "main_channel"));
    
    public static void init() {
        //https://gist.github.com/williewillus/353c872bcf1a6ace9921189f6100d09a
        INSTANCE.register(CableDataMessage.class, CableDataMessage::encode, CableDataMessage::decode, CableDataMessage::handle);
        INSTANCE.register(CableIOMessage.class, CableIOMessage::encode, CableIOMessage::decode, CableIOMessage::handle);
        INSTANCE.register(StackRefreshClientMessage.class, StackRefreshClientMessage::encode, StackRefreshClientMessage::decode, StackRefreshClientMessage::handle);
        INSTANCE.register(InsertMessage.class, InsertMessage::encode, InsertMessage::decode, InsertMessage::handle);
        INSTANCE.register(RequestMessage.class, RequestMessage::encode, RequestMessage::decode, RequestMessage::handle);
        INSTANCE.register(ClearRecipeMessage.class, ClearRecipeMessage::encode, ClearRecipeMessage::decode, ClearRecipeMessage::handle);
        INSTANCE.register(SettingsSyncMessage.class, SettingsSyncMessage::encode, SettingsSyncMessage::decode, SettingsSyncMessage::handle);
        INSTANCE.register(RecipeMessage.class, RecipeMessage::encode, RecipeMessage::decode, RecipeMessage::handle);
        //    INSTANCE.registerMessage(CableFilterMessage.class, CableFilterMessage::encode, CableFilterMessage::decode, CableFilterMessage::handle);
        INSTANCE.register(CableLimitMessage.class, CableLimitMessage::encode, CableLimitMessage::decode, CableLimitMessage::handle);
        INSTANCE.register(StackResponseClientMessage.class, StackResponseClientMessage::encode, StackResponseClientMessage::decode, StackResponseClientMessage::handle);
        INSTANCE.register(RefreshFilterClientMessage.class, RefreshFilterClientMessage::encode, RefreshFilterClientMessage::decode, RefreshFilterClientMessage::handle);
        INSTANCE.register(SortClientMessage.class, SortClientMessage::encode, SortClientMessage::decode, SortClientMessage::handle);
        INSTANCE.register(KeybindCurioMessage.class, KeybindCurioMessage::encode, KeybindCurioMessage::decode, KeybindCurioMessage::handle);
    }
}
