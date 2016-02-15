package com.peevs.dictpick.view;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.R;
import com.peevs.dictpick.model.TranslationEntry;

/**
 * Created by zarrro on 14.2.2016 г..
 */
public class WordbookCursorAdapter extends CursorAdapter {

    public WordbookCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.wordsbook_listitem_layout, parent,
                false);
    }

    @Override
    public void bindView(View v, Context context, Cursor cursor) {
        TextView foreignLang = (TextView) v.findViewById(R.id.foreign_lang_li);
        TextView foreignText = (TextView) v.findViewById(R.id.foreign_text_li);
        TextView nativeLang = (TextView) v.findViewById(R.id.native_lang_li);
        TextView nativeText = (TextView) v.findViewById(R.id.native_text_li);

        TranslationEntry te = TranslationEntry.fromCursor(cursor);

        foreignLang.setText(te.getSrcText().getLang().toString());
        foreignText.setText(te.getSrcText().getVal());
        nativeLang.setText(te.getTargetText().getLang().toString());
        nativeText.setText(te.getTargetText().getVal());
    }
}
