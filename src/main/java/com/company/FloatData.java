package com.company;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public record FloatData(Target target, List<Float> data) implements MeshData {

    public int getBYTES() {
        return Float.BYTES;
    }

    public int getComponentType() {
        return ComponentType.Float.value;
    }

    public int count() {
        return data.size();
    }

    @Override
    public String type() {
        return Type.Scalar.value;
    }

    @Override
    public float[] getMax() {
        return new float[]{data.stream().max(Float::compareTo).get()};
    }

    @Override
    public float[] getMin() {
        return new float[]{data.stream().min(Float::compareTo).get()};
    }

    public byte[] toBytes() {
        var buf = ByteBuffer.allocate(Float.BYTES * data.size());
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (var x : data) {
            buf.putFloat(x);
        }
        return buf.array();
    }
}
