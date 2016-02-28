package com.peevs.dictpick.view;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.R;
import com.peevs.dictpick.TabFragmentHost;
import com.peevs.dictpick.logic.DeleteTranslationEntryTask;
import com.peevs.dictpick.model.TranslationEntry;

import java.util.List;

import static com.peevs.dictpick.view.WordsbookTab.ContextMenuItem.*;

/**
 * Created by zarrro on 03.01.16.
 */
public class WordsbookTab extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    enum ContextMenuItem {
        EDIT, DELETE
    }

    private static final int WORD_DETAILS_REQUEST_CODE = 10;

    private TabFragmentHost parentActivity;
    private TranslationEntry selectedTranslation;

    private List<TranslationEntry> entries;
    private WordbookCursorAdapter adapter;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new WordbookCursorAdapter(parentActivity, null, 0);
        setListAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        selectedTranslation = TranslationEntry.fromCursor((Cursor) getListAdapter().getItem(
                ((AdapterView.AdapterContextMenuInfo) menuInfo).position));

        menu.setHeaderTitle("Options for " + selectedTranslation.getSrcText().getVal());
        menu.add(1, EDIT.ordinal(), 1, getContextMenuItemTitle(EDIT));
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
        if (selectedTranslation != null) {
            switch (ContextMenuItem.values()[item.getItemId()]) {
                case EDIT:
                    startTranslationEntryActivity();
                    break;
                case DELETE:
                    (new DeleteTranslationEntryTask(parentActivity,selectedTranslation.getId())).
                            execute();
                    break;
                default:
                    throw new IllegalArgumentException(item.toString());
            }
        }
        return true;
    }

    private String getContextMenuItemTitle(ContextMenuItem item) {
        switch (item) {
            case EDIT:
                return getString(R.string.wb_ctx_menu_item_details);
            case DELETE:
                return getString(R.string.wb_ctx_menu_item_delete);
            default:
                throw new IllegalArgumentException(item.name());
        }
    }

    private void startTranslationEntryActivity() {
        if(selectedTranslation != null) {
            Intent intent = new Intent(parentActivity, TranslationEntryActivity.class);
            intent.putExtra(TranslationEntry.class.getName(),
                    selectedTranslation.toString());
            startActivity(intent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Uri.parse(ExamDbContract.WordsTable.CONTENT_URI),
                null, null, null, null) {

            private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

            @Override
            protected Cursor onLoadInBackground() {
                ExamDbFacade examDbFacade =
                        new ExamDbFacade(new ExamDbHelper(parentActivity));
                Cursor c = examDbFacade.queryTranslationsCursor(
                        ExamDbContract.WordsTable.DEFAULT_BOOK_ID, null, null);
                if (c != null) {
                    // Ensure the cursor window is filled
                    c.getCount();
                    c.registerContentObserver(mObserver);
                }
                c.setNotificationUri(parentActivity.getContentResolver(), getUri());
                return c;
            }
        };
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
