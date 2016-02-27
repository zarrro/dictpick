package com.peevs.dictpick.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.peevs.dictpick.Language;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by zarrro on 14.9.2015 г..
 */
public class TestQuestion extends Question implements Parcelable {

    public static final int WRONG_OPTIONS_COUNT = 4;
    // question, inverse, correctOptionIndex, correctOption + wrong options
    private static final int PARCEL_DATA_SIZE = WRONG_OPTIONS_COUNT + 4;
    private TextEntry[] options;
    private int correctOptionIndex;

    private String getTextEntryAsString(TextEntry e) {
        return e.getId() + "::" + e.getText().getVal() + "::" + e.getText().getLang();
    }

    private TextEntry getTextEntryFromString(String s) {
        String[] parts = s.split("::");
        return new TextEntry(new Text(parts[1], Language.valueOf(parts[2])), Long.valueOf(parts[0]));
    }

    private TestQuestion(Parcel in) {
        String[] data = new String[PARCEL_DATA_SIZE];
        in.readStringArray(data);
        int index = 0;
        question = TranslationEntry.fromString(data[index++]);
        correctOptionIndex = Integer.valueOf(data[index++]);
        inverse = Boolean.valueOf(data[index++]);
        options = new TextEntry[WRONG_OPTIONS_COUNT + 1];
        for (int i = 0; i < options.length; i++) {
            options[i] = getTextEntryFromString(data[index++]);
        }
    }

    public TestQuestion(TranslationEntry translation) {
        super(translation);
    }

    public void setQuestion(TranslationEntry translationEntry) {
        question = translationEntry;
        // re-initialize the correct option TextEntry
        if(inverse) {
            options[correctOptionIndex] = new TextEntry(question.getSrcText(), question.getId());
        } else {
            options[correctOptionIndex] = new TextEntry(question.getTargetText(), question.getId());
        }
    }



    protected boolean isAnswerCorrect(Object answer) {
        if(!(answer instanceof Integer))
            throw new IllegalArgumentException("answer is not integer, answer index is expected");
        return ((Integer) answer).intValue() == getCorrectOptionIndex();
    }

    public static final Parcelable.Creator<TestQuestion> CREATOR
            = new Parcelable.Creator<TestQuestion>() {

        public TestQuestion createFromParcel(Parcel in) {
            return new TestQuestion(in);
        }

        public TestQuestion[] newArray(int size) {
            return new TestQuestion[size];
        }
    };

    public void writeToParcel(Parcel out, int flags) {
        // Store test question like a string array, with the format:
        // index 0 - the question word entry
        // index 1 - index of the correct answer
        // index 2 - inverse value
        // index 3..end - the word entries of the answer options
        String[] testQuestionString = new String[PARCEL_DATA_SIZE];
        int index = 0;
        testQuestionString[index++] = question.toString();
        testQuestionString[index++] = String.valueOf(correctOptionIndex);
        testQuestionString[index++] = Boolean.toString(inverse);
        for (int i = 0; i < options.length; i++) {
            testQuestionString[index++] = getTextEntryAsString(options[i]);
        }
        out.writeStringArray(testQuestionString);
    }

    public TextEntry[] getOptions() {
        return options;
    }

    public void setOptions(TextEntry[] options) {
        this.options = options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public TextEntry getCorrectAnswer() {
        return options[correctOptionIndex];
    }

    public void setCorrectOptionIndex(int correctAnswerIndex) {
        this.correctOptionIndex = correctAnswerIndex;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    protected int getCoeficientForCorrect() {
        return 4;
    }

    @Override
    protected int getCoeficientForWrong() {
        return 2;
    }

    @Override
    public String toString() {
        return "TestQuestion{" +
                "question=" + question +
                ", options=" + Arrays.toString(options) +
                ", correctOptionIndex=" + correctOptionIndex +
                '}';
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }
}
