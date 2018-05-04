package br.com.kepler.diffjson;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public enum EditOperation {
    INSERT     ("I"),
    SUBSTITUTE ("S"),
    DELETE     ("D"),
    NONE       ("N");

    private final String s;

    private EditOperation(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return s;
    }
}


