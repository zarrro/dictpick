package com.peevs.dictpick;

/**
 * Created by zarrro on 12.10.2015 г..
 */
public enum Language {
    EN, BG, DE;

    public static Language val(String s) {
        return Language.valueOf(s.trim().toUpperCase());
    }
}
