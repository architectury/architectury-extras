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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.IGuiNetwork;
import com.lothrazar.storagenetwork.block.request.GuiNetworkTable;
import com.lothrazar.storagenetwork.gui.ButtonRequest.TextureEnum;
import com.lothrazar.storagenetwork.network.InsertMessage;
import com.lothrazar.storagenetwork.network.RequestMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkWidget {
    
    private final IGuiNetwork gui;
    public EditBox searchBar;
    long lastClick;
    int page = 1, maxPage = 1;
    public List<ItemStack> stacks;
    List<ItemSlotNetwork> slots;
    private int lines = 4;
    private int columns = 9;
    public ItemStack stackUnderMouse = ItemStack.EMPTY;
    public int fieldHeight = 90;
    public ButtonRequest directionBtn;
    public ButtonRequest sortBtn;
    public ButtonRequest jeiBtn;
    public ButtonRequest focusBtn;
    
    public NetworkWidget(IGuiNetwork gui) {
        this.gui = gui;
        stacks = Lists.newArrayList();
        slots = Lists.newArrayList();
        PacketRegistry.INSTANCE.sendToServer(new RequestMessage());
        lastClick = System.currentTimeMillis();
    }
    
    public void applySearchTextToSlots() {
        String searchText = searchBar.getValue();
        List<ItemStack> stacksToDisplay = searchText.equals("") ? Lists.newArrayList(stacks) : Lists.newArrayList();
        if (!searchText.equals("")) {
            for (ItemStack stack : stacks) {
                if (doesStackMatchSearch(stack)) {
                    stacksToDisplay.add(stack);
                }
            }
        }
        this.sortStackWrappers(stacksToDisplay);
        this.applyScrollPaging(stacksToDisplay);
        this.rebuildItemSlots(stacksToDisplay);
    }
    
    public void clearSearch() {
        if (searchBar == null) {
            return;
        }
        searchBar.setValue("");
//        if (Platform.isModLoaded("jei") && gui.isJeiSearchSynced()) {
//            JeiHooks.setFilterText("");
//        }
    }
    
    private boolean doesStackMatchSearch(ItemStack stack) {
        String searchText = searchBar.getValue();
        if (searchText.startsWith("@")) { // TODO: ENUM //search modname 
            String name = UtilTileEntity.getModNameForItem(stack.getItem());
            return name.toLowerCase().contains(searchText.toLowerCase().substring(1));
        } else if (searchText.startsWith("#")) { // search tooltips
            String tooltipString;
            Minecraft mc = Minecraft.getInstance();
            List<Component> tooltip = stack.getTooltipLines(mc.player, TooltipFlag.Default.NORMAL);
            List<String> unformattedTooltip = tooltip.stream().map(Component::getString).collect(Collectors.toList());
            tooltipString = Joiner.on(' ').join(unformattedTooltip).toLowerCase().trim();
            return tooltipString.contains(searchText.toLowerCase().substring(1));
        } else if (searchText.startsWith("$")) { // search tags
            List<String> joiner = new ArrayList<>();
            for (ResourceLocation oreId : stack.getTags().map((tagKey) -> tagKey.location()).collect(Collectors.toList())) {
                String oreName = oreId.toString();
                joiner.add(oreName);
            }
            String dictFinal = Joiner.on(' ').join(joiner).toLowerCase().trim();
            return dictFinal.contains(searchText.toLowerCase().substring(1));
        } else {
            return stack.getHoverName().getString().toLowerCase().contains(searchText.toLowerCase());
        }
    }
    
    public boolean canClick() {
        return System.currentTimeMillis() > lastClick + 100L;
    }
    
    int getLines() {
        return lines;
    }
    
    int getColumns() {
        return columns;
    }
    
    public void setLines(int v) {
        lines = v;
    }
    
    void setColumns(int v) {
        columns = v;
    }
    
    public void applyScrollPaging(List<ItemStack> stacksToDisplay) {
        maxPage = stacksToDisplay.size() / (getColumns());
        if (stacksToDisplay.size() % (getColumns()) != 0) {
            maxPage++;
        }
        maxPage -= (getLines() - 1);
        if (maxPage < 1) {
            maxPage = 1;
        }
        if (page < 1) {
            page = 1;
        }
        if (page > maxPage) {
            page = maxPage;
        }
    }
    
    public void mouseScrolled(double mouseButton) {
        // < 0 going down
        // > 0 going up
        if (mouseButton > 0 && page > 1) {
            page--;
        }
        if (mouseButton < 0 && page < maxPage) {
            page++;
        }
    }
    
    public void rebuildItemSlots(List<ItemStack> stacksToDisplay) {
        slots = Lists.newArrayList();
        int index = (page - 1) * (getColumns());
        for (int row = 0; row < getLines(); row++) {
            for (int col = 0; col < getColumns(); col++) {
                if (index >= stacksToDisplay.size()) {
                    break;
                }
                int in = index;
                //        StorageNetwork.LOGGER.info(in + "GUI STORAGE rebuildItemSlots "+stacksToDisplay.get(in));
                slots.add(new ItemSlotNetwork(gui, stacksToDisplay.get(in),
                        gui.getGuiLeft() + 8 + col * 18,
                        gui.getGuiTopFixJei() + 10 + row * 18,
                        stacksToDisplay.get(in).getCount(),
                        gui.getGuiLeft(), gui.getGuiTopFixJei(), true));
                index++;
            }
        }
    }
    
    public boolean inSearchBar(double mouseX, double mouseY) {
        return gui.isInRegion(
                searchBar.x - gui.getGuiLeft(), searchBar.y - gui.getGuiTopFixJei(), // x, y
                searchBar.getWidth(), searchBar.getHeight(), // width, height
                mouseX, mouseY);
    }
    
    public void initSearchbar() {
        searchBar.setBordered(false);
        searchBar.setVisible(true);
        searchBar.setTextColor(16777215);
        //    searchBar.setFocus(StorageNetwork.CONFIG.enableAutoSearchFocus());
        if (Platform.isModLoaded("jei")) {
            initJei();
        }
    }
    
    private void initJei() {
        try {
            if (gui != null && searchBar != null && gui.isJeiSearchSynced()) {
//                searchBar.setValue(JeiHooks.getFilterText());
            }
        } catch (Exception e) {
            StorageNetwork.LOGGER.error("Search bar error ", e);
        }
    }
    
    public void syncTextToJei() {
        if (Platform.isModLoaded("jei") && gui.isJeiSearchSynced()) {
//            JeiHooks.setFilterText(searchBar.getValue());
        }
    }
    
    public void drawGuiContainerForegroundLayer(PoseStack ms, int mouseX, int mouseY, Font font) {
        for (ItemSlotNetwork slot : slots) {
            if (slot != null && slot.isMouseOverSlot(mouseX, mouseY)) {
                slot.drawTooltip(ms, mouseX, mouseY);
                return; // slots and btns do not overlap
            }
        }
        // 
        MutableComponent tooltip = null;
        if (directionBtn != null && directionBtn.isMouseOver(mouseX, mouseY)) {
            tooltip = Component.translatable("gui.storagenetwork.sort");
        } else if (sortBtn != null && sortBtn.isMouseOver(mouseX, mouseY)) {
            tooltip = Component.translatable("gui.storagenetwork.req.tooltip_" + gui.getSort().name().toLowerCase());
        } else if (focusBtn != null && focusBtn.isMouseOver(mouseX, mouseY)) {
            tooltip = Component.translatable("gui.storagenetwork.autofocus.tooltip." + gui.getAutoFocus());
        } else if (Platform.isModLoaded("jei") && jeiBtn != null && jeiBtn.isMouseOver(mouseX, mouseY)) {
            tooltip = Component.translatable(gui.isJeiSearchSynced() ? "gui.storagenetwork.fil.tooltip_jei_on" : "gui.storagenetwork.fil.tooltip_jei_off");
        } else if (this.inSearchBar(mouseX, mouseY)) {
            //tooltip = new TranslationTextComponent("gui.storagenetwork.fil.tooltip_clear");
            if (!Screen.hasShiftDown()) {
                tooltip = Component.translatable("gui.storagenetwork.shift");
            } else {
                List<Component> lis = Lists.newArrayList();
                lis.add(Component.translatable("gui.storagenetwork.fil.tooltip_mod")); //@
                lis.add(Component.translatable("gui.storagenetwork.fil.tooltip_tooltip")); //#
                lis.add(Component.translatable("gui.storagenetwork.fil.tooltip_tags")); //$
                lis.add(Component.translatable("gui.storagenetwork.fil.tooltip_clear")); //clear
                Screen screen = ((Screen) gui);
                screen.renderTooltip(ms, lis, Optional.empty(), mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTopFixJei());
                return; // all done, we have our tts rendered
            }
        }
        //do we have a tooltip
        if (tooltip != null) {
            Screen screen = ((Screen) gui);
            screen.renderTooltip(ms, Lists.newArrayList(tooltip), Optional.empty(), mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTopFixJei());
        }
    }
    
    public void renderItemSlots(PoseStack ms, int mouseX, int mouseY, Font font) {
        stackUnderMouse = ItemStack.EMPTY;
        for (ItemSlotNetwork slot : slots) {
            slot.drawSlot(ms, font, mouseX, mouseY);
            if (slot.isMouseOverSlot(mouseX, mouseY)) {
                stackUnderMouse = slot.getStack();
            }
        }
        if (slots.isEmpty()) {
            stackUnderMouse = ItemStack.EMPTY;
        }
    }
    
    public boolean charTyped(char typedChar, int keyCode) {
        if (searchBar.isFocused() && searchBar.charTyped(typedChar, keyCode)) {
            PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
            syncTextToJei();
            return true;
        }
        return false;
    }
    
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        searchBar.setFocus(false);
        if (inSearchBar(mouseX, mouseY)) {
            searchBar.setFocus(true);
            if (mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
                clearSearch();
                return;
            }
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack stackCarriedByMouse = player.containerMenu.getCarried();
        if (!stackUnderMouse.isEmpty()
                && (mouseButton == UtilTileEntity.MOUSE_BTN_LEFT || mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT)
                && stackCarriedByMouse.isEmpty() &&
                this.canClick()) {
            PacketRegistry.INSTANCE.sendToServer(new RequestMessage(mouseButton, this.stackUnderMouse.copy(), Screen.hasShiftDown(),
                    Screen.hasAltDown() || Screen.hasControlDown()));
            this.lastClick = System.currentTimeMillis();
        } else if (!stackCarriedByMouse.isEmpty() && inField((int) mouseX, (int) mouseY) &&
                this.canClick()) {
            //0 isd getDim()
            PacketRegistry.INSTANCE.sendToServer(new InsertMessage(0, mouseButton));
            this.lastClick = System.currentTimeMillis();
        }
    }
    
    private boolean inField(int mouseX, int mouseY) {
        return mouseX > (gui.getGuiLeft() + 7) && mouseX < (gui.getGuiLeft() + GuiNetworkTable.WIDTH - 7)
                && mouseY > (gui.getGuiTopFixJei() + 7) && mouseY < (gui.getGuiTopFixJei() + fieldHeight);
    }
    
    public void initButtons() {
        int y = this.searchBar.y - 4;
        directionBtn = new ButtonRequest(
                gui.getGuiLeft() + 6, y, "", (p) -> {
            gui.setDownwards(!gui.getDownwards());
            gui.syncDataToServer();
        });
        directionBtn.setHeight(16);
        sortBtn = new ButtonRequest(gui.getGuiLeft() + 22, y, "", (p) -> {
            gui.setSort(gui.getSort().next());
            gui.syncDataToServer();
        });
        sortBtn.setHeight(16);
        if (Platform.isModLoaded("jei")) {
            jeiBtn = new ButtonRequest(gui.getGuiLeft() + 38, y, "", (p) -> {
                gui.setJeiSearchSynced(!gui.isJeiSearchSynced());
                gui.syncDataToServer();
            });
            jeiBtn.setHeight(16);
        }
        focusBtn = new ButtonRequest(
                gui.getGuiLeft() + 166, y + 2, "", (p) -> {
            gui.setAutoFocus(!gui.getAutoFocus());
            gui.syncDataToServer();
            System.out.println("Syncinc focus " + gui.getAutoFocus());
        });
        focusBtn.setHeight(11);
        focusBtn.setWidth(6);
    }
    
    public void sortStackWrappers(List<ItemStack> stacksToDisplay) {
        Collections.sort(stacksToDisplay, new Comparator<ItemStack>() {
            
            final int mul = gui.getDownwards() ? -1 : 1;
            
            @Override
            public int compare(ItemStack o2, ItemStack o1) {
                switch (gui.getSort()) {
                    case AMOUNT:
                        return Integer.compare(o1.getCount(), o2.getCount()) * mul;
                    case NAME:
                        return o2.getHoverName().getString().compareToIgnoreCase(o1.getHoverName().getString()) * mul;
                    case MOD:
                        return UtilTileEntity.getModNameForItem(o2.getItem()).compareToIgnoreCase(UtilTileEntity.getModNameForItem(o1.getItem())) * mul;
                }
                return 0;
            }
        });
    }
    
    public void render() {
        switch (gui.getSort()) {
            case AMOUNT:
                sortBtn.setTextureId(TextureEnum.SORT_AMT);
                break;
            case MOD:
                sortBtn.setTextureId(TextureEnum.SORT_MOD);
                break;
            case NAME:
                sortBtn.setTextureId(TextureEnum.SORT_NAME);
                break;
        }
        focusBtn.setTextureId(gui.getAutoFocus() ? TextureEnum.RED : TextureEnum.GREY);
        directionBtn.setTextureId(gui.getDownwards() ? TextureEnum.SORT_DOWN : TextureEnum.SORT_UP);
        if (jeiBtn != null && Platform.isModLoaded("jei")) {
            jeiBtn.setTextureId(gui.isJeiSearchSynced() ? TextureEnum.JEI_GREEN : TextureEnum.JEI_RED);
        }
    }
}
