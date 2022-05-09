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

import com.lothrazar.storagenetwork.capability.handler.FilterItemStackHandler;
import net.minecraft.world.item.ItemStack;

/**
 * All blocks that can connect to the storage-network need to expose this capability. Because of the way the storage-networking is built up, each connectable needs to expose its own position and
 * dimension, so it can be fully traversed when necessary.
 * <p>
 * If you want to expose this yourself instead of accessing it, you probably want to extend the DefaultConnectable implementation for your own capability.
 */
public interface IConnectable {
    
    void toggleNeedsRedstone();
    
    boolean needsRedstone();
    
    void needsRedstone(boolean in);
    
    /**
     * Return the position of the main. For historic reasons each block currently needs to know this.
     *
     * @return a DimPos with the proper dimension and position
     */
    DimPos getMainPos();
    
    /**
     * Return the position of this connectable.
     * <p>
     * This is used to traverse the network and might be different from the actual block position. For example the Compact Machines mod bridges capabilities across dimensions and we need to continue
     * traversing the network inside the compact machine and not at the machine block itself.
     * <p>
     * You should simply return the position of your block here.
     *
     * @return
     */
    DimPos getPos();
    
    /**
     * When your block is placed and a connected network updates, it calls this method to tell your capability where the {@link INetworkmain} is. Store this value and return it in getmainPos().
     *
     * @param mainPos
     */
    void setMainPos(DimPos mainPos);
    
    /**
     * Set data used in getPos
     *
     * @param lookPos
     */
    void setPos(DimPos lookPos);
    
    void setFilter(int value, ItemStack copy);
    
    FilterItemStackHandler getFilter();
}
