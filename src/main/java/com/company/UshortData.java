package com.company;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public record UshortData(Target target, List<Short> data) implements MeshData {

    public int getBYTES() {
        return Short.BYTES;
    }

    public int getComponentType() {
        return ComponentType.UShort.value;
    }

    public int count() {
        return data.size();
    }

    public float[] getMax() {
        return new float[]{data.stream().max(Short::compareTo).get()};
    }

    public float[] getMin() {
        return new float[]{data.stream().min(Short::compareTo).get()};
    }

    public String type() {
        return Type.Scalar.value;
    }

    public byte[] toBytes() {
        var buf = ByteBuffer.allocate(Short.BYTES * data.size());
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (var x : data) {
            buf.putShort(x);
        }
        return buf.array();
    }
}
