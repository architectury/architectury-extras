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

package com.lothrazar.storagenetwork.block.main;

import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.*;
import com.lothrazar.storagenetwork.block.exchange.TileExchange;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import com.lothrazar.storagenetwork.util.UtilInventory;
import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.registry.registries.Registries;
import dev.architectury.transfer.ResourceView;
import dev.architectury.transfer.TransferAction;
import dev.architectury.transfer.TransferHandler;
import dev.architectury.transfer.item.ItemTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TileMain extends BlockEntity {
    
    private Set<DimPos> connectables;
    private Map<String, DimPos> importCache = new HashMap<>();
    private boolean shouldRefresh = true;
    
    private DimPos getDimPos() {
        return new DimPos(level, worldPosition);
    }
    
    public TileMain(BlockPos pos, BlockState state) {
        super(SsnRegistry.MAINTILEENTITY.get(), pos, state);
    }
    
    public List<ItemStack> getSortedStacks() {
        List<ItemStack> stacks = Lists.newArrayList();
        try {
            if (getConnectablePositions() == null) {
                refreshNetwork();
            }
        } catch (Exception e) {
            //since this has external mod connections, if they break then catch it
            //      for example, AE2 can break with  Ticking GridNode
            StorageNetwork.LOGGER.info("3rd party storage mod has an error", e);
        }
        try {
            for (IConnectableLink storage : getSortedConnectableStorage()) {
                for (ItemStack stack : storage.getStoredStacks(true)) {
                    if (stack == null || stack.isEmpty()) {
                        continue;
                    }
                    addOrMergeIntoList(stacks, stack);
                }
            }
        } catch (Exception e) {
            StorageNetwork.LOGGER.info("3rd party storage mod has an error", e);
        }
        return stacks;
    }
    
    public List<ItemStack> getStacks() {
        List<ItemStack> stacks = Lists.newArrayList();
        try {
            if (getConnectablePositions() == null) {
                refreshNetwork();
            }
        } catch (Exception e) {
            //since this has external mod connections, if they break then catch it
            //      for example, AE2 can break with  Ticking GridNode
            StorageNetwork.LOGGER.info("3rd party storage mod has an error", e);
        }
        try {
            for (IConnectableLink storage : getConnectableStorage()) {
                for (ItemStack stack : storage.getStoredStacks(true)) {
                    if (stack == null || stack.isEmpty()) {
                        continue;
                    }
                    addOrMergeIntoList(stacks, stack);
                }
            }
        } catch (Exception e) {
            StorageNetwork.LOGGER.info("3rd party storage mod has an error", e);
        }
        return stacks;
    }
    
    private static void addOrMergeIntoList(List<ItemStack> list, ItemStack stackToAdd) {
        boolean added = false;
        for (ItemStack stack : list) {
            if (UtilInventory.canStack(stackToAdd, stack)) {
                stack.setCount(stack.getCount() + stackToAdd.getCount());
                added = true;
                break;
            }
        }
        if (!added) {
            list.add(stackToAdd);
        }
    }
    
    int emptySlots() {
        int countEmpty = 0;
        for (IConnectableLink storage : getSortedConnectableStorage()) {
            countEmpty += storage.getEmptySlots();
        }
        return countEmpty;
    }
    
    public int getAmount(ItemStackMatcher fil) {
        if (fil == null) {
            return 0;
        }
        int totalCount = 0;
        for (ItemStack stack : getStacks()) {
            if (!fil.match(stack)) {
                continue;
            }
            totalCount += stack.getCount();
        }
        return totalCount;
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt);
        return nbt;
    }
    
    /**
     * This is a recursively called method that traverses all connectable blocks and stores them in this tiles connectables list.
     *
     * @param sourcePos
     */
    private Set<DimPos> getConnectables(DimPos sourcePos) {
        HashSet<DimPos> result = new HashSet<>();
        addConnectables(sourcePos, result);
        return result;
    }
    
    private void addConnectables(DimPos sourcePos, Set<DimPos> set) {
        if (sourcePos == null || sourcePos.getWorld() == null || !sourcePos.isLoaded()) {
            return;
        }
        // Look in all directions
        for (Direction direction : Direction.values()) {
            DimPos lookPos = sourcePos.offset(direction);
            if (!lookPos.isLoaded()) {
                continue;
            }
            ChunkAccess chunk = lookPos.getChunk();
            if (chunk == null) {
                continue;
            }
            // Prevent having multiple  on a network and break all others.
            TileMain maybeMain = lookPos.getTileEntity(TileMain.class);
            if (maybeMain != null && !lookPos.equals(level, worldPosition)) {
                nukeAndDrop(lookPos);
                continue;
            }
            BlockEntity tileHere = lookPos.getTileEntity(BlockEntity.class);
            if (tileHere == null) {
                continue;
            }
            IConnectable capabilityConnectable = StorageNetworkCapabilities.CONNECTABLE_CAPABILITY.get(tileHere, direction.getOpposite());
            if (capabilityConnectable == null) {
                continue;
            }
            //
            if (capabilityConnectable.getPos() == null) {
                //  1.15 hax
                // StorageNetwork.LOGGER.info("1.15 HAX NULL POS !! " + lookPos + "has tile " + tileHere);
                //wait what 
                capabilityConnectable.setPos(lookPos);
                capabilityConnectable.setMainPos(this.getDimPos());
            }
            //
            if (capabilityConnectable != null) {
                //        IConnectable capabilityConnectable = tileHere.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, direction.getOpposite());
                capabilityConnectable.setMainPos(getDimPos());
                DimPos realConnectablePos = capabilityConnectable.getPos();
                boolean beenHereBefore = set.contains(realConnectablePos);
                if (beenHereBefore) {
                    continue;
                }
                if (realConnectablePos.getWorld() == null) {
                    // StorageNetwork.LOGGER.info("1.15 realConnectablePos HAX NULL WORLD  " + realConnectablePos);
                    realConnectablePos.setWorld(sourcePos.getWorld());
                }
                set.add(realConnectablePos);
                addConnectables(realConnectablePos, set);
                tileHere.setChanged();
                chunk.setUnsaved(true);
            }
        }
    }
    
    private static void nukeAndDrop(DimPos lookPos) {
        lookPos.getWorld().destroyBlock(lookPos.getBlockPos(), true);
        lookPos.getWorld().removeBlockEntity(lookPos.getBlockPos());
    }
    
    public static boolean isTargetAllowed(BlockState state) {
        if (state.getBlock() == Blocks.AIR) {
            return false;
        }
        String blockId = Registries.getId(state.getBlock(), Registry.BLOCK_REGISTRY).toString();
        for (String s : StorageNetwork.CONFIG.ignorelist()) {
            if (blockId.equals(s)) {
                return false;
            }
        }
        return true;
    }
    
    public void refreshNetwork() {
        if (level.isClientSide) {
            return;
        }
        shouldRefresh = true;
    }
    
    private boolean hasCachedSlot(ItemStack stack) {
        return importCache.containsKey(getStackKey(stack));
    }
    
    private DimPos getCachedSlot(ItemStack stack) {
        return importCache.get(getStackKey(stack));
    }
    
    /**
     * returns countUnmoved , the number of items NOT inserted.
     */
    public int insertStack(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return 0;
        }
        // 1. Try to insert into a recent slot for the same item.
        //    We do this to avoid having to search for the appropriate inventory repeatedly.
        String key = getStackKey(stack);
        if (hasCachedSlot(stack)) {
            DimPos cachedStoragePos = getCachedSlot(stack);
            IConnectableLink storage = cachedStoragePos.getCapability(StorageNetworkCapabilities.CONNECTABLE_ITEM_STORAGE_CAPABILITY, null);
            if (storage == null) {
                // The block at the cached position is not even an IConnectableLink anymore
                importCache.remove(key);
            } else {
                // But if it is, we test whether it can still import that particular stack and do so if it does.
                boolean canStillImport = storage.getSupportedTransferDirection().match(EnumStorageDirection.IN);
                if (canStillImport && storage.insertStack(stack, true).getCount() < stack.getCount()) {
                    stack = storage.insertStack(stack, simulate);
                } else {
                    importCache.remove(key);
                }
            }
        }
        // 2. If everything got transferred into the cached storage, end here
        if (stack.isEmpty()) {
            return 0;
        }
        // 3. Otherwise try to find a new inventory that can take the remainder of the itemstack
        List<IConnectableLink> storages = getSortedConnectableStorage();
        for (IConnectableLink storage : storages) {
            try {
                // Ignore storages that can not import
                if (!storage.getSupportedTransferDirection().match(EnumStorageDirection.IN)) {
                    continue;
                }
                // The given import-capable storage can not import this particular stack
                if (storage.insertStack(stack, true).getCount() >= stack.getCount()) {
                    continue;
                }
                // If it can we need to know, i.e. store the remainder
                stack = storage.insertStack(stack, simulate);
            } catch (Exception e) {
                StorageNetwork.LOGGER.error("insertStack container issue", e);
            }
        }
        return stack.getCount();
    }
    
    private static String getStackKey(ItemStack stackInCopy) {
        return Registries.getId(stackInCopy.getItem(), Registry.ITEM_REGISTRY).toString();
    }
    
    /**
     * Pull into the network from the relevant linked cables
     */
    private void updateImports() {
        for (IConnectable connectable : getConnectables()) {
            if (connectable == null || connectable.getPos() == null) {
                continue;
            }
            IConnectableItemAutoIO storage = connectable.getPos().getCapability(StorageNetworkCapabilities.CONNECTABLE_AUTO_IO, null);
            if (storage == null) {
                continue;
            }
            //
            // We explicitely don't want to check whether this can do BOTH, because we don't
            // want to import what we've just exported in updateExports().
            if (storage.ioDirection() != EnumStorageDirection.IN) {
                continue;
            }
            // Give the storage a chance to have a cooldown or other conditions that prevent it from running
            if (!storage.runNow(connectable.getPos(), this)) {
                continue;
            }
            //      int amtToRequest = storage.getTransferRate();
            //TODO
            // storage.getUpgrades().getUpgradesOfType(SsnRegistry.STACK_UPGRADE) > 0 ? 64 : 4;
            // Do a simulation first and abort if we got an empty stack,
            //(filters used internally in extractNextStack)
            TransferHandler<ItemStack> itemHandler = storage.getItemHandler();
            if (itemHandler == null) {
                continue;
            }
            if (storage.needsRedstone()) {
                boolean power = level.hasNeighborSignal(connectable.getPos().getBlockPos());
                if (power == false) {
                    continue;
                }
            }
            itemHandler.withContents(resourceViews -> {
                for (ResourceView<ItemStack> view : resourceViews) {
                    if (view.getResource().isEmpty()) {
                        continue;
                    }
                    ItemStack stackCurrent = view.getResource().copy();
                    // Ignore stacks that are filtered
                    if (storage.getFilters() == null || !storage.getFilters().isStackFiltered(stackCurrent)) {
                        if (storage.isStockMode()) {
                            int filterSize = storage.getFilters().getStackCount(stackCurrent);
                            BlockEntity tileEntity = level.getBlockEntity(connectable.getPos().getBlockPos().relative(storage.facingInventory()));
                            TransferHandler<ItemStack> targetInventory = ItemTransfer.BLOCK.get(tileEntity, storage.facingInventory().getOpposite());
                            //request with false to see how many even exist in there.
                            int chestHowMany = UtilInventory.countHowMany(targetInventory, stackCurrent);
                            //so if chest=37 items of that kind
                            //and the filter is say filterSize == 20
                            //we SHOULD import 37
                            //as we want the STOCK of the chest to not go less than the filter number , just down to it
                            if (chestHowMany > filterSize) {
                                int realSize = Math.min(chestHowMany - filterSize, 64);
                                StorageNetwork.log(" : stock mode import  realSize = " + realSize);
                                stackCurrent.setCount(realSize);
                            } else {
                                StorageNetwork.log(" : stock mode CANCEL: ITS NOT ENOUGH chestHowMany <= filter size ");
                                continue;
                            }
                        }
                        //
                        //
                        //
                        int extractSize = Math.min(storage.getTransferRate(), stackCurrent.getCount());
                        ItemStack stackToImport = view.extractAny(extractSize, TransferAction.SIMULATE); //simulate to grab a reference
                        if (stackToImport.isEmpty()) {
                            continue; //continue back to itemHandler
                        }
                        // Then try to insert the stack into this masters network and store the number of remaining items in the stack
                        int countUnmoved = this.insertStack(stackToImport, true);
                        // Calculate how many items in the stack actually got moved
                        int countMoved = stackToImport.getCount() - countUnmoved;
                        if (countMoved <= 0) {
                            continue; //continue back to itemHandler
                        }
                        // Alright, simulation says we're good, let's do it!
                        // First extract from the storage
                        ItemStack actuallyExtracted = view.extractAny(countMoved, TransferAction.ACT); // storage.extractNextStack(countMoved, false);
                        //          storage.getPos().getChunk().markDirty();
                        // Then insert into our network
                        this.insertStack(actuallyExtracted, false);
                        break; // break out of itemHandler loop, done processing this cable, so move to next
                    } //end of checking on filter for this stack
                }
            });
            //    }
            //      //
            //      //
        }
    }
    
    private void updateProcess() {
        //    for (IConnectable connectable : getConnectables()) {
        //    if (connectable == null || connectable.getPos() == null) {
        //      //        StorageNetwork.log("null connectable or pos : updateProcess() ");
        //      continue;
        //    }
        //      TileCableProcess cableProcess = connectable.getPos().getTileEntity(TileCableProcess.class);
        //      if (cableProcess == null) {
        //        continue;
        //      }
        //      cableProcess.run();
        //    }
    }
    
    /**
     * push OUT of the network to attached export cables
     */
    private void updateExports() {
        Set<IConnectable> conSet = getConnectables();
        for (IConnectable connectable : conSet) {
            if (connectable == null || connectable.getPos() == null) {
                //        StorageNetwork.log("null connectable or pos : updateExports() ");
                continue;
            }
            IConnectableItemAutoIO storage = connectable.getPos().getCapability(StorageNetworkCapabilities.CONNECTABLE_AUTO_IO, null);
            if (storage == null) {
                continue;
            }
            // We explicitely don't want to check whether this can do BOTH, because we don't
            // want to import what we've just exported in updateExports().
            if (storage.ioDirection() != EnumStorageDirection.OUT) {
                continue;
            }
            // Give the storage a chance to have a cooldown
            if (!storage.runNow(connectable.getPos(), this)) {
                continue;
            }
            if (storage.needsRedstone()) {
                boolean power = level.hasNeighborSignal(connectable.getPos().getBlockPos());
                if (power == false) {
                    //  StorageNetwork.log(power + " Export pow here ; needs yes skip me");
                    continue;
                }
            }
            for (IItemStackMatcher matcher : storage.getAutoExportList()) {
                if (matcher.getStack().isEmpty()) {
                    continue;
                }
                //default amt to request. can be overriden by other upgrades
                int amtToRequest = storage.getTransferRate();
                //check operations upgrade for export 
                boolean stockMode = storage.isStockMode();
                if (stockMode) {
                    StorageNetwork.log("stockMode == TRUE ; updateExports: attempt " + matcher.getStack());
                    //STOCK upgrade means
                    try {
                        BlockEntity tileEntity = level.getBlockEntity(connectable.getPos().getBlockPos().relative(storage.facingInventory()));
                        TransferHandler<ItemStack> targetInventory = ItemTransfer.BLOCK.get(tileEntity, null);
                        //request with false to see how many even exist in there.
                        int stillNeeds = UtilInventory.containsAtLeastHowManyNeeded(targetInventory, matcher.getStack(), matcher.getStack().getCount());
                        if (stillNeeds == 0) {
                            //they dont need any more, they have the stock they need
                            StorageNetwork.log("stockMode continnue; canc");
                            continue;
                        }
                        amtToRequest = Math.min(stillNeeds, amtToRequest);
                        StorageNetwork.log("updateExports stock mode edited value: amtToRequest = " + amtToRequest);
                    } catch (Throwable e) {
                        StorageNetwork.LOGGER.error("Error thrown from a connected block" + e);
                    }
                }
                if (matcher.getStack().isEmpty() || amtToRequest == 0) {
                    //either the thing is empty or we are requesting none
                    continue;
                }
                ItemStack requestedStack = this.request((ItemStackMatcher) matcher, amtToRequest, true);
                if (requestedStack.isEmpty()) {
                    continue;
                }
                //     StorageNetwork.log("updateExports: found requestedStack = " + requestedStack);
                // The stack is available in the network, let's simulate inserting it into the storage
                ItemStack insertedSim = storage.insertStack(requestedStack, true);
                // Determine the amount of items moved in the stack
                if (!insertedSim.isEmpty()) {
                    int movedItems = requestedStack.getCount() - insertedSim.getCount();
                    if (movedItems <= 0) {
                        continue;
                    }
                    requestedStack.setCount(movedItems);
                }
                // Alright, some items got moved in the simulation. Let's do it for real this time.
                ItemStack realExtractedStack = request(new ItemStackMatcher(requestedStack, false, true), requestedStack.getCount(), false);
                if (realExtractedStack.isEmpty()) {
                    continue;
                }
                storage.insertStack(realExtractedStack, false);
                break;
            }
        }
    }
    
    public ItemStack request(ItemStackMatcher matcher, int size, boolean simulate) {
        if (size == 0 || matcher == null) {
            return ItemStack.EMPTY;
        }
        // TODO: Test against storage drawers. There was some issue with it: https://github.com/PrinceOfAmber/Storage-Network/issues/19
        IItemStackMatcher usedMatcher = matcher;
        int alreadyTransferred = 0;
        for (IConnectableLink storage : getSortedConnectableStorage()) {
            int req = size - alreadyTransferred;
            ItemStack simExtract = storage.extractStack(usedMatcher, req, simulate);
            if (simExtract.isEmpty()) {
                continue;
            }
            // Do not stack items of different types together, i.e. make the filter rules more strict for all further items
            usedMatcher = new ItemStackMatcher(simExtract, matcher.isOre(), matcher.isNbt());
            alreadyTransferred += simExtract.getCount();
            if (alreadyTransferred >= size) {
                break;
            }
        }
        if (alreadyTransferred <= 0) {
            return ItemStack.EMPTY;
        }
        return ItemStackHooks.copyWithCount(usedMatcher.getStack(), alreadyTransferred);
    }
    
    private Set<IConnectable> getConnectables() {
        Set<DimPos> positions = getConnectablePositions();
        if (positions == null) {
            return new HashSet<>();
        }
        Set<IConnectable> result = new HashSet<>();
        for (DimPos pos : positions) {
            if (!pos.isLoaded()) {
                continue;
            }
            BlockEntity tileEntity = pos.getTileEntity(BlockEntity.class);
            if (tileEntity == null) {
                continue;
            }
            IConnectable cap = StorageNetworkCapabilities.CONNECTABLE_CAPABILITY.get(tileEntity, null);
            if (cap == null) {
                StorageNetwork.LOGGER.info("Somehow stored a dimpos that is not connectable... Skipping " + pos);
                continue;
            }
            result.add(cap);
        }
        return result;
    }
    
    private Set<IConnectableLink> getConnectableStorage() {
        Set<IConnectableLink> result = new HashSet<>();
        Set<DimPos> conSet = getConnectablePositions();
        for (DimPos dimpos : conSet) {
            if (!dimpos.isLoaded()) {
                continue;
            }
            BlockEntity tileEntity = dimpos.getTileEntity(BlockEntity.class);
            if (tileEntity == null) {
                continue;
            }
            IConnectableLink capConnect = StorageNetworkCapabilities.CONNECTABLE_ITEM_STORAGE_CAPABILITY.get(tileEntity, null);
            if (capConnect == null) {
                continue;
            }
            if (tileEntity instanceof TileExchange) {
                StorageNetwork.log("keep going??main tile exhchange bandaid");
                //        continue;
            }
            result.add(capConnect);
        }
        return result;
    }
    
    private List<IConnectableLink> getSortedConnectableStorage() {
        try {
            Set<IConnectableLink> storage = getConnectableStorage();
            Stream<IConnectableLink> stream = storage.stream();
            List<IConnectableLink> sorted = stream.sorted(Comparator.comparingInt(IConnectableLink::getPriority)).collect(Collectors.toList());
            return sorted;
        } catch (Exception e) {
            //trying to avoid 
            //java.lang.StackOverflowError: Ticking block entity
            //and similar issues
            StorageNetwork.LOGGER.error("Error: network get sorted by priority error, some network components are disconnected ", e);
            return new ArrayList<>();
        }
    }
    
    private void tick() {
        if (level == null || level.isClientSide) {
            return;
        }
        //refresh time in config, default 200 ticks aka 10 seconds
        if ((level.getGameTime() % StorageNetwork.CONFIG.refreshTicks() == 0)
                || shouldRefresh) {
            try {
                connectables = getConnectables(getDimPos());
                shouldRefresh = false;
                level.getChunk(worldPosition).setUnsaved(true);
            } catch (Throwable e) {
                StorageNetwork.LOGGER.info("Refresh network error ", e);
            }
        }
        updateImports();
        updateExports();
        updateProcess();
    }
    
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        saveWithFullMetadata();
        return ClientboundBlockEntityDataPacket.create(this); // new ClientboundBlockEntityDataPacket(worldPosition, 1, syncData);
    }
    
    public static boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
    
    /**
     * dont create an iterator over the original one that is being modified
     *
     * @return
     */
    public Set<DimPos> getConnectablePositions() {
        if (connectables == null) {
            connectables = new HashSet<>();
        }
        return new HashSet<>(connectables);
    }
    
    public void clearCache() {
        importCache = new HashMap<>();
    }
    
    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileMain tile) {
    }
    
    public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileMain tile) {
        tile.tick();
    }
}
