package com.example.rediscachefailurehandling;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Token implements Serializable {
    private String data;
    private String id;
    public long duration;

    public Token(String id, String data) {
        this.id = id;
        this.data = data;
        this.duration = 0;
    }

}
