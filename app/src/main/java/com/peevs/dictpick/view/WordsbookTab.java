package com.peevs.dictpick.view;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.TabFragmentHost;
import com.peevs.dictpick.model.TranslationEntry;

/**
 * Created by zarrro on 03.01.16.
 */
public class WordsbookTab extends ListFragment {

    private TabFragmentHost parentActivity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExamDbFacade examDb = new ExamDbFacade(new ExamDbHelper(parentActivity));
        WordsbookListAdapter adapter = new WordsbookListAdapter(getActivity(),
                examDb.listTranslationEntries(ExamDbContract.WordsTable.DEFAULT_BOOK_ID));
        setListAdapter(adapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = (TabFragmentHost) activity;
    }
}
