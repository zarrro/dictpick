package com.peevs.dictpick;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by zarrro on 14.10.2015 г..
 *
 * Logic common for all DictPick activities
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Language srcLang = null;
    protected Language targetLang = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        initLanguages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initLanguages();
    }

    protected void initLanguages() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        srcLang = Language.valueOf(sharedPrefs.getString("key_pref_src_lang", "EN"));
        targetLang = Language.valueOf(sharedPrefs.getString("key_pref_target_lang", "BG"));
    }
}
