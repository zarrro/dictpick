package com.peevs.dictpick;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.peevs.dictpick.model.Question;
import com.peevs.dictpick.model.TestQuestion;
import com.peevs.dictpick.model.Text;
import com.peevs.dictpick.model.TextEntry;
import com.peevs.dictpick.model.TranslationEntry;
import com.peevs.dictpick.model.Wordbook;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by zarrro on 13.9.2015 г..
 */
public class ExamDbFacade {

    public static class AlreadyExistsException extends Exception {

        public AlreadyExistsException() {

        }

        public AlreadyExistsException(String msg) {
            super(msg);
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

    public static final int ID_NOT_EXISTS = -1;
    public static final int UNIQUE_CONTRAINT_FAILED_ERR_CODE = -2067;
    private static final String TAG = ExamDbFacade.class.getSimpleName();

    private final SQLiteOpenHelper sqliteHelper;
    private Random rand = new Random(System.currentTimeMillis());

    public ExamDbFacade(SQLiteOpenHelper sqliteHelper) {
        if (sqliteHelper == null)
            throw new IllegalArgumentException();
        this.sqliteHelper = sqliteHelper;
    }

    public TestQuestion getRandomTestQuestion(Language srcLang, Language targetLang,
                                              int wrongOptionsCount, int book_id) {
        SQLiteDatabase examDb = null;
        Cursor c = null;
        try {
            examDb = sqliteHelper.getReadableDatabase();
            c = initTranslationsCursor(srcLang, targetLang, examDb, book_id, null, null);
            if (c.getCount() <= 0) {
                return null;
            }
            return getRandomTestQuestion(srcLang, targetLang, wrongOptionsCount, c);
        } finally {
            if (c != null) c.close();
            if (examDb != null) examDb.close();
        }
    }

    /**
     * Persists the given translation entry to the DB.
     * If the entry doesn't have id, it is inserted as new row and its id is initialized.
     * If id was set, the row with that id is updated.
     *
     * @param te - TranslationEntry instance to be persisted.
     */
    public void saveTranslation(TranslationEntry te) {
        Log.i(TAG, "saveTranslation - translationEntry: " + te.toString());
        SQLiteDatabase examDb = null;
        try {
            examDb = sqliteHelper.getWritableDatabase();
            saveTranslation(examDb, te);
        } catch (SQLiteConstraintException e) {
            throw new IllegalStateException("translation %s -> %s already exists");
        } finally {
            if (examDb != null) {
                examDb.close();
            }
        }
    }

    private void saveTranslation(SQLiteDatabase writableExamDb, TranslationEntry te) {
        ContentValues values = new ContentValues();
        values.put(ExamDbContract.WordsTable.S_TEXT, te.getSrcText().getVal());
        values.put(ExamDbContract.WordsTable.T_TEXT, te.getTargetText().getVal());
        values.put(ExamDbContract.WordsTable.S_LANG, te.getSrcText().getLang().toString());
        values.put(ExamDbContract.WordsTable.T_LANG, te.getTargetText().getLang().toString());
        values.put(ExamDbContract.WordsTable.RATING, te.getRating());
        values.put(ExamDbContract.WordsTable.BOOKID, te.getBookId());
        if (te.getId() <= 0) {
            te.setId(writableExamDb.insertOrThrow(ExamDbContract.WordsTable.TABLE_NAME,
                    "null", values));
            Log.i(TAG, "added new translation " + te.getId());
        } else {
            writableExamDb.update(ExamDbContract.WordsTable.TABLE_NAME, values,
                    ExamDbContract.WordsTable._ID + " = " + te.getId(), null);
            Log.i(TAG, "updated existing translation " + te.getId());
        }
    }

    public void deleteTranslation(long id) {
        Log.i(TAG, "deleteTranslation: " + id);

        SQLiteDatabase examDb = null;
        try {
            examDb = sqliteHelper.getWritableDatabase();
            examDb.delete(ExamDbContract.WordsTable.TABLE_NAME,
                    ExamDbContract.WordsTable._ID + " = " + id, null);
        } finally {
            if (examDb != null) {
                examDb.close();
            }
        }
    }

    /**
     * Updates Exam DB stats for the question. To be invoked after each question was answered.
     * NOTE: The method doesn't detect whether checkAnswer was actually invoked or not for the
     * question.
     */
    public long saveAnswer(Question q) {
        Log.i(TAG, "saveAnswer invoked");

        Integer wrongWordId = null;
        String wrongText = null;

        Object wrongAnswer = q.getLastWrongAnswer();
        if (wrongAnswer != null) {
            if (wrongAnswer instanceof Integer) {
                wrongWordId = (Integer) wrongAnswer;
            } else {
                wrongText = wrongAnswer.toString();
            }
        }

        SQLiteDatabase examDb = null;
        try {
            examDb = sqliteHelper.getWritableDatabase();


            // With each answer, all other words ratings is decreased a little, so they will have
            // precedence in the rating selection compared to the most recently answered. This is
            // need to avoid the case where for example after give answer the rating is 250,
            // and no matter that there is other older words with the same rating again the
            // same question is selected by the rating base algorithm.
            examDb.execSQL("UPDATE " + ExamDbContract.WordsTable.TABLE_NAME + " SET " +
                    getSqlUpdateDecreaseInteger(ExamDbContract.WordsTable.RATING, 1) +
                    " WHERE " + ExamDbContract.WordsTable.RATING + " > " + Question.MIN_RATING);

            // Update the rating of the translation entry of the question.
            saveTranslation(examDb, q.getQuestion());

            // Add entry in the answer stats table
            ContentValues values = new ContentValues();
            values.put(ExamDbContract.AnswerStats.QUESTION_WORD_ID, q.getQuestion().getId());
            values.put(ExamDbContract.AnswerStats.QUESTION_TYPE, q.getType().toString());
            values.put(ExamDbContract.AnswerStats.WRONG_ANSWER_WORD_ID, wrongWordId);
            values.put(ExamDbContract.AnswerStats.WRONG_ANSWER_TEXT, wrongText);
            values.put(ExamDbContract.AnswerStats.TIME_STAMP, new Date().toString());
            return examDb.insert(ExamDbContract.AnswerStats.TABLE_NAME, "null", values);
        } finally {
            if (examDb != null) {
                examDb.close();
            }
        }
    }

    String getSqlUpdateDecreaseInteger(String columnName, int decrease) {
        return String.format("%1$s = %1$s - %2$s", columnName, decrease);
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

    /**
     * Filter out the translations which don't exist in the DB, and return list with WordEntries
     * for the existing ones.
     *
     * @param srcText      - source text
     * @param translations - list of the translations of the source text
     * @param srcLang      - the language of the srcText
     * @param targetLang   - the language of the targetLang
     * @return - List of TranslationEntryFragment elements, for the translation which exists in the DB.
     */
    public Map<String, Integer> filterExisting(String srcText, List<String> translations,
                                               Language srcLang, Language targetLang) {

        if (srcLang == null) {
            throw new IllegalArgumentException("srcLang is null");
        }
        if (targetLang == null) {
            throw new IllegalArgumentException("targetLang is null");
        }
        if (srcText == null) {
            throw new IllegalArgumentException("srcText is null");
        }
        if (translations == null || translations.isEmpty()) {
            throw new IllegalArgumentException("translations is null or empty");
        }


        StringBuilder translationsSetBuilder = new StringBuilder(" (");
        int size = translations.size();
        int counter = 0;
        for (String s : translations) {
            ++counter;
            translationsSetBuilder.append(" '").append(s).append("'");
            if (counter < size) {
                translationsSetBuilder.append(",");
            }
        }
        translationsSetBuilder.append(" )");
        String translationSet = translationsSetBuilder.toString();

        Log.d(TAG, String.format(
                "invoked filterExisting:  srcText = %s, translations = %s, srcLang = %s, targetLang = %s",
                srcText, translationSet, srcLang, targetLang));

        SQLiteDatabase examDb = null;
        Cursor c = null;

        final String AND = " and ";
        final String EQUALS = " = ";

        try {
            examDb = sqliteHelper.getReadableDatabase();

            String[] projection = {
                    ExamDbContract.WordsTable._ID,
                    ExamDbContract.WordsTable.T_TEXT
            };

            StringBuilder whereClause = new StringBuilder();
            whereClause.
                    append(ExamDbContract.WordsTable.S_LANG).append(EQUALS).
                    append("'" + srcLang.toString().toLowerCase() + "'").append(AND).

                    append(ExamDbContract.WordsTable.T_LANG).append(EQUALS).
                    append("'" + targetLang.toString().toLowerCase() + "'").append(AND).

                    append(ExamDbContract.WordsTable.S_TEXT).append(EQUALS).
                    append("'" + srcText + "'").append(AND).append(ExamDbContract.WordsTable.T_TEXT).
                    append(" in ").append(translationSet);

            c = examDb.query(
                    ExamDbContract.WordsTable.TABLE_NAME,  // The table to query
                    projection,             // The columns to return
                    whereClause.toString(), // The columns for the WHERE clause
                    null,                   // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    null                    // The sort order
            );

            if (c.getCount() > translations.size()) {
                throw new IllegalStateException("Filtered set bigger than the source set");
            }

            Map<String, Integer> result = new HashMap<>();
            final int ID_COLUMN_INDEX = 0;
            final int TARGET_TEXT_COLUMN_INDEX = 1;
            while (c.moveToNext()) {
                result.put(c.getString(TARGET_TEXT_COLUMN_INDEX), c.getInt(ID_COLUMN_INDEX));
            }
            return result;
        } finally {
            if (c != null) {
                c.close();
            }
            if (examDb != null) {
                examDb.close();
            }
        }
    }

    private Cursor initTranslationsCursor(Language srcLang, Language targetLang,
                                          SQLiteDatabase examDb, int book_id, String orderBy,
                                          Integer limit) {
        Log.d(TAG, String.format(
                "initTranslationsCursor srcLang = %s, targetLang = %s, book_id = %s," +
                        " orderBy = %s, limit = %s", srcLang, targetLang, book_id, orderBy, limit));

        Cursor c = null;
        try {
            examDb = sqliteHelper.getReadableDatabase();



            StringBuilder whereClauseBuilder = new StringBuilder();
            boolean and = false;
            if (srcLang != null) {
                whereClauseBuilder.append(ExamDbContract.WordsTable.S_LANG);
                whereClauseBuilder.append(" = ");
                whereClauseBuilder.append("'" + srcLang.toString() + "'");
                and = true;
            }
            if (and) {
                whereClauseBuilder.append(" and ");
            }
            if (srcLang != null) {
                whereClauseBuilder.append(ExamDbContract.WordsTable.T_LANG);
                whereClauseBuilder.append(" = ");
                whereClauseBuilder.append("'" + targetLang.toString() + "'");
                and = true;
            }
            if (and) {
                whereClauseBuilder.append(" and ");
            }

            whereClauseBuilder.append(ExamDbContract.WordsTable.BOOKID);
            whereClauseBuilder.append(" = ");
            whereClauseBuilder.append(book_id);

            c = examDb.query(
                    ExamDbContract.WordsTable.TABLE_NAME,  // The table to query
                    TranslationEntry.dbProjection(),       // The columns to return
                    whereClauseBuilder.toString(), // The columns for the WHERE clause
                    null,                   // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    orderBy,                // order by column asc, desc
                    limit != null ? String.valueOf(limit) : null  // limit 10
            );

            if (c == null || c.getCount() == 0) {
                Log.d(TAG, "initTranslationsCursor - no translations retrieved");
            } else {
                Log.d(TAG, "initTranslationsCursor - " + c.getCount() + " translations retrieved");
            }
        } finally {
            if (examDb != null) {
                examDb.close();
            }
        }

        return c;
    }

    private TestQuestion getRandomTestQuestion(Language srcLang, Language targetLang,
                                               int wrongOptionsCount, Cursor c) {
        int[] wordIndexes =
                Utils.generateUniqueRandomNumbers(wrongOptionsCount + 1, c.getCount(), rand);

        // which of the randomly generated word indexes will be the translation in question
        int correctAnswer = rand.nextInt(wordIndexes.length);
        Log.d(TAG, String.format("correct answer: %s", correctAnswer));

        int questionIndex = wordIndexes[correctAnswer];
        Log.d(TAG, String.format("questionIndex = %s", questionIndex));

        // 0 - is the source lang column, 1 - is the target lang
        // randomly select whether the question is from source to target lang or vise versa

        boolean inverse = rand.nextInt(2) == 1;
        int answersColumn = inverse ? 0 : 1;

        c.moveToPosition(questionIndex);
        TestQuestion testQuestion = new TestQuestion(TranslationEntry.fromCursor(c));
        testQuestion.setInverse(inverse);

        Language optionLanguge = !inverse ? targetLang : srcLang;

        testQuestion.setCorrectOptionIndex(correctAnswer);
        // all the translations follow
        TextEntry[] answers = new TextEntry[wordIndexes.length];
        for (int i = 0; i < answers.length; i++) {
            answers[i] = createOptionTextEntry(c, wordIndexes[i], answersColumn,
                    optionLanguge);
        }
        testQuestion.setOptions(answers);
        Log.i(TAG, "getRandomTestQuestion - " + testQuestion.toString());
        return testQuestion;
    }

    private TextEntry createOptionTextEntry(Cursor c, int row, int textColumn, Language lang) {
        c.moveToPosition(row);
        // column 2 - _id
        return new TextEntry(new Text(c.getString(textColumn), lang), c.getInt(2));
    }

    public Wordbook[] listAllWordbooks() {
        Log.d(TAG, String.format("listAllWordbooks invoked"));

        Cursor c = initAllWordbooksCursor();
        int i = 0;
        Wordbook[] ret = new Wordbook[c.getCount()];
        if (c.moveToFirst())
            do {
                ret[i] = new Wordbook(c.getInt(0), c.getString(1));
                i++;
            } while (c.moveToNext());
        return ret;
    }

    public List<TranslationEntry> listTranslationsWithLowestRating(Language sl, Language tl,
                                                                   int book_id) {
        Log.d(TAG, "listTranslationsWithLowestRating invoked");

        SQLiteDatabase examDb = null;
        Cursor c = null;
        List<TranslationEntry> result = null;
        try {
            examDb = sqliteHelper.getReadableDatabase();

            c = examDb.rawQuery(String.format(
                    "select %1$s from %2$s order by %1$s limit 1",
                    ExamDbContract.WordsTable.RATING,
                    ExamDbContract.WordsTable.TABLE_NAME), null);

            if(c.getCount() == 0) {
                // empty db
                return null;
            }

            c.moveToFirst();
            int minRating = c.getInt(0);

            c = examDb.query(
                    ExamDbContract.WordsTable.TABLE_NAME,  // The table to query
                    TranslationEntry.dbProjection(),       // The columns to return
                    // The columns for the WHERE clause
                    ExamDbContract.WordsTable.RATING + " = " + minRating,
                    null,                   // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    null                // order by column asc, desc
            );

            Log.d(TAG, String.format("%s translation with the minimal rating %s queried",
                    c.getCount(), minRating));

            result = new ArrayList<>(c.getCount());
            c.moveToFirst();
            do {
                result.add(TranslationEntry.fromCursor(c));
            } while (c.moveToNext());
        } finally {
            if (c != null) c.close();
            if (examDb != null) examDb.close();
        }
        return result;
    }

    public List<TranslationEntry> queryTranslations(Language sl, Language tl, int book_id,
                                                    String orderBy, Integer limit) {
        Log.d(TAG, "queryTranslations invoked");

        SQLiteDatabase examDb = null;
        Cursor c = null;
        List<TranslationEntry> result = null;
        try {
            examDb = sqliteHelper.getReadableDatabase();
            c = initTranslationsCursor(sl, tl, examDb, book_id, orderBy, limit);
            if (c.getCount() <= 0) {
                return null;
            }
            result = new ArrayList<>(c.getCount());
            c.moveToFirst();
            do {
                result.add(TranslationEntry.fromCursor(c));
            } while (c.moveToNext());
        } finally {
            if (c != null) c.close();
            if (examDb != null) examDb.close();
        }
        return result;
    }

    public Cursor queryTranslationsCursor(int book_id, String orderBy, Integer limit) {
        return initTranslationsCursor(null, null, sqliteHelper.getReadableDatabase(), book_id,
                orderBy, limit);
    }

    private Cursor initAllWordbooksCursor() {
        SQLiteDatabase examDb = null;
        Cursor c = null;
        examDb = sqliteHelper.getReadableDatabase();
        try {
            examDb = sqliteHelper.getReadableDatabase();

            String[] projection = {
                    ExamDbContract.WordbookTable._ID,
                    ExamDbContract.WordbookTable.NAME
            };

            c = examDb.query(
                    ExamDbContract.WordbookTable.TABLE_NAME,  // The table to query
                    projection,             // The columns to return
                    null,                   // The columns for the WHERE clause
                    null,                   // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    null                    // The sort order
            );

            if (c == null || c.getCount() == 0) {
                Log.d(TAG, "initAllWordbooksCursor - no wordbooks found");
            } else {
                Log.d(TAG, "initAllWordbooksCursor - " + c.getCount() + " wordbooks retrieved");
            }
        } finally {
            if (examDb != null) {
                examDb.close();
            }
        }

        return c;
    }
}