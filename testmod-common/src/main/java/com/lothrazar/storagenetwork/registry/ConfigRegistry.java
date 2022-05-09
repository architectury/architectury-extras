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

package com.lothrazar.storagenetwork.registry;

import java.nio.file.Path;
import java.util.List;

public class ConfigRegistry {
    private static boolean LOGSPAM = false;
    private static int REFRESHTICKS = 20;
    public static int EXCHANGEBUFFER = 1024;
    private static boolean RELOADONCHUNK = false;
    private static List<String> IGNORELIST = List.of("extrautils2:playerchest");
    public static int ITEMRANGE = -1;
    public static int RECIPEMAXTAGS = 64;
    public static boolean JEINEGATIVECRASH = true;
    
    static {
        initConfig();
    }
    
    private static void initConfig() {
        /*COMMON_BUILDER.comment("General settings").push(StorageNetwork.MODID);
        JEINEGATIVECRASH = COMMON_BUILDER.comment("If true, this will prevent jei from crashing on smaller gui scale/screen resolutions because -8<0, and instead pushes the players items down off the screen a bit.  "
                        + "False means everything will try to render perfectly as in older verisons but will not try to prevent any possible crashes")
                .define("offsetItemsPreventJeiCrashNegativeNumbers", true);
        LOGSPAM = COMMON_BUILDER.comment("Enable very spammy logs.  Sometimes useful for debugging. ").define("logSpam", false);
        RELOADONCHUNK = COMMON_BUILDER.comment(
                        "\r\n If this is true, reload network when a chunk unloads, this keeps your network always up to date.  It has been reported that this cause lag and chunk load issues on servers, "
                                + "so disable if you have any problems. ")
                .define("reloadNetworkWhenUnloadChunk", false);
        REFRESHTICKS = COMMON_BUILDER.comment("\r\n How often to auto-refresh a network (one second is 20 ticks)").defineInRange("autoRefreshTicks", 20, 2, 4096);
        List<String> list = new ArrayList<String>();
        list.add("extrautils2:playerchest");
        IGNORELIST = COMMON_BUILDER.comment("\r\n Disable these blocks from ever being able to connect to the network, they will be treated as a non-inventory.").define("NotallowedBlocks",
                list);
        EXCHANGEBUFFER = COMMON_BUILDER.comment("\r\n How many itemstacks from the network are visible to external connections through the storagenetwork:exchange.  "
                        + "Too low and not all items can pass through, too large and there will be packet/buffer overflows.")
                .defineInRange("exchangeBufferSize", 1024, 1, 5000);
        ITEMRANGE = COMMON_BUILDER.comment("\r\n Maximum range of the Storage Remote and Crafting Remote.   -1 means unlimited.")
                .defineInRange("remoteMaxRange", -1, -1, Integer.MAX_VALUE / 256);
        RECIPEMAXTAGS = COMMON_BUILDER.comment("\r\n When matching items to recipes in the JEI + button, this is the maximum number of tags to serialize over the network when on a server.  Reduce if you get errors relating to Packet Sizes being too large (Minecraft 1.12.2 had this hardcoded at 5).")
                .defineInRange("jeiMaximumRecipeTags", 64, 5, 128);
        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();*/
    }
    
    public ConfigRegistry(Path path) {
        /*final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();
        configData.load();
        COMMON_CONFIG.setConfig(configData);*/
    }
    
    public boolean logspam() {
        return LOGSPAM;
    }
    
    public boolean doReloadOnChunk() {
        return RELOADONCHUNK;
    }
    
    public int refreshTicks() {
        return REFRESHTICKS;
    }
    
    public List<String> ignorelist() {
        return IGNORELIST;
    }
}
