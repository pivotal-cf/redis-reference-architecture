package com.example.multiredisha;


import java.io.Serializable;
import java.util.Objects;

public class Token implements Serializable {
    private /*final*/ String data;
    private /*final*/ String id;
    public long duration;

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Token() {
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Token(String id, String data) {
        this.id = id;
        this.data = data;
        this.duration = 0;
    }

    public String getData() {
        return this.data;
    }

    public String getId() {
        return this.id;
    }

    public long getDuration() {
        return this.duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return
                Objects.equals(data, token.data) &&
                Objects.equals(id, token.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(data, id);
    }

    @Override
    public String toString() {
        return "Token{" +
                "data='" + data + '\'' +
                ", id='" + id + '\'' +
                ", duration=" + duration +
                '}';
    }
}
