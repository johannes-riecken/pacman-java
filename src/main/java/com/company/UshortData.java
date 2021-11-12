package com.company;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;

public final class UshortData implements MeshData {
    private final Target target;
    private final List<Short> data;

    public UshortData(Target target, List<Short> data) {
        this.target = target;
        this.data = data;
    }

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

    public Target target() {
        return target;
    }

    public List<Short> data() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UshortData) obj;
        return Objects.equals(this.target, that.target) &&
                Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, data);
    }

    @Override
    public String toString() {
        return "UshortData[" +
                "target=" + target + ", " +
                "data=" + data + ']';
    }

}
