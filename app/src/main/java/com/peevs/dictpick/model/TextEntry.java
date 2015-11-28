package com.peevs.dictpick.model;

/**
 * Created by zarrro on 28.11.15.
 */
public class TextEntry {

    private final long id;
    private final Text text;

    public  TextEntry(Text text, long id) {
        this.text = text;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Text getText() {
        return text;
    }

    @Override
    public String toString() {
        return "TextEntry{" +
                "id=" + id +
                ", text=" + text +
                '}';
    }
}
