package com.peevs.dictpick.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.peevs.dictpick.R;
import com.peevs.dictpick.model.TranslationEntry;

import java.util.List;

/**
 * Created by zarrro on 17.01.16.
 */
public class WordsbookListAdapter extends ArrayAdapter<TranslationEntry> {

    private final Context context;
    private final List<TranslationEntry> values;

    public WordsbookListAdapter(Context context, List<TranslationEntry> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View li = inflater.inflate(R.layout.wordsbook_listitem_layout, parent, false);

        TextView foreignLang = (TextView) li.findViewById(R.id.foreign_lang_li);
        TextView foreignText = (TextView) li.findViewById(R.id.foreign_text_li);
        TextView nativeLang = (TextView) li.findViewById(R.id.native_lang_li);
        TextView nativeText = (TextView) li.findViewById(R.id.native_text_li);

        TranslationEntry te = values.get(position);
        foreignLang.setText(te.getSrcText().getLang().toString());
        foreignText.setText(te.getSrcText().getVal());
        nativeLang.setText(te.getTargetText().getLang().toString());
        nativeText.setText(te.getTargetText().getVal());
        return li;
    }
}
