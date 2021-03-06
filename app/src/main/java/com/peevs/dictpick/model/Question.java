package com.peevs.dictpick.model;

/**
 * Created by zarrro on 28.11.15.
 */
public abstract class Question {

    public enum Type {
        TEST,
        OPEN
    }

    public static final int MAX_RATING = 1000;
    public static final int MIN_RATING = 0;

    protected boolean inverse;
    protected TranslationEntry question;

    protected Object lastWrongAnswer;

    public Question(TranslationEntry question) {
        if (question == null)
            throw new IllegalArgumentException("question");
        this.question = question;
    }

    public Text getQuestionText() {
        return inverse ? question.getTargetText() : question.getSrcText();
    }

    public boolean isInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public abstract TextEntry getCorrectAnswer();

    protected Question() {
        this.question = null;
    }

    private void updateOnCorrectAnswer() {
        int newRating = question.getRating() + (MAX_RATING - question.getRating())
                / getCoeficientForCorrect();
        if (newRating >= MAX_RATING) {
            question.setRating(MAX_RATING);
        } else {
            question.setRating(newRating);
        }
    }

    private void updateOnWrongAnswer() {
        int newRating = question.getRating() - (MAX_RATING - question.getRating())
                / getCoeficientForWrong();
        if (newRating <= MIN_RATING) {
            question.setRating(MIN_RATING);
        } else {
            question.setRating(newRating);
        }
    }

    public TranslationEntry getQuestion() {
        return question;
    }

    protected abstract int getCoeficientForCorrect();

    protected abstract int getCoeficientForWrong();

    public boolean checkAnswer(Object answer) {
        if (answer == null)
            throw new IllegalArgumentException("answer is null");

        boolean isCorrect = isAnswerCorrect(answer);
        if (isCorrect) {
            updateOnCorrectAnswer();
            lastWrongAnswer = -1;
        } else {
            updateOnWrongAnswer();
            lastWrongAnswer = answer;
        }
        return isCorrect;
    }

    protected abstract boolean isAnswerCorrect(Object answer);

    public abstract Type getType();

    public Object getLastWrongAnswer() {
        return lastWrongAnswer;
    }
}
