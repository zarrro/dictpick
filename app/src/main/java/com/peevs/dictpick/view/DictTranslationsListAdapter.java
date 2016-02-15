package com.peevs.dictpick.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.R;
import com.peevs.dictpick.logic.DictOnSaveTranslation;
import com.peevs.dictpick.logic.SaveTranslationEntryTask;
import com.peevs.dictpick.model.TranslationEntry;

/**
 * Created by zarrro on 1.2.2016 г..
 */
public class DictTranslationsListAdapter extends ArrayAdapter<TranslationEntry> {
    private final Context context;
    private final TranslationEntry[] values;

    public DictTranslationsListAdapter(Context context, TranslationEntry[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View li = inflater.inflate(R.layout.dict_translation_listitem_layout, parent, false);

        TextView translationText = (TextView) li.findViewById(R.id.translation_list_item_text);
        ImageView star = (ImageView) li.findViewById(R.id.translation_list_item_star);

        TranslationEntry te = values[position];
        translationText.setText(te.getTargetText().getVal().toString());

        if (te.getId() != ExamDbFacade.ID_NOT_EXISTS) {
            star.setImageResource(R.drawable.ic_star_enabled);
        } else {
            star.setImageResource(R.drawable.ic_star_disabled);
            li.setOnClickListener(new SaveTranslationEntryTask(context, te,
                    new DictOnSaveTranslation()));
        }
        return li;
    }
}
