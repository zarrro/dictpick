package com.peevs.dictpick.logic;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.model.TranslationEntry;

/**
 * Created by zarrro on 1.2.2016 г..
 */
public class SaveTranslationEntryTask extends AsyncTask<Void, Void, Long>
        implements View.OnClickListener {
    private final TranslationEntry te;
    private final TaskPostExecuteAction<Long> onPostExecuteAction;

    private String errorMessage;
    protected Context context;
    private View lastClickedView;

    public SaveTranslationEntryTask(Context context, TranslationEntry te,
                                    TaskPostExecuteAction<Long> onPostExecuteAction) {
        this.te = te;
        this.context = context;
        this.onPostExecuteAction = onPostExecuteAction;
    }

    @Override
    protected Long doInBackground(Void... params) {
        ExamDbFacade examDbFacade =
                new ExamDbFacade(new ExamDbHelper(context));
        // insert new or save existing to DB

        examDbFacade.saveTranslation(te, ExamDbContract.WordsTable.DEFAULT_BOOK_ID);

        context.getContentResolver().notifyChange(
                Uri.parse(ExamDbContract.WordsTable.CONTENT_URI), null);

        return te.getId();
    }

    @Override
    protected void onPostExecute(Long result) {
       if(onPostExecuteAction != null) {
           onPostExecuteAction.onPostExecute(this, result);
       }
    }

    @Override
    public void onClick(View v) {
        lastClickedView = v;
        execute();
    }

    public TaskPostExecuteAction<Long> getOnPostExecuteAction() {
        return onPostExecuteAction;
    }

    public View getLastClickedView() {
        return lastClickedView;
    }

    public void setLastClickedView(View lastClickedView) {
        this.lastClickedView = lastClickedView;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public TranslationEntry getTe() {
        return te;
    }
}
