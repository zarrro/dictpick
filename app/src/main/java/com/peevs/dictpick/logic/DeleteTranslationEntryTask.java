package com.peevs.dictpick.logic;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.model.TranslationEntry;

import java.util.Iterator;
import java.util.List;

/**
 * Created by zarrro on 1.2.2016 г..
 */
public class DeleteTranslationEntryTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private ArrayAdapter addapterToBeChanged;
    private TranslationEntry te;

    public DeleteTranslationEntryTask(Context context, TranslationEntry te,
                                      ArrayAdapter addapterToBeChanged) {
        this.context = context;
        this.te = te;
        this.addapterToBeChanged = addapterToBeChanged;
    }

    @Override
    protected Void doInBackground(Void... params) {
        ExamDbFacade examDbFacade =
                new ExamDbFacade(new ExamDbHelper(context));
        examDbFacade.deleteTranslation(te.getId());
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
       if(addapterToBeChanged != null) {
           addapterToBeChanged.remove(te);
           addapterToBeChanged.notifyDataSetChanged();
       }
       Toast.makeText(context, "Deleted translation " + te.getId(), Toast.LENGTH_SHORT).show();
    }
}
