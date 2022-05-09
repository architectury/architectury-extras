/*
 * This file is part of architectury.
 * Copyright (C) 2020, 2021, 2022 architectury
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package dev.architectury.transfer.energy.wrapper;

import dev.architectury.transfer.TransferAction;
import dev.architectury.transfer.energy.EnergyTransferHandler;
import org.jetbrains.annotations.Nullable;

public interface BaseEnergyTransferHandler extends EnergyTransferHandler {
    @Override
    default Object saveState() {
        return getResource();
    }
    
    @Override
    default void loadState(Object state) {
        setResource((Long) state);
    }
    
    void setResource(long resource);
    
    @Override
    default long insert(Long toInsert, TransferAction action) {
        long currentAmount = getResource();
        boolean isEmpty = currentAmount <= 0;
        if (canInsert(toInsert)) {
            @Nullable
            Long slotSpace = isEmpty ? getResourceCapacity() : getResourceCapacity() - currentAmount;
            long inserted = slotSpace == null ? toInsert : Math.min(slotSpace, toInsert);
            
            if (inserted > 0 && action == TransferAction.ACT) {
                if (isEmpty) {
                    setResource(inserted);
                } else {
                    setResource(currentAmount + inserted);
                }
            }
            
            return inserted;
        }
        
        return 0;
    }
    
    @Override
    default Long extract(Long toExtract, TransferAction action) {
        long resource = getResource();
        long extracted = Math.min(toExtract, resource);
        if (extracted > 0) {
            if (action == TransferAction.ACT) {
                setResource(resource - extracted);
            }
            
            return extracted;
        }
        
        return blank();
    }
}
