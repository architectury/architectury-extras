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

package com.lothrazar.storagenetwork.block.collection;

import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.IGuiPrivate;
import com.lothrazar.storagenetwork.capability.handler.FilterItemStackHandler;
import com.lothrazar.storagenetwork.gui.ItemSlotNetwork;
import com.lothrazar.storagenetwork.network.CableIOMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GuiCollectionFilter extends AbstractContainerScreen<ContainerCollectionFilter> implements IGuiPrivate {
    
    private final ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/plain_filter.png");
    ContainerCollectionFilter containerCableLink;
    private List<ItemSlotNetwork> itemSlotsGhost;
    
    public GuiCollectionFilter(ContainerCollectionFilter containerCableFilter, Inventory inv, Component name) {
        super(containerCableFilter, inv, name);
        this.containerCableLink = containerCableFilter;
    }
    
    @Override
    public void renderStackTooltip(PoseStack ms, ItemStack stack, int mousex, int mousey) {
        super.renderTooltip(ms, stack, mousex, mousey);
    }
    
    @Override
    public void drawGradient(PoseStack ms, int x, int y, int x2, int y2, int u, int v) {
        super.fillGradient(ms, x, y, x2, y2, u, v);
    }
    
    @Override
    public int getGuiTop() {
        return topPos;
    }
    
    @Override
    public int getGuiLeft() {
        return leftPos;
    }
    
    @Override
    public void init() {
        super.init();
    }
    
    private void sendStackSlot(int value, ItemStack stack) {
        PacketRegistry.INSTANCE.sendToServer(new CableIOMessage(CableIOMessage.CableMessageType.SAVE_FITLER.ordinal(), value, stack));
    }
    
    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);
        this.renderTooltip(ms, mouseX, mouseY);
    }
    
    @Override
    public void renderLabels(PoseStack ms, int mouseX, int mouseY) {
        //    super.drawGuiContainerForegroundLayer(ms, mouseX, mouseY);
    }
    
    public static final int SLOT_SIZE = 18;
    
    @Override
    protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
        //    minecraft.getTextureManager().bind(texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        int xCenter = (width - imageWidth) / 2;
        int yCenter = (height - imageHeight) / 2;
        blit(ms, xCenter, yCenter, 0, 0, imageWidth, imageHeight);
        itemSlotsGhost = Lists.newArrayList();
        //TODO: shared with GuiCableIO
        int rows = 2;
        int cols = 9;
        int index = 0;
        int y = 35;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                ItemStack stack = containerCableLink.cap.getFilter().get(index).getResource();
                int x = 8 + col * SLOT_SIZE;
                itemSlotsGhost.add(new ItemSlotNetwork(this, stack, leftPos + x, topPos + y, stack.getCount(), leftPos, topPos, true));
                index++;
            }
            //move down to second row
            y += SLOT_SIZE;
        }
        for (ItemSlotNetwork s : itemSlotsGhost) {
            s.drawSlot(ms, font, mouseX, mouseY);
        }
    }
    
    public void setFilterItems(List<ItemStack> stacks) {
        FilterItemStackHandler filter = this.containerCableLink.cap.getFilter();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack s = stacks.get(i);
            filter.set(i, s);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        ItemStack mouse = minecraft.player.containerMenu.getCarried();
        for (int i = 0; i < this.itemSlotsGhost.size(); i++) {
            ItemSlotNetwork slot = itemSlotsGhost.get(i);
            if (slot.isMouseOverSlot((int) mouseX, (int) mouseY)) {
                if (slot.getStack().isEmpty() == false) {
                    //i hit non-empty slot, clear it no matter what
                    if (mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
                        int direction = hasShiftDown() ? -1 : 1;
                        int newCount = Math.min(64, slot.getStack().getCount() + direction);
                        if (newCount < 1) {
                            newCount = 1;
                        }
                        slot.getStack().setCount(newCount);
                    } else {
                        slot.setStack(ItemStack.EMPTY);
                    }
                    this.sendStackSlot(i, slot.getStack());
                    return true;
                } else {
                    //i hit an empty slot, save what im holding
                    ItemStack cpy = mouse.copy();
                    cpy.setCount(1);
                    slot.setStack(cpy);
                    this.sendStackSlot(i, cpy);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }
}
