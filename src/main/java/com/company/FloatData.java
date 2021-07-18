package com.company;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record FloatData(float... data) implements MeshData {

    public int getBYTES() {
        return Float.BYTES;
    }

    public byte[] toBytes() {
        var buf = ByteBuffer.allocate(Float.BYTES * data.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (var x : data) {
            buf.putFloat(x);
        }
        return buf.array();
    }
}
