package com.peevs.dictpick.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.peevs.dictpick.R;
import com.peevs.dictpick.model.TranslationEntry;

/**
 * Created by zarrro on 12.2.2016 г..
 */
public class TranslationEntryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translation_entry_activity);

        Intent intent = getIntent();
        if(intent == null) {
            throw new IllegalStateException("intent is null");
        }
        TranslationEntryFragment teFragment = new TranslationEntryFragment();
        String translationEntryString = intent.getStringExtra(
                TranslationEntry.class.getName());
        if(translationEntryString != null && !translationEntryString.isEmpty()) {
            teFragment.setTeInstance(TranslationEntry.fromString(translationEntryString));
        }
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.te_activity, teFragment, "translationEntryFragment");
        ft.commit();
    }
}
