package com.company;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record MeshBuffer(List<MeshData> objects) implements MeshData {
    @Override
    public String toString() {
        return "data:application/octet-stream;base64," + Base64.getEncoder().encodeToString(toBytes());
    }

    @Override
    public byte[] toBytes() {
        var buf = toBytesHelper().stream().flatMap(Collection::stream).collect(Collectors.toList());
        byte[] res = new byte[buf.size()];
        for (var i = 0; i < buf.size(); i++) {
            res[i] = buf.get(i);
        }
        return res;
    }

    public List<List<Byte>> toBytesHelper() {
        var buf = new ArrayList<List<Byte>>();
        for (var i = 0; i < objects.size(); i++) {
            var paddedObjectBytes = new ArrayList<Byte>();
            for (var b : objects.get(i).toBytes()) {
                paddedObjectBytes.add(b);
            }
            var alignByteCount = i == objects.size() - 1 ? 0 : paddedObjectBytes.size() % objects.get(i + 1).getBYTES();
            for (var j = 0; j < alignByteCount; j++) {
                paddedObjectBytes.add((byte) 0);
            }
            buf.add(paddedObjectBytes);
        }
        return buf;
    }

    @Override
    public int getBYTES() {
        return 0;
    }

    @Override
    public Target target() {
        return null;
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public int getComponentType() {
        return 0;
    }

    @Override
    public String type() {
        return "";
    }

    @Override
    public float[] getMax() {
        return null;
    }

    @Override
    public float[] getMin() {
        return null;
    }
}
