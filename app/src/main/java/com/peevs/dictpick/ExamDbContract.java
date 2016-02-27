package com.peevs.dictpick;

import android.provider.BaseColumns;

/**
 * Created by zarrro on 16.8.2015 г..
 */
public final class ExamDbContract {

    public static final String TEXT_TYPE = " TEXT";
    public static final String INT_TYPE = " INTEGER";
    public static final String COMMA_SEP = ",";

    private ExamDbContract() {
    }

    public static final class WordsTable implements BaseColumns {
        public static final String TABLE_NAME = "words";
        public static final String S_TEXT = "s_text";
        public static final String T_TEXT = "t_text";
        public static final String S_LANG = "s_lang";
        public static final String T_LANG = "t_lang";
        public static final String RATING = "rating";
        public static final String TESTORDER = "tesorder";
        public static final String BOOKID = "book_id";
        public static final String SQL_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        S_TEXT + TEXT_TYPE + COMMA_SEP +
                        T_TEXT + TEXT_TYPE + COMMA_SEP +
                        S_LANG + TEXT_TYPE + COMMA_SEP +
                        T_LANG + TEXT_TYPE + COMMA_SEP +
                        RATING + INT_TYPE + COMMA_SEP +
                        TESTORDER + INT_TYPE + COMMA_SEP +
                        " UNIQUE ( " + S_TEXT + COMMA_SEP + T_TEXT + " ))";

        public static final String SQL_DELETE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final int DEFAULT_BOOK_ID = 1;

        // used as identifier of the content for usage in CursorLoader
        public static final String CONTENT_URI = "content://examdb." + TABLE_NAME;
    }

    public static final class AnswerStats implements BaseColumns {

        public static final String TABLE_NAME = "answer_stats";
        public static final String QUESTION_WORD_ID = "question_word_id";
        public static final String QUESTION_TYPE = "question_type";
        public static final String WRONG_ANSWER_WORD_ID = "wrong_answer_word_id";
        public static final String WRONG_ANSWER_TEXT = "wrong_answer_text";
        public static final String TIME_STAMP = "time_stamp";

        public static final String SQL_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        QUESTION_WORD_ID + INT_TYPE + COMMA_SEP +
                        WRONG_ANSWER_WORD_ID + INT_TYPE + COMMA_SEP +
                        TIME_STAMP + TEXT_TYPE + COMMA_SEP +
                        WordsTable.T_LANG + TEXT_TYPE + COMMA_SEP + ")";


        public static final String DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class WordbookTable implements BaseColumns {

        public static final String TABLE_NAME = "wordbook";
        public static final String NAME = "name";

        public static final String SQL_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        NAME + TEXT_TYPE;
    }

}
