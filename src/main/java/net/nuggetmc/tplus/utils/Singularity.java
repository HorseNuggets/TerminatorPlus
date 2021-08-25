package net.nuggetmc.tplus.utils;

public class Singularity {

    private Object value;

    public Singularity(Object value) {
        this.value = value;
    }

    public Singularity() {
        this.value = null;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean hasValue() {
        return value != null;
    }
}
