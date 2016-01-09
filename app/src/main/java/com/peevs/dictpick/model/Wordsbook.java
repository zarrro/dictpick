package com.peevs.dictpick.model;

/**
 * Created by zarrro on 07.01.16.
 */
public class Wordsbook {

    private final int id;
    private final String name;

    public Wordsbook(int id, String name) {
        if (name == null)
            throw new IllegalArgumentException("name is null");
        if (id < 0)
            throw new IllegalArgumentException("id is negative");
        this.name = name;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
