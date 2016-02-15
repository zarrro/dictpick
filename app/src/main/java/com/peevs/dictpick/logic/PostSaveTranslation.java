package com.peevs.dictpick.logic;

import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.ExamDbFacade;

/**
 * Created by zarrro on 13.2.2016 г..
 */
public class PostSaveTranslation implements TaskPostExecuteAction<Long> {
    protected SaveTranslationEntryTask saveTask;

    @Override
    public void onPostExecute(AsyncTask<?, ?, Long> task, Long result) {
        if (!(task instanceof SaveTranslationEntryTask)) {
            throw new IllegalArgumentException("to be used only for SaveTranslationEntryTask");
        }
        saveTask = (SaveTranslationEntryTask) task;

        if (result == null || result == -1) {
            if (saveTask.getErrorMessage() != null) {
                Toast.makeText(saveTask.getContext(), saveTask.getErrorMessage(),
                        Toast.LENGTH_SHORT).show();
                saveTask.setErrorMessage(null);
            } else {
                Toast.makeText(saveTask.getContext(), "Error on saving...",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (result.intValue() == ExamDbFacade.UNIQUE_CONTRAINT_FAILED_ERR_CODE) {
            Toast.makeText(saveTask.getContext(), "Translation is already saved.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(saveTask.getContext(), String.format("Saved translation number %s",
                            result),
                    Toast.LENGTH_SHORT).show();

            ImageView star = null;
        }
    }
}
