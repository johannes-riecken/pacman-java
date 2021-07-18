package com.company;

public enum ComponentType {
    Byte(5120),
    UByte(5121),
    Short(5122),
    UShort(5123),
    Int(5124),
    UInt(5125),
    Float(5126),
    ;

    ComponentType(int value) {
        this.value = value;
    }

    int value;
}
