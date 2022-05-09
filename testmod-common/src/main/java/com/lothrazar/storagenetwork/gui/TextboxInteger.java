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
//package com.lothrazar.cyclic.gui;

import com.lothrazar.storagenetwork.network.CableIOMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;

/**
 * https://github.com/Lothrazar/Cyclic/blob/61887dc2b69541a553bb0259347d13d6f9d7730e/src/main/java/com/lothrazar/cyclic/gui/TextboxInteger.java
 */
public class TextboxInteger extends EditBox {
    
    private static final int KEY_DELETE = 261;
    private static final int KEY_BACKSPACE = 259;
    
    public TextboxInteger(Font fontIn, int xIn, int yIn, int widthIn) {
        super(fontIn, xIn, yIn, widthIn, 16, null);
        this.setMaxLength(2);
        this.setBordered(true);
        this.setVisible(true);
        this.setTextColor(16777215);
    }
    
    @Override
    protected void onFocusedChanged(boolean onFocusedChanged) {
        super.onFocusedChanged(onFocusedChanged);
        saveValue();
    }
    
    @Override
    public boolean keyPressed(int key, int mx, int my) {
        if (key == KEY_BACKSPACE || key == KEY_DELETE) {
            saveValue();
        }
        return super.keyPressed(key, mx, my);
    }
    
    private void saveValue() {
        PacketRegistry.INSTANCE.sendToServer(new CableIOMessage(CableIOMessage.CableMessageType.SYNC_OP_TEXT.ordinal(), this.getCurrent(), false));
    }
    
    @Override
    public boolean charTyped(char chr, int p) {
        if (!Character.isDigit(chr)) {
            return false;
        }
        boolean worked = super.charTyped(chr, p);
        if (worked) {
            saveValue();
        }
        return worked;
    }
    
    public int getCurrent() {
        try {
            return Integer.parseInt(this.getValue());
        } catch (Exception e) {
            return 0;
        }
    }
}
