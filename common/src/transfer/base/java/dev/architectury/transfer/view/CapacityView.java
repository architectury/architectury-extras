package dev.architectury.transfer.view;

public interface CapacityView<T> {
    /**
     * Returns the capacity of this view.
     *
     * @return the capacity
     */
    long getCapacity(T resource);
}
