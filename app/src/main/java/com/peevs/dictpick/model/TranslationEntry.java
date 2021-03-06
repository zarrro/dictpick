package com.peevs.dictpick.model;

import android.database.Cursor;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.Language;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zarrro on 28.11.15.
 */
public class TranslationEntry {

    private final AtomicLong atomicId;
    private final AtomicInteger rating;
    private final Text srcText;
    private final Text targetText;

    // NOTE: currently bookId is private constant. To be made modifiable when functionality
    // for multiple books support is needed.
    private final int bookId = ExamDbContract.WordsTable.DEFAULT_BOOK_ID;

    public TranslationEntry(long id, Text srcText, Text targetText) {
        this(id, srcText, targetText, 0);
    }

    public TranslationEntry(long id, Text srcText, Text targetText, int rating) {
        this.atomicId = new AtomicLong(id);
        this.rating = new AtomicInteger(rating);
        this.srcText = srcText;
        this.targetText = targetText;
    }

    /**
     * @return - id of the translation so it can be tracked in statistics.
     */
    public long getId() {
        return atomicId.get();
    }

    public void setId(long id) {
        atomicId.set(id);
    }

    public Text getSrcText() {
        return srcText;
    }

    public Text getTargetText() {
        return targetText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TranslationEntry that = (TranslationEntry) o;

        return !(atomicId != null ? !atomicId.equals(that.atomicId) : that.atomicId != null);
    }

    @Override
    public int hashCode() {
        return atomicId != null ? atomicId.hashCode() : 0;
    }

    public static TranslationEntry fromString(String s) {
        String[] parts = s.split(Text.SEP);
        return new TranslationEntry(
                Long.valueOf(parts[0]),
                Text.strToText(parts[1], parts[2]),
                Text.strToText(parts[3], parts[4]), Integer.valueOf(parts[5]));
    }

    public String toString() {
        return getId() + Text.SEP + srcText.toString() + Text.SEP +
                targetText.toString() + Text.SEP + rating;
    }

    public static TranslationEntry fromCursor(Cursor cursor) {
        String srcLang = cursor.getString(cursor.getColumnIndexOrThrow(
                ExamDbContract.WordsTable.S_LANG));
        String srcText = cursor.getString(cursor.getColumnIndexOrThrow(
                ExamDbContract.WordsTable.S_TEXT));
        String targetLang = cursor.getString(cursor.getColumnIndexOrThrow(
                ExamDbContract.WordsTable.T_LANG));
        String targetText = cursor.getString(cursor.getColumnIndexOrThrow(
                ExamDbContract.WordsTable.T_TEXT));
        int rating = cursor.getInt(cursor.getColumnIndexOrThrow(
                ExamDbContract.WordsTable.RATING));
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(
                ExamDbContract.WordsTable._ID));

        Text st = new Text(srcText, Language.val(srcLang));
        Text tt = new Text(targetText, Language.val(targetLang));
        return new TranslationEntry(id, st, tt, rating);
    }

    public static String[] dbProjection() {
        return new String[] {
                ExamDbContract.WordsTable.S_TEXT,
                ExamDbContract.WordsTable.T_TEXT,
                ExamDbContract.WordsTable._ID,
                ExamDbContract.WordsTable.S_LANG,
                ExamDbContract.WordsTable.T_LANG,
                ExamDbContract.WordsTable.RATING
        };
    }

    public int getRating() {
        return rating.get();
    }

    public int getBookId() {
        return bookId;
    }

    public void setRating(int rating) {
        this.rating.set(rating);
    }
}
