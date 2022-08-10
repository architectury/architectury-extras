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

package com.lothrazar.storagenetwork.capability;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.*;
import com.lothrazar.storagenetwork.block.exchange.ExchangeItemStackHandler;
import com.lothrazar.storagenetwork.capability.handler.FilterItemStackHandler;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import com.lothrazar.storagenetwork.util.UtilInventory;
import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.transfer.ResourceView;
import dev.architectury.transfer.TransferAction;
import dev.architectury.transfer.TransferHandler;
import dev.architectury.transfer.item.ItemTransfer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

// TODO: We should add support for CommonCapabilities SlotlessItemHandler for efficiency reasons and compatibility with colossal chests, integrated dynamics etc
public class CapabilityConnectableLink implements IConnectableLink {
    
    public final IConnectable connectable;
    private boolean operationMustBeSmaller = true;
    private ItemStack operationStack = ItemStack.EMPTY;
    private int operationLimit = 0;
    private FilterItemStackHandler filters = new FilterItemStackHandler();
    private EnumStorageDirection filterDirection = EnumStorageDirection.BOTH;
    private Direction inventoryFace;
    private int priority;
    
    CapabilityConnectableLink() {
        connectable = new CapabilityConnectable();
        filters.setIsAllowlist(false);
    }
    
    public CapabilityConnectableLink(BlockEntity tile) {
        connectable = StorageNetworkCapabilities.CONNECTABLE_CAPABILITY.get(tile, null);
        filters.setIsAllowlist(false);
    }
    
    public FilterItemStackHandler getFilter() {
        return filters;
    }
    
    @Override
    public void setFilter(int value, ItemStack stack) {
        filters.set(value, stack);
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    @Override
    public List<ItemStack> getStoredStacks(boolean isFiltered) {
        if (inventoryFace == null || connectable.getPos() == null) {
            return Collections.emptyList();
        }
        DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
        // Test whether the connected block has the IItemHandler capability
        TransferHandler<ItemStack> itemHandler = inventoryPos.getCapability(ItemTransfer.BLOCK, inventoryFace.getOpposite());
        if (itemHandler == null) {
            return Collections.emptyList();
        }
        // If it does, iterate its stacks, filter them and add them to the result list
        List<ItemStack> result = new ArrayList<>();
        for (ResourceView<ItemStack> view : itemHandler) {
            ItemStack stack = view.getResource();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (isFiltered && filters.isStackFiltered(stack)) {
                continue;
            }
            result.add(stack.copy());
        }
        return result;
    }
    
    @Override
    public ItemStack insertStack(ItemStack stack, boolean simulate) {
        // If this storage is configured to only import into the network, do not
        // insert into the storage, but abort immediately.
        if (filterDirection == EnumStorageDirection.IN) {
            return stack;
        }
        if (filters.isStackFiltered(stack)) {
            return stack;
        }
        if (inventoryFace == null) {
            return stack;
        }
        DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
        try {
            // Test whether the connected block has the IItemHandler capability
            TransferHandler<ItemStack> itemHandler = inventoryPos.getCapability(ItemTransfer.BLOCK, inventoryFace.getOpposite());
            if (itemHandler == null) {
                return stack;
            }
            long inserted = itemHandler.insert(stack, simulate ? TransferAction.SIMULATE : TransferAction.ACT);
            return ItemStackHooks.copyWithCount(stack, stack.getCount() - (int) inserted);
        } catch (Exception e) {
            StorageNetwork.LOGGER.error("Insert stack error from other block ", e);
            return stack;
        }
    }
    
    @Override
    public ItemStack extractStack(IItemStackMatcher matcher, int size, boolean simulate) {
        // If nothing is actually being requested, abort immediately
        if (size <= 0) {
            return ItemStack.EMPTY;
        }
        // If this storage is configured to only export from the network, do not
        // extract from the storage, but abort immediately.
        if (filterDirection == EnumStorageDirection.IN) {
            return ItemStack.EMPTY;
        }
        if (inventoryFace == null) {
            return ItemStack.EMPTY;
        }
        DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
        // Test whether the connected block has the IItemHandler capability
        TransferHandler<ItemStack> itemHandler = inventoryPos.getCapability(ItemTransfer.BLOCK, inventoryFace.getOpposite());
        if (itemHandler == null) {
            return ItemStack.EMPTY;
        }
        if (itemHandler instanceof ExchangeItemStackHandler) {
            return ItemStack.EMPTY;
        }
        ItemStack[] firstMatchedStack = {ItemStack.EMPTY};
        int remaining = size;
        for (ResourceView<ItemStack> view : itemHandler) {
            //force simulate: allow them to not let me see the stack, also dont extract since it might steal/dupe
            ItemStack stack = view.extract(s ->
                            !filters.isStackFiltered(s) && (firstMatchedStack[0].isEmpty() ? matcher.match(s) : UtilInventory.canStack(firstMatchedStack[0], s)),
                    remaining, TransferAction.SIMULATE);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            // If its not even the item type we're looking for -> continue
            if (firstMatchedStack[0].isEmpty()) {
                firstMatchedStack[0] = stack.copy();
            }
            int toExtract = Math.min(stack.getCount(), remaining);
            ItemStack extractedStack = view.extractAny(toExtract, simulate ? TransferAction.SIMULATE : TransferAction.ACT);
            remaining -= extractedStack.getCount();
            if (remaining <= 0) {
                break;
            }
        }
        int extractCount = size - remaining;
        if (!firstMatchedStack[0].isEmpty() && extractCount > 0) {
            firstMatchedStack[0].setCount(extractCount);
        }
        return firstMatchedStack[0];
    }
    
    @Override
    public int getEmptySlots() {
        // If this storage is configured to only import into the network, do not
        // insert into the storage, but abort immediately.
        if (filterDirection == EnumStorageDirection.IN) {
            return 0;
        }
        if (inventoryFace == null) {
            return 0;
        }
        DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
        // Test whether the connected block has the IItemHandler capability
        TransferHandler<ItemStack> itemHandler = inventoryPos.getCapability(ItemTransfer.BLOCK, inventoryFace.getOpposite());
        if (itemHandler == null) {
            return 0;
        }
        int emptySlots = 0;
        for (ResourceView<ItemStack> view : itemHandler) {
            ItemStack stack = view.getResource();
            if (stack != null && !stack.isEmpty()) {
                continue;
            }
            emptySlots++;
        }
        return emptySlots;
    }
    
    @Override
    public void setPriority(int value) {
        this.priority = value;
    }
    
    @Override
    public EnumStorageDirection getSupportedTransferDirection() {
        return filterDirection;
    }
    
    public void setInventoryFace(Direction inventoryFace) {
        this.inventoryFace = inventoryFace;
    }
    
    public CompoundTag serializeNBT() {
        CompoundTag result = new CompoundTag();
        result.putInt("prio", priority);
        if (inventoryFace != null) {
            result.putString("inventoryFace", inventoryFace.toString());
        }
        result.putString("way", filterDirection.toString());
        CompoundTag operation = new CompoundTag();
        operation.put("stack", operationStack.save(new CompoundTag()));
        operation.putBoolean("mustBeSmaller", operationMustBeSmaller);
        operation.putInt("limit", operationLimit);
        result.put("operation", operation);
        CompoundTag filters = this.filters.save(new CompoundTag());
        result.put("filters", filters);
        return result;
    }
    
    public void deserializeNBT(CompoundTag nbt) {
        priority = nbt.getInt("prio");
        CompoundTag filters = nbt.getCompound("filters");
        this.filters.load(filters);
        if (nbt.contains("inventoryFace")) {
            inventoryFace = Direction.byName(nbt.getString("inventoryFace"));
        }
        try {
            filterDirection = EnumStorageDirection.valueOf(nbt.getString("way"));
        } catch (Exception e) {
            filterDirection = EnumStorageDirection.BOTH;
        }
        CompoundTag operation = nbt.getCompound("operation");
        operationStack = ItemStack.EMPTY;
        if (operation != null) {
            operationLimit = operation.getInt("limit");
            operationMustBeSmaller = operation.getBoolean("mustBeSmaller");
            if (operation.contains("stack")) {
                operationStack = ItemStack.of(operation.getCompound("stack"));
            }
        }
    }
    
    public static class Factory implements Callable<IConnectableLink> {
        
        @Override
        public IConnectableLink call() throws Exception {
            return new CapabilityConnectableLink();
        }
    }
    //  public static class Storage implements Capability.IStorage<IConnectableLink> {
    //
    //    @Override
    //    public Tag writeNBT(Capability<IConnectableLink> capability, IConnectableLink rawInstance, Direction side) {
    //      CapabilityConnectableLink instance = (CapabilityConnectableLink) rawInstance;
    //      return instance.serializeNBT();
    //    }
    //
    //    @Override
    //    public void readNBT(Capability<IConnectableLink> capability, IConnectableLink rawInstance, Direction side, Tag nbt) {
    //      CapabilityConnectableLink instance = (CapabilityConnectableLink) rawInstance;
    //      instance.deserializeNBT((CompoundTag) nbt);
    //    }
    //  }
}
