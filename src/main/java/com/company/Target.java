package com.company;

public enum Target {
    ArrayBuffer(34962),
    ElementArrayBuffer(34963);
    int value;

    Target(int value) {
        this.value = value;
    }
}
