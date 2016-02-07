package com.peevs.dictpick;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.peevs.dictpick.model.TestQuestion;
import com.peevs.dictpick.model.Text;
import com.peevs.dictpick.model.TextEntry;
import com.peevs.dictpick.model.TranslationEntry;
import com.peevs.dictpick.model.Wordsbook;

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

    public static final int ID_NOT_EXISTS = -1;
    public static final int UNIQUE_CONTRAINT_FAILED_ERR_CODE = -2067;

    public static class AlreadyExistsException extends Exception {

        public AlreadyExistsException() {

        }

        public AlreadyExistsException(String msg) {
            super(msg);
        }
    }

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

    public TestQuestion getRandomTestQuestion(Language srcLang, Language targetLang,
                                              int wrongOptionsCount, int book_id) {
        SQLiteDatabase examDb = null;
        Cursor c = null;
        try {
            examDb = sqliteHelper.getReadableDatabase();
            c = queryAllTranslations(srcLang, targetLang, examDb, book_id);
            if (c.getCount() <= 0) {
                return null;
            }
            return getRandomTestQuestion(srcLang, targetLang, wrongOptionsCount, c);
        } finally {
            if (c != null) c.close();
            if (examDb != null) examDb.close();
        }
    }

    public long saveTranslation(String sourceText, String targetText, String sourceLang,
                                String targetLang) throws AlreadyExistsException {

        Log.i(TAG, String.format("saveTranslation - sourceText %s, targetText %s, sourceLang %s," +
                "nativeLang %s", sourceText, targetText, sourceLang, targetLang));

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
            throw new AlreadyExistsException("translation %s -> %s already exists");
        } finally {
            if (examDb != null) {
                examDb.close();
            }
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

    /**
     * Filter out the translations which don't exist in the DB, and return list with WordEntries
     * for the existing ones.
     *
     * @param srcText      - source text
     * @param translations - list of the translations of the source text
     * @param srcLang      - the language of the srcText
     * @param targetLang   - the language of the targetLang
     * @return - List of TranslationEntry elements, for the translation which exists in the DB.
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

    private Cursor queryAllTranslations(Language srcLang, Language targetLang,
                                        SQLiteDatabase examDb, int book_id) {
        Cursor c = null;
        try {
            examDb = sqliteHelper.getReadableDatabase();

            String[] projection = {
                    ExamDbContract.WordsTable.S_TEXT,
                    ExamDbContract.WordsTable.T_TEXT,
                    ExamDbContract.WordsTable._ID,
                    ExamDbContract.WordsTable.S_LANG,
                    ExamDbContract.WordsTable.T_LANG
            };

            StringBuilder whereClauseBuilder = new StringBuilder();
            boolean and = false;
            if (srcLang != null) {
                whereClauseBuilder.append(ExamDbContract.WordsTable.S_LANG);
                whereClauseBuilder.append(" = ");
                whereClauseBuilder.append("'" + srcLang.toString().toLowerCase() + "'");
                and = true;
            }
            if (and) {
                whereClauseBuilder.append(" and ");
            }
            if (srcLang != null) {
                whereClauseBuilder.append(ExamDbContract.WordsTable.T_LANG);
                whereClauseBuilder.append(" = ");
                whereClauseBuilder.append("'" + targetLang.toString().toLowerCase() + "'");
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
                    projection,             // The columns to return
                    whereClauseBuilder.toString(), // The columns for the WHERE clause
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
        TestQuestion testQuestion = new TestQuestion(teFromCursor(c));
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

    private TranslationEntry teFromCursor(Cursor c) {
        final int stCol = 0;
        final int ttCol = 1;
        final int idCol = 2;
        final int slCol = 3;
        final int tlCol = 4;

        Text st = new Text(c.getString(stCol), Language.val(c.getString(slCol)));
        Text tt = new Text(c.getString(ttCol), Language.val(c.getString(tlCol)));
        return new TranslationEntry(c.getLong(idCol), st, tt);
    }

    private TextEntry createOptionTextEntry(Cursor c, int row, int textColumn, Language lang) {
        c.moveToPosition(row);
        // column 2 - _id
        return new TextEntry(new Text(c.getString(textColumn), lang), c.getInt(2));
    }

    public Wordsbook[] listAllWordsbooks() {
        Log.d(TAG, String.format("listAllWordsbooks invoked"));

        Cursor c = listAllWordsbooksCursor();
        int i = 0;
        Wordsbook[] ret = new Wordsbook[c.getCount()];
        if (c.moveToFirst())
            do {
                ret[i] = new Wordsbook(c.getInt(0), c.getString(1));
                i++;
            } while (c.moveToNext());
        return ret;
    }

    public List<TranslationEntry> listTranslationEntries(int book_id) {
        Log.d(TAG, String.format("listTranslationEntries invoked"));

        SQLiteDatabase examDb = null;
        Cursor c = null;
        List<TranslationEntry> result = null;
        try {
            examDb = sqliteHelper.getReadableDatabase();
            c = queryAllTranslations(null, null, examDb, book_id);
            if (c.getCount() <= 0) {
                return null;
            }
            result = new ArrayList<>(c.getCount());
            int i = 0;
            while (c.moveToNext()) {
                result.add(teFromCursor(c));
                ++i;
            }
        } finally {
            if (c != null) c.close();
            if (examDb != null) examDb.close();
        }

        return result;
    }

    private Cursor listAllWordsbooksCursor() {
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
                Log.d(TAG, "listAllWordsbooksCursor - no wordbooks found");
            } else {
                Log.d(TAG, "listAllWordsbooksCursor - " + c.getCount() + " wordbooks retrieved");
            }
        } finally {
            if (examDb != null) {
                examDb.close();
            }
        }

        return c;
    }
}