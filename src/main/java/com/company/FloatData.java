package com.company;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;

public final class FloatData implements MeshData {
    private final Target target;
    private final List<Float> data;

    public FloatData(Target target, List<Float> data) {
        this.target = target;
        this.data = data;
    }

    @Override
    public int getBYTES() {
        return Float.BYTES;
    }

    @Override
    public int getComponentType() {
        return ComponentType.Float.value;
    }

    @Override
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

    @Override
    public byte[] toBytes() {
        var buf = ByteBuffer.allocate(Float.BYTES * data.size());
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (var x : data) {
            buf.putFloat(x);
        }
        return buf.array();
    }

    @Override
    public Target target() {
        return target;
    }

    public List<Float> data() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FloatData) obj;
        return Objects.equals(this.target, that.target) &&
                Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, data);
    }

    @Override
    public String toString() {
        return "FloatData[" +
                "target=" + target + ", " +
                "data=" + data + ']';
    }

}
