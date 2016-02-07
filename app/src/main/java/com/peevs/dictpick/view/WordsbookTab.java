package com.peevs.dictpick.view;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.R;
import com.peevs.dictpick.TabFragmentHost;
import com.peevs.dictpick.logic.DeleteTranslationEntryTask;
import com.peevs.dictpick.model.TranslationEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.peevs.dictpick.view.ContextMenuItem.*;

enum ContextMenuItem {
    DETAILS, DELETE
}

/**
 * Created by zarrro on 03.01.16.
 */
public class WordsbookTab extends ListFragment {

    private TabFragmentHost parentActivity;
    private TranslationEntry selectedTranslationEntry;
    private List<TranslationEntry> entries;
    private WordsbookListAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExamDbFacade examDb = new ExamDbFacade(new ExamDbHelper(parentActivity));

        entries = examDb.listTranslationEntries(ExamDbContract.WordsTable.DEFAULT_BOOK_ID);
        adapter = new WordsbookListAdapter(getActivity(), entries);
        setListAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo aInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;

        selectedTranslationEntry = (TranslationEntry) getListAdapter().getItem(aInfo.position);

        menu.setHeaderTitle("Options for " + selectedTranslationEntry.getSrcText().getVal());
        menu.add(1, DETAILS.ordinal(), 1, getContextMenuItemTitle(DETAILS));
        menu.add(1, DELETE.ordinal(), 2, getContextMenuItemTitle(DELETE));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = (TabFragmentHost) activity;
    }

    // This method is called when user selects an Item in the Context menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (selectedTranslationEntry != null) {
            switch (ContextMenuItem.values()[item.getItemId()]) {
                case DETAILS:
                    Toast.makeText(getActivity(), "Details", Toast.LENGTH_SHORT).show();
                    break;
                case DELETE:
                    (new DeleteTranslationEntryTask(parentActivity, selectedTranslationEntry,
                            adapter)).execute();
                    break;
                default:
                    throw new IllegalArgumentException(item.toString());
            }
        }
        return true;
    }

    private String getContextMenuItemTitle(ContextMenuItem item) {
        switch (item) {
            case DETAILS:
                return getString(R.string.wb_ctx_menu_item_details);
            case DELETE:
                return getString(R.string.wb_ctx_menu_item_delete);
            default:
                throw new IllegalArgumentException(item.name());
        }
    }
}
