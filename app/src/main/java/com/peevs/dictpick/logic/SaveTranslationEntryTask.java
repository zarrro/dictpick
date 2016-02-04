package com.peevs.dictpick.logic;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.R;
import com.peevs.dictpick.model.TranslationEntry;

/**
 * Created by zarrro on 1.2.2016 г..
 */
public class SaveTranslationEntryTask extends AsyncTask<Void, Void, Long>
        implements View.OnClickListener {
    private final TranslationEntry te;
    private String errorMessage;
    private Context context;
    private View lastClickedView;

    public SaveTranslationEntryTask(Context context, TranslationEntry te) {
        this.te = te;
        this.context = context;
    }

    @Override
    protected Long doInBackground(Void... params) {
        long wordId = ExamDbFacade.ID_NOT_EXISTS;

        // save to DB
        ExamDbFacade examDbFacade =
                new ExamDbFacade(new ExamDbHelper(context));
        try {
            wordId = examDbFacade.saveTranslation(
                    te.getSrcText().getVal(),
                    te.getTargetText().getVal(),
                    te.getSrcText().getLang().toString().toLowerCase(),
                    te.getTargetText().getLang().toString().toLowerCase());
            te.setId(wordId);
        } catch (ExamDbFacade.AlreadyExistsException e) {
            errorMessage = e.getMessage();
        }
        return wordId;
    }

    @Override
    protected void onPostExecute(Long result) {
        if (result == null || result == -1) {
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                errorMessage = null;
            } else {
                Toast.makeText(context, "Error on saving...", Toast.LENGTH_SHORT).show();
            }
        } else if (result.intValue() == ExamDbFacade.UNIQUE_CONTRAINT_FAILED_ERR_CODE) {
            Toast.makeText(context, "Translation is already saved.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, String.format("Saved translation number %s", result),
                    Toast.LENGTH_SHORT).show();

            ImageView star = null;
            if (lastClickedView != null && (star = (ImageView) lastClickedView.findViewById(
                    R.id.translation_list_item_star)) != null) {
                star.setImageResource(R.drawable.ic_star_enabled);
            }
            lastClickedView.setOnClickListener(null);
        }
    }

    @Override
    public void onClick(View v) {
        lastClickedView = v;
        execute();
    }
}
