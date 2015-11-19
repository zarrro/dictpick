package com.peevs.dictpick;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.peevs.dictpick.settings.SettingsActivity;

/**
 * Created by zarrro on 14.10.2015 г..
 *
 * Logic common for all DictPick activities
 */
public abstract class BaseActivity extends Activity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected Language foreignLang = null;
    protected Language nativeLang = null;

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
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        foreignLang = Language.valueOf(sharedPrefs.getString("key_pref_src_lang", "EN"));
        nativeLang = Language.valueOf(sharedPrefs.getString("key_pref_target_lang", "BG"));
    }

    protected void sayQuestion(String srcText) {
        new TextToSpeechTask(srcText, foreignLang, getFilesDir()).execute();
    }

    protected void sayQuestion(String srcText, Language lang) {
        new TextToSpeechTask(srcText, lang, getFilesDir()).execute();
    }

}
