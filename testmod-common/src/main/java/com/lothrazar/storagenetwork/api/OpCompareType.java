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

public enum OpCompareType {
    
    GREATER, LESS, EQUAL;
    
    public static OpCompareType get(int i) {
        return OpCompareType.values()[i];
    }
    
    public OpCompareType toggle() {
        switch (this) {
            case LESS:
                return EQUAL;
            case EQUAL:
                return GREATER;
            case GREATER:
                return LESS;
        }
        return OpCompareType.EQUAL;
    }
    
    public String symbol() {
        switch (this) {
            case LESS:
                return "<";
            case EQUAL:
                return "=";
            case GREATER:
            default:
                return ">";
        }
    }
    
    public String word() {
        switch (this) {
            case LESS:
                return "less";
            case EQUAL:
                return "eq";
            case GREATER:
            default:
                return "greater";
        }
    }
}
