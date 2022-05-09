package dev.architectury.transfer.energy.simple;

import dev.architectury.transfer.TagSerializable;
import dev.architectury.transfer.energy.wrapper.BaseEnergyTransferHandler;
import net.minecraft.nbt.CompoundTag;

public class SimpleEnergyTransferHandler implements BaseEnergyTransferHandler, TagSerializable<CompoundTag> {
    protected long stored;
    protected long capacity;
    
    public SimpleEnergyTransferHandler() {
        this(0, 0);
    }
    
    public SimpleEnergyTransferHandler(long stored, long capacity) {
        this.stored = stored;
        this.capacity = capacity;
    }
    
    @Override
    public long getCapacity(Long resource) {
        return this.capacity;
    }
    
    @Override
    public Long getResource() {
        return this.stored;
    }
    
    @Override
    public void setResource(long resource) {
        this.stored = resource;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("Energy", this.stored);
        tag.putLong("Capacity", this.capacity);
        return tag;
    }
    
    @Override
    public void load(CompoundTag tag) {
        this.stored = tag.getLong("Energy");
        this.capacity = tag.getLong("Capacity");
    }
    
    @Override
    public void close() {
    }
}
