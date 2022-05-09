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

package com.lothrazar.storagenetwork.api;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * All storage-networks parts that expose IConnectable can also expose this capability to make the part able to interact with nearby inventories.
 * <p>
 * What's being done with the stacks is completely up to your implementation. This can be for example a cable that can connect to a nearby InventoryHandler. Or the cable could store the given
 * itemstacks in a WorldSavedData structure or simply forget about them (which is obviously a bad idea).
 */
public interface IConnectableLink {
    
    /**
     * Use this method to return all stacks that are stored via this {@link IConnectableLink}. This should be a filtered list, i.e. if you offer some sort of item filter for your storage, apply the
     * filters here or players will see the item in their control panel.
     * <p>
     * isFiltered : if true , it does apply the filter. if not it returns all stacks ignoring filter
     *
     * @return
     */
    List<ItemStack> getStoredStacks(boolean isFiltered);
    
    /**
     * This is called on your capability every time the network tries to insert a stack into your storage.
     * <p>
     * If your getSupportedTransferDirection is set to OUT, this should never get called, unless another malicious mod is doing it.
     *
     * @param stack    The stack being inserted into your storage
     * @param simulate Whether or not this is just a simulation
     * @return The remainder of the stack if not all of it fit into your storage
     */
    ItemStack insertStack(ItemStack stack, boolean simulate);
    
    /**
     * This is called whenever a stack is requested from the network. If your storage can not supply it, return an empty itemstack. Otherwise return a matching stack. If its not a simulation actually
     * remove the stack from your storage!
     * <p>
     * Extract at maximum the given size.
     * <p>
     * If your getSupportedTransferDirection is set to IN, this should never get called, unless another malicious mod is doing it.
     *
     * @param matcher  A description of an itemstack that would fulfill the request
     * @param size     The maximum stack size you should extract from your storage
     * @param simulate Whether or not this is just a simulation
     * @return The stack that has been requested, if you have it
     */
    ItemStack extractStack(IItemStackMatcher matcher, final int size, boolean simulate);
    
    /**
     * Storages with a higher priority (== lower number) are processed first. You probably want to add a way to configure the priority of your storage.
     *
     * @return Return the priority here
     */
    int getPriority();
    
    /**
     * If you want to limit this IConnectableLink to only export or only import you can return the appropriate EnumStorageDirection here
     *
     * @return
     */
    EnumStorageDirection getSupportedTransferDirection();
    
    /**
     * We need to know for various reasons how many more stacks this inventory can hold, i.e. how many empty slots there are.
     *
     * @return
     */
    int getEmptySlots();
    
    void setPriority(int value);
    
    void setFilter(int value, ItemStack copy);
}
