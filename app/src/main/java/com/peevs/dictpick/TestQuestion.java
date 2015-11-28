package com.peevs.dictpick;

import android.os.Parcel;
import android.os.Parcelable;

import com.peevs.dictpick.model.Text;
import com.peevs.dictpick.model.TextEntry;

import java.util.Arrays;

/**
 * Created by zarrro on 14.9.2015 г..
 */
public class TestQuestion implements Parcelable {

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        // Store test question like a string array, with the format:
        // index 0 - the question word entry
        // index 1 - index of the correct answer
        // index 2..end - the word entries of the answer options
        String[] testQuestionString = new String[options.length + 2];
        testQuestionString[0] = getTextEntryAsString(question);
        testQuestionString[1] = String.valueOf(correctOptionIndex);
        for (int i = 2; i < testQuestionString.length; i++) {
            testQuestionString[i] = getTextEntryAsString(options[i - 2]);
        }
        out.writeStringArray(testQuestionString);
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

    private String getTextEntryAsString(TextEntry e) {
        return e.getId() + "::" + e.getText().getVal() + "::" + e.getText().getLang();
    }

    private TextEntry getTextEntryFromString(String s) {
        String[] parts = s.split("::");
        return new TextEntry(new Text(parts[1], Language.valueOf(parts[2])), Long.valueOf(parts[0]));
    }

    private TestQuestion(Parcel in) {
        String[] data = new String[WRONG_OPTIONS_COUNT + 3];
        in.readStringArray(data);

        this.question = getTextEntryFromString(data[0]);
        this.correctOptionIndex = Integer.valueOf(data[1]);
        this.options = new TextEntry[WRONG_OPTIONS_COUNT + 1];
        for (int i = 0; i < this.options.length; ++i) {
            this.options[i] = getTextEntryFromString(data[i + 2]);
        }
    }

    public TestQuestion() {
    }

    public static final int WRONG_OPTIONS_COUNT = 4;

    private TextEntry question;
    private TextEntry[] options;
    private int correctOptionIndex;
    private Language questionLanguage;
    private Language optionLanguage;

    public TextEntry getQuestion() {
        return question;
    }

    public void setQuestion(TextEntry question) {
        this.question = question;
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

    public TextEntry getCorrectOptionWordEntry() {
        return options[correctOptionIndex];
    }

    public void setCorrectOptionIndex(int correctAnswerIndex) {
        this.correctOptionIndex = correctAnswerIndex;
    }

    public Language getQuestionLanguage() {
        return questionLanguage;
    }

    public void setQuestionLanguage(Language questionLanguage) {
        this.questionLanguage = questionLanguage;
    }

    public Language getOptionLanguage() {
        return optionLanguage;
    }

    public void setOptionLanguage(Language optionLanguage) {
        this.optionLanguage = optionLanguage;
    }

    @Override
    public String toString() {
        return "TestQuestion{" +
                "question=" + question +
                ", options=" + Arrays.toString(options) +
                ", correctOptionIndex=" + correctOptionIndex +
                '}';
    }
}
