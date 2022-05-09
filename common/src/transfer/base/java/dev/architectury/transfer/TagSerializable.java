package dev.architectury.transfer;

import net.minecraft.nbt.Tag;

public interface TagSerializable<T extends Tag> {
    T save(T tag);
    
    void load(T tag);
}
