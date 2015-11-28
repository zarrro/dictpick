package com.peevs.dictpick.model;

import com.peevs.dictpick.Language;

/**
 * Created by zarrro on 28.11.15.
 */
public class Question {

    private final static String SEP = "::";
    protected boolean inverse;
    protected TranslationEntry question;

    public Question(TranslationEntry question) {
        if (question == null)
            throw new IllegalArgumentException("question");
        this.question = question;
    }

    public Text getQuestion() {
        return inverse ? question.getTargetText() : question.getSrcText();
    }

    public boolean isInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    protected Question() {

    }

    protected TranslationEntry translationEntryFromString(String s) {
        String[] parts = s.split(SEP);
        return new TranslationEntry(
                Long.valueOf(parts[0]),
                strToText(parts[1], parts[2]),
                strToText(parts[3], parts[4]));
    }

    protected String translationEntryToString(TranslationEntry te) {
        return te.getId() + SEP + textToStr(te.getSrcText()) + SEP + textToStr(te.getTargetText());
    }

    protected String textToStr(Text t) {
        return t.getVal() + SEP + t.getLang();
    }

    protected Text strToText(String... s) {
        return new Text(s[0], Language.valueOf(s[1]));
    }
}
