package com.company;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record UshortData(short... data) implements MeshData {

    public int getBYTES() {
        return Short.BYTES;
    }

    public byte[] toBytes() {
        var buf = ByteBuffer.allocate(Short.BYTES * data.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (var x : data) {
            buf.putShort(x);
        }
        return buf.array();
    }
}
