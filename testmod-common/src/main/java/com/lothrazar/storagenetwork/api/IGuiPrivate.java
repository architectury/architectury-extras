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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public interface IGuiPrivate {
    //  void renderStackToolTip(ItemStack stack, int x, int y);
    //  void renderTooltip(List<String> t, int x, int y);
    //  void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor);
    
    int getGuiTop();
    
    //jei is forcing crashes for negative values so dodge that arbitrary made up rule java.lang.IllegalArgumentException: guiTop must be >= 0
    //  at mezz.jei.gui.overlay.GuiProperties.<init>(GuiProperties.java:110) ~[jei-1.18.2-9.5.0.132_mapped_official_1.18.2.jar%2382!/:9.5.0.132] {re:classloading}
    default int getGuiTopFixJei() {
        return getGuiTop(); // default if no fix override -8 needed
    }
    
    int getGuiLeft();
    
    boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY);
    
    void drawGradient(PoseStack ms, int j1, int k1, int i, int j, int k, int l);
    
    void renderStackTooltip(PoseStack ms, ItemStack stack, int i, int j);
}
