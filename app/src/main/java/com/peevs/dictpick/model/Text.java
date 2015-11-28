package com.peevs.dictpick.model;

import com.peevs.dictpick.Language;

/**
 * Created by zarrro on 28.11.15.
 */
public class Text {
    private final Language lang;
    private final String val;

    public Text(String val, Language lang) {
        this.val = val;
        this.lang = lang;
    }

    public Language getLang() {
        return lang;
    }

    public String getVal() {
        return val;
    }

    @Override
    public String toString() {
        return "Text{" +
                "lang=" + lang +
                ", val='" + val + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Text)) return false;

        Text text = (Text) o;

        if (lang != text.lang) return false;
        return val.equals(text.val);

    }

    @Override
    public int hashCode() {
        int result = lang.hashCode();
        result = 31 * result + val.hashCode();
        return result;
    }
}
