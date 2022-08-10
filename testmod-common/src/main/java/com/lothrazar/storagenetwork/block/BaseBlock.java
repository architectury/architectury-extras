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

package com.lothrazar.storagenetwork.block;

import com.lothrazar.storagenetwork.block.cable.BlockCable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.List;

public abstract class BaseBlock extends BaseEntityBlock {
    public BaseBlock(Material materialIn) {
        super(Block.Properties.of(materialIn).strength(0.5F).sound(SoundType.STONE));
    }
    
    public BaseBlock(Properties prop) {
        super(prop);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, BlockGetter playerIn, List<Component> tooltip, TooltipFlag advanced) {
        super.appendHoverText(stack, playerIn, tooltip, advanced);
        MutableComponent t = Component.translatable(getDescriptionId() + ".tooltip");
        t.withStyle(ChatFormatting.GRAY);
        tooltip.add(t);
    }
    
    protected void updateConnection(Level worldIn, BlockPos pos, BlockState stateIn) {
        BlockState facingState;
        for (Direction d : Direction.values()) {
            BlockPos facingPos = pos.relative(d);
            facingState = worldIn.getBlockState(facingPos);
            if (facingState.getBlock() instanceof BlockCable) {
                BlockCable c = (BlockCable) facingState.getBlock();
                c.updateShape(facingState, d.getOpposite(), stateIn, worldIn, facingPos, pos);
            }
        }
    }
}
