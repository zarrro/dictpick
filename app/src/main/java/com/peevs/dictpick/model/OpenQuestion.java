package com.peevs.dictpick.model;

/**
 * Created by zarrro on 28.11.15.
 */
public class OpenQuestion extends Question {

    public OpenQuestion(TranslationEntry translation) {
        super(translation);
    }

    public TextEntry getAnswerText;

    @Override
    protected int getCoeficientForCorrect() {
        return 2;
    }

    @Override
    protected int getCoeficientForWrong() {
        return 4;
    }

    public TextEntry getCorrectAnswer() {
        if(inverse) {
            return new TextEntry(question.getTargetText(), question.getId());
        } else {
            return new TextEntry(question.getSrcText(), question.getId());
        }
    }

    @Override
    public boolean isAnswerCorrect(Object answer) {
        if(!(answer instanceof String))
            throw new IllegalArgumentException("answer is not String, but text answer is expected");

        return ((String) answer).trim().equalsIgnoreCase(getCorrectAnswer().getText().getVal().
                trim());
    }

    @Override
    public Type getType() {
        return Type.OPEN;
    }
}
