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

package com.lothrazar.storagenetwork.gui;

import com.lothrazar.storagenetwork.api.IGuiPrivate;
import com.lothrazar.storagenetwork.util.UtilInventory;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

/**
 * used as the MAIN grid in the network item display
 * <p>
 * also as ghost/filter items in the cable filter slots
 */
public class ItemSlotNetwork {
    
    private final int x;
    private final int y;
    private int size;
    private final int guiLeft;
    private final int guiTop;
    private boolean showNumbers;
    private final IGuiPrivate parent;
    private ItemStack stack;
    
    //TODO: Interface for parent expose isInRegion and drawgradient rect and the tooltip one
    public ItemSlotNetwork(IGuiPrivate parent, ItemStack stack, int x, int y, int size, int guiLeft, int guiTop, boolean number) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        setShowNumbers(number);
        this.parent = parent;
        setStack(stack);
    }
    
    public boolean isMouseOverSlot(int mouseX, int mouseY) {
        return parent.isInRegion(x - guiLeft, y - guiTop, 16, 16, mouseX, mouseY);
    }
    
    public void drawSlot(PoseStack poseStack, Font font, int mx, int my) {
        if (!getStack().isEmpty()) {
            //      poseStack.pushPose();
            String amount;
            //cant sneak in gui
            //default to short form, show full amount if sneak
            if (Screen.hasShiftDown()) {
                amount = size + "";
            } else {
                amount = UtilInventory.formatLargeNumber(size);
            }
            final float scale = 0.85F;
            PoseStack viewModelPose = RenderSystem.getModelViewStack();
            viewModelPose.pushPose();
            viewModelPose.translate(x + 3, y + 3, 0);
            viewModelPose.scale(scale, scale, scale);
            viewModelPose.translate(-1 * x, -1 * y, 0);
            RenderSystem.applyModelViewMatrix();
            if (isShowNumbers() && size > 1) {
                Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, stack, x, y, amount);
            }
            viewModelPose.popPose();
            RenderSystem.applyModelViewMatrix();
            if (isMouseOverSlot(mx, my)) {
                int j1 = x;
                int k1 = y;
                RenderSystem.colorMask(true, true, true, false);
                parent.drawGradient(poseStack, j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                RenderSystem.colorMask(true, true, true, true);
            }
            Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(getStack(), x, y);
        }
    }
    
    public void drawTooltip(PoseStack ms, int mx, int my) {
        if (isMouseOverSlot(mx, my) && !getStack().isEmpty()) {
            parent.renderStackTooltip(ms, getStack(),
                    mx - parent.getGuiLeft(),
                    my - parent.getGuiTopFixJei());
        }
    }
    
    public ItemStack getStack() {
        return stack;
    }
    
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    private boolean isShowNumbers() {
        return showNumbers;
    }
    
    private void setShowNumbers(boolean showNumbers) {
        this.showNumbers = showNumbers;
    }
}
