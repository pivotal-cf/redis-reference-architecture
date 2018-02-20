package com.example.rediscachefailurehandling;

import lombok.Getter;
import java.io.Serializable;

public class Token implements Serializable {
    @Getter private final String data;
    @Getter private final String id;
    @Getter public long duration;

    public Token(String id, String data) {
        this.id = id;
        this.data = data;
        this.duration = 0;
    }
}
