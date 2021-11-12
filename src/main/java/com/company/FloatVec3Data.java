package com.company;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;

public final class FloatVec3Data implements MeshData {

    private static int nComponents = 3;
    private final Target target;
    private final List<float[]> data;

    public FloatVec3Data(Target target, List<float[]> data) {
        this.target = target;
        this.data = data;
    }

    public int getBYTES() {
        return Float.BYTES;
    }

    public int getComponentType() {
        return ComponentType.Float.value;
    }

    public float[] getMax() {
        float[] res = new float[3];
        res[0] = Float.NEGATIVE_INFINITY;
        res[1] = Float.NEGATIVE_INFINITY;
        res[2] = Float.NEGATIVE_INFINITY;
        for (var x : data) {
            res[0] = Float.max(res[0], x[0]);
            res[1] = Float.max(res[1], x[1]);
            res[2] = Float.max(res[2], x[2]);
        }
        return res;
    }

    public float[] getMin() {
        float[] res = new float[3];
        res[0] = Float.POSITIVE_INFINITY;
        res[1] = Float.POSITIVE_INFINITY;
        res[2] = Float.POSITIVE_INFINITY;
        for (var x : data) {
            res[0] = Float.min(res[0], x[0]);
            res[1] = Float.min(res[1], x[1]);
            res[2] = Float.min(res[2], x[2]);
        }
        return res;
    }

    public int count() {
        return data.size();
    }

    @Override
    public String type() {
        return Type.Vec3.value;
    }

    public byte[] toBytes() {
        var buf = ByteBuffer.allocate(Float.BYTES * nComponents * data.size());
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (var x : data) {
            for (var y : x)
                buf.putFloat(y);
        }
        return buf.array();
    }

    public Target target() {
        return target;
    }

    public List<float[]> data() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FloatVec3Data) obj;
        return Objects.equals(this.target, that.target) &&
                Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, data);
    }

    @Override
    public String toString() {
        return "FloatVec3Data[" +
                "target=" + target + ", " +
                "data=" + data + ']';
    }

}
