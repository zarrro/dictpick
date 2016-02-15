package com.peevs.dictpick.model;

import com.peevs.dictpick.Language;

/**
 * Created by zarrro on 28.11.15.
 */
public class Text {
    public final static String SEP = "::";
    private final Language lang;

    public Text(String val, Language lang) {
        this.val = val;
        this.lang = lang;
    }

    private String val;

    public String getVal() {
        synchronized (this) {
            return val;
        }
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

    @Override
    public String toString() {
        return getVal() + SEP + getLang();
    }

    public static Text strToText(String... s) {
        return new Text(s[0], Language.valueOf(s[1]));
    }

    public void setVal(String val) {
        synchronized (this) {
            this.val = val;
        }
    }

    public Language getLang() {
        return lang;
    }

}
