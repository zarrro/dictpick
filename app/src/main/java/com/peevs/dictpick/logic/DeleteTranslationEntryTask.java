package com.peevs.dictpick.logic;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.model.TranslationEntry;

/**
 * Created by zarrro on 1.2.2016 г..
 */
public class DeleteTranslationEntryTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private long teId;

    public DeleteTranslationEntryTask(Context context, long teId) {
        this.context = context;
        this.teId = teId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        ExamDbFacade examDbFacade =
                new ExamDbFacade(new ExamDbHelper(context));
        examDbFacade.deleteTranslation(teId);
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        context.getContentResolver().notifyChange(
                Uri.parse(ExamDbContract.WordsTable.CONTENT_URI), null);
       Toast.makeText(context, "Deleted translation " + teId, Toast.LENGTH_SHORT).show();
    }
}
