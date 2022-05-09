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
import com.lothrazar.storagenetwork.util.UtilInventory;
import dev.architectury.networking.NetworkManager;
import dev.architectury.transfer.item.ItemTransfer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RecipeMessage {
    
    /** @formatter:off
   * Sample data structure can have list of items for each slot (example: ore dictionary)
   * {
   *  s0:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s1:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s2:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s3:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s4:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s5:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s6:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s7:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s8:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}]
   *  }
   * @formatter:on
     */
    private CompoundTag nbt;
    private int index = 0;
    
    private RecipeMessage() {
    }
    
    public RecipeMessage(CompoundTag nbt) {
        this.nbt = nbt;
    }
    
    public static RecipeMessage decode(FriendlyByteBuf buf) {
        RecipeMessage message = new RecipeMessage();
        message.index = buf.readInt();
        message.nbt = buf.readNbt();
        return message;
    }
    
    public static void encode(RecipeMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.index);
        buf.writeNbt(msg.nbt);
    }
    
    public static void handle(RecipeMessage message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getPlayer();
            if (player.containerMenu instanceof ContainerNetwork == false) {
                return;
            }
            ContainerNetwork ctr = (ContainerNetwork) player.containerMenu;
            TileMain main = ctr.getTileMain();
            if (main == null) {
                StorageNetwork.log("Recipe message cancelled, null tile " + ctr);
                return;
            }
            ClearRecipeMessage.clearContainerRecipe(player, false);
            CraftingContainer craftMatrix = ctr.getCraftMatrix();
            for (int slot = 0; slot < 9; slot++) {
                Map<Integer, ItemStack> map = new HashMap<>();
                //if its a string, then ore dict is allowed
                /*********
                 * parse nbt of the slot, whether its ore dict, itemstack, ore empty
                 **********/
                boolean isOreDict;
                isOreDict = false;
                ListTag invList = message.nbt.getList("s" + slot, Tag.TAG_COMPOUND);
                for (int i = 0; i < invList.size(); i++) {
                    CompoundTag stackTag = invList.getCompound(i);
                    ItemStack s = ItemStack.of(stackTag);
                    map.put(i, s);
                }
                /********* end parse nbt of this current slot ******/
                /********** now start trying to fill in recipe **/
                for (int i = 0; i < map.size(); i++) {
                    ItemStack stackCurrent = map.get(i);
                    if (stackCurrent == null || stackCurrent.isEmpty()) {
                        continue;
                    }
                    ItemStackMatcher itemStackMatcher = new ItemStackMatcher(stackCurrent);
                    itemStackMatcher.setNbt(true);
                    itemStackMatcher.setOre(isOreDict);
                    ItemStack ex = UtilInventory.extractItem(ItemTransfer.container(player.getInventory(), null), itemStackMatcher, 1, true);
                    /*********** First try and use the players inventory **/
                    if (ex != null && !ex.isEmpty() && craftMatrix.getItem(slot).isEmpty()) {
                        UtilInventory.extractItem(ItemTransfer.container(player.getInventory(), null), itemStackMatcher, 1, false);
                        //make sure to add the real item after the nonsimulated withdrawl is complete https://github.com/PrinceOfAmber/Storage-Network/issues/16
                        craftMatrix.setItem(slot, ex);
                        break;
                    }
                    /********* now find it from the network ***/
                    stackCurrent = main.request(!stackCurrent.isEmpty() ? itemStackMatcher : null, 1, false);
                    if (!stackCurrent.isEmpty() && craftMatrix.getItem(slot).isEmpty()) {
                        craftMatrix.setItem(slot, stackCurrent);
                        break;
                    }
                }
                /************** finished recipe population **/
                //        }
                //now make sure client sync happens.
                ctr.slotChanged();
                List<ItemStack> list = main.getStacks();
                PacketRegistry.INSTANCE.sendToPlayer(player, new StackRefreshClientMessage(list, new ArrayList<>()));
            } //end run
        });
    }
}
