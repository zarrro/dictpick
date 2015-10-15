package com.peevs.dictpick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.peevs.dictpick.settings.SettingsActivity;

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent startSettings = new Intent(this, SettingsActivity.class);
                startActivity(startSettings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void initLanguages() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        srcLang = Language.valueOf(sharedPrefs.getString("key_pref_src_lang", "EN"));
        targetLang = Language.valueOf(sharedPrefs.getString("key_pref_target_lang", "BG"));
    }
}
