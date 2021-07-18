package com.company;

public enum Type {
    Scalar("SCALAR"),
    Vec3("VEC3"),
    ;
    public String value;

    Type(String value) {
        this.value = value;
    }
}
