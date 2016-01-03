package com.peevs.dictpick;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.peevs.dictpick.settings.Settings;
import com.peevs.dictpick.settings.SettingsActivity;
import com.peevs.dictpick.view.DictPickTabFragmentHost;

/**
 * Created by zarrro on 14.10.2015 г..
 *
 * Logic common for all DictPick activities
 */
public abstract class TabFragmentHost extends Activity implements DictPickTabFragmentHost {

    private static final String TAG = TabFragmentHost.class.getSimpleName();

    private Language foreignLang = null;
    private Language nativeLang = null;
    private boolean autoSayQuestion = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        initFromPreferrences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFromPreferrences();
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

    protected void initFromPreferrences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        foreignLang = Language.valueOf(sharedPrefs.getString("key_pref_src_lang", "EN"));
        nativeLang = Language.valueOf(sharedPrefs.getString("key_pref_target_lang", "BG"));
        autoSayQuestion = sharedPrefs.getBoolean(Settings.PREF_KEY_AUTO_SAY_QUESTION, true);
    }

    @Override
    public Language getForeignLanguage() {
        return foreignLang;
    }

    @Override
    public Language getNativeLanguage() {
        return nativeLang;
    }

    @Override
    public boolean getAutoSayQuestion() {
        return autoSayQuestion;
    }
}
