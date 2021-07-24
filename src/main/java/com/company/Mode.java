package com.company;

public enum Mode {
    Points(0),
    Lines(1),
    LineLoop(2),
    LineStrip(3),
    Triangles(4),
    TriangleStrip(5),
    TriangleFan(6);

    public int value;

    Mode(int value) {
        this.value = value;
    }
}
