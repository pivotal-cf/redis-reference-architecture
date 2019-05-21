package com.example.rediscachefailurehandling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Token implements Serializable {
    @JsonProperty
    private String data;
    @JsonProperty
    private String id;
    @JsonProperty
    public long duration;

    public Token() {}

    public Token(String id, String data, long duration) {
        this.id = id;
        this.data = data;
        this.duration = duration;
    }
}
