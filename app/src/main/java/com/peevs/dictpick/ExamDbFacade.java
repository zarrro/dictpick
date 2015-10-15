package com.peevs.dictpick;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;
import java.util.Random;

import static com.peevs.dictpick.ExamDbContract.UNIQUE_CONTRAINT_FAILED_ERR_CODE;
import static com.peevs.dictpick.TestQuestion.*;

/**
 * Created by zarrro on 13.9.2015 г..
 */
public class ExamDbFacade {

    public static class AnswerStatsEntry {

        private long questionWordId;
        private long wrongAnswerWordId;
        private Date timestamp;

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public long getQuestionWordId() {
            return questionWordId;
        }

        public void setQuestionWordId(long questionWordId) {
            this.questionWordId = questionWordId;
        }

        public long getWrongAnswerWordId() {
            return wrongAnswerWordId;
        }

        public void setWrongAnswerWordId(long wrongAnswerWordId) {
            this.wrongAnswerWordId = wrongAnswerWordId;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "AnswerStatsEntry{" +
                    "questionWordId=" + questionWordId +
                    ", wrongAnswerWordId=" + wrongAnswerWordId +
                    ", timestamp=" + timestamp.toString() +
                    '}';
        }
    }

    public static class AnswerStats {

        private long count;

        private float successRate;

        public float getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(float successRate) {
            this.successRate = successRate;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    private final SQLiteOpenHelper sqliteHelper;
    private static final String TAG = ExamDbHelper.class.getSimpleName();
    private Random rand = new Random(System.currentTimeMillis());

    public ExamDbFacade(SQLiteOpenHelper sqliteHelper) {
        if (sqliteHelper == null)
            throw new IllegalArgumentException();
        this.sqliteHelper = sqliteHelper;
    }

    public TestQuestion getRandomTestQuestion(Language srcLang, Language targetLang, int wrongOptionsCount) {
        SQLiteDatabase examDb = null;
        Cursor c = null;
        try {
            examDb = sqliteHelper.getReadableDatabase();
            c = queryAllTranslations(srcLang, targetLang, examDb);
            if(c.getCount() <= 0) {
                return null;
            }
            return getRandomTestQuestion(wrongOptionsCount, c);
        } finally {
            if (c != null) c.close();
            if (examDb != null) examDb.close();
        }
    }

    public long saveTranslation(String sourceText, String targetText, String sourceLang,
                                String targetLang) {

        Log.i(TAG, String.format("saveTranslation - sourceText %s, targetText %s, sourceLang %s," +
                "targetLang %s", sourceText, targetText, sourceLang, targetLang));

        SQLiteDatabase examDb = null;
        try {
            examDb = sqliteHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(ExamDbContract.WordsTable.S_TEXT, sourceText);
            values.put(ExamDbContract.WordsTable.T_TEXT, targetText);
            values.put(ExamDbContract.WordsTable.S_LANG, sourceLang);
            values.put(ExamDbContract.WordsTable.T_LANG, targetLang);
            return examDb.insertOrThrow(ExamDbContract.WordsTable.TABLE_NAME, "null", values);
        } catch (SQLiteConstraintException e) {
            Log.w(TAG, String.format("translation %s -> %s already exists", sourceText, targetText));
            return new Long(UNIQUE_CONTRAINT_FAILED_ERR_CODE);
        } finally {
            if (examDb != null) {
                examDb.close();
            }
        }
    }

    public long saveAnswer(AnswerStatsEntry statsInput) {
        Log.i(TAG, "saveAnswer: " + statsInput.toString());

        SQLiteDatabase examDb = null;
        try {
            examDb = sqliteHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ExamDbContract.AnswerStats.QUESTION_WORD_ID, statsInput.getQuestionWordId());
            values.put(ExamDbContract.AnswerStats.WRONG_ANSWER_WORD_ID, statsInput.getWrongAnswerWordId());
            values.put(ExamDbContract.AnswerStats.TIME_STAMP, statsInput.getTimestamp().toString());
            return examDb.insert(ExamDbContract.AnswerStats.TABLE_NAME, "null", values);
        } finally {
            if (examDb != null) {
                examDb.close();
            }
        }
    }

    public AnswerStats queryAnswerStats() {

        Log.i(TAG, String.format("queryAnswerStats"));

        SQLiteDatabase examDb = null;
        try {
            examDb = sqliteHelper.getReadableDatabase();
            // return single row, single column the size of the table
            Cursor allAnswers = examDb.rawQuery("SELECT COUNT(*) FROM " +
                    ExamDbContract.AnswerStats.TABLE_NAME, null);
            allAnswers.moveToFirst();
            long answersCount = allAnswers.getLong(0);

            Cursor successAnswers = examDb.rawQuery("SELECT COUNT(*) FROM " +
                    ExamDbContract.AnswerStats.TABLE_NAME + " WHERE " +
                    ExamDbContract.AnswerStats.WRONG_ANSWER_WORD_ID + " = -1", null);
            successAnswers.moveToFirst();
            long successAnswersCount = successAnswers.getLong(0);

            ExamDbFacade.AnswerStats result = new ExamDbFacade.AnswerStats();
            result.setCount(answersCount);
            result.setSuccessRate((float) successAnswersCount / (float) answersCount);

            Log.i(TAG, String.format(
                    "queryAnswerStats result - answersCount: %s, successAnswers: %s, successRate: %s",
                    answersCount, successAnswersCount, result.getSuccessRate()));
            return result;
        } finally {
            if (examDb != null) {
                examDb.close();
            }
        }
    }

    private Cursor queryAllTranslations(Language srcLang, Language targetLang,
                                        SQLiteDatabase examDb) {
        Log.d(TAG, String.format("getAllTranslations - srcLang = %s, targetLang = %s",
                srcLang.toString(), targetLang.toString()));

        Cursor c = null;
        try {
            examDb = sqliteHelper.getReadableDatabase();

            String[] projection = {
                    ExamDbContract.WordsTable.S_TEXT,
                    ExamDbContract.WordsTable.T_TEXT,
                    ExamDbContract.WordsTable._ID
            };

            String whereClause = String.format("%s = '%s' and %s = '%s'",
                    ExamDbContract.WordsTable.S_LANG, srcLang.toString().toLowerCase(),
                    ExamDbContract.WordsTable.T_LANG, targetLang.toString().toLowerCase());

            c = examDb.query(
                    ExamDbContract.WordsTable.TABLE_NAME,  // The table to query
                    projection,             // The columns to return
                    whereClause,            // The columns for the WHERE clause
                    null,                   // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    null                    // The sort order
            );


            if (c == null || c.getCount() == 0) {
                Log.d(TAG, "getAllTranslations - no translations retrieved");
            } else {
                Log.d(TAG, "getAllTranslations - " + c.getCount() + " translations retrieved");
            }
        } finally {
            if (examDb != null) {
                examDb.close();
            }
        }

        return c;
    }

    private TestQuestion getRandomTestQuestion(int wrongOptionsCount, Cursor c) {
        int[] wordIndexes =
                Utils.generateUniqueRandomNumbers(wrongOptionsCount + 1, c.getCount(), rand);

        // which of the randomly generated word indexes will be the translation in question
        int correctAnswer = rand.nextInt(wordIndexes.length);
        Log.d(TAG, String.format("correct answer: %s", correctAnswer));

        int questionIndex = wordIndexes[correctAnswer];
        Log.d(TAG, String.format("questionIndex = %s", questionIndex));

        // 0 - is the source lang column, 1 - is the target lang
        // randomly select whether the question is from source to target lang or vise versa
        int srcLangColumn = rand.nextInt(2);
        int targetLangColumn = 1 - srcLangColumn;

        TestQuestion result = new TestQuestion();
        result.setQuestion(wordEntryFromCursor(questionIndex, srcLangColumn, c));
        result.setCorrectAnswerIndex(correctAnswer);
        // all the translations follow
        WordEntry[] answers = new WordEntry[wordIndexes.length];
        for (int i = 0; i < answers.length; i++) {
            answers[i] = wordEntryFromCursor(wordIndexes[i], targetLangColumn, c);
        }
        result.setOptions(answers);
        Log.i(TAG, "getRandomTestQuestion - " + result.toString());
        return result;
    }

    private WordEntry wordEntryFromCursor(int row, int textColumn, Cursor c) {
        c.moveToPosition(row);
        // column 2 - _id
        return new WordEntry(c.getInt(2), c.getString(textColumn));
    }

}
