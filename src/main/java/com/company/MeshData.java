package com.company;

// must have non-zero length underlying data array
public interface MeshData {
    byte[] toBytes();
    Target target();
    // get the number of bytes of the element data type
    int getBYTES();
    int getComponentType();
    int count();
    String type();
    float[] getMax();
    float[] getMin();
}
