package com.example.rediscachefailurehandling;

import lombok.Getter;

import java.io.Serializable;


@SuppressWarnings("serial")
public class Token implements Serializable {
    @Getter private final String data;
    @Getter private final String id;

    public Token(String id, String data) {
        this.id = id;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Token token = (Token) o;

        return this.id.equals(token.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
