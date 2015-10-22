package com.peevs.dictpick;

import android.os.Parcel;
import android.os.Parcelable;

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
        testQuestionString[0] = wordEntryAsString(question);
        testQuestionString[1] = String.valueOf(correctAnswerIndex);
        for (int i = 2; i < testQuestionString.length; i++) {
            testQuestionString[i] = wordEntryAsString(options[i - 2]);
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

    private String wordEntryAsString(WordEntry e) {
        return e.getId() + "=" + e.getText();
    }

    private WordEntry wordEntryFromString(String s) {
        String[] parts = s.split("=");
        return new WordEntry(Integer.valueOf(parts[0]), parts[1]);
    }

    private TestQuestion(Parcel in) {
        String[] data = new String[WRONG_OPTIONS_COUNT + 3];
        in.readStringArray(data);

        this.question = wordEntryFromString(data[0]);
        this.correctAnswerIndex = Integer.valueOf(data[1]);
        this.options = new WordEntry[WRONG_OPTIONS_COUNT + 1];
        for (int i = 0; i < this.options.length; ++i) {
            this.options[i] = wordEntryFromString(data[i + 2]);
        }
    }

    public TestQuestion() {
    }

    public static final int WRONG_OPTIONS_COUNT = 4;

    public static class WordEntry {
        private final int id;
        private final String text;

        public WordEntry(int id, String text) {
            this.id = id;
            this.text = text;
        }

        /**
         * @return - id of the translation so it can be tracked in statistics.
         */
        public int getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "WordEntry{" +
                    "id=" + id +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    private WordEntry question;
    private WordEntry[] options;
    private int correctAnswerIndex;
    private Language questionLanguage;
    private Language optionLanguage;

    public WordEntry getQuestion() {
        return question;
    }

    public void setQuestion(WordEntry question) {
        this.question = question;
    }

    public WordEntry[] getOptions() {
        return options;
    }

    public void setOptions(WordEntry[] options) {
        this.options = options;
    }

    public int getCorrectOptionIndex() {
        return correctAnswerIndex;
    }

    public WordEntry getCorrectOptionWordEntry() {
        return options[correctAnswerIndex];
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
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
                ", correctAnswerIndex=" + correctAnswerIndex +
                '}';
    }
}
