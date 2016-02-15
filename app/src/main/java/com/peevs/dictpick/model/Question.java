package com.peevs.dictpick.model;

import com.peevs.dictpick.Language;

/**
 * Created by zarrro on 28.11.15.
 */
public class Question {

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
}
