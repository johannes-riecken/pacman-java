package com.company;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public record MeshBuffer(List<MeshData> objects) implements MeshData {
    @Override
    public String toString() {
        return "data:application/octet-stream;base64," + Base64.getEncoder().encodeToString(toBytes());
    }

    @Override
    public byte[] toBytes() {
        var buf = new ArrayList<Byte>();
        for (var x : objects) {
            // align
            var alignByteCount = buf.size() % x.getBYTES();
            for (int i = 0; i < alignByteCount; i++) {
                buf.add((byte) 0);
            }
            for (var y : x.toBytes()) {
                buf.add(y);
            }
        }
        var arr = new byte[buf.size()];
        for (int i = 0; i < buf.size(); i++) {
            arr[i] = buf.get(i);
        }
        return arr;
    }

    @Override
    public int getBYTES() {
        return 0;
    }
}
