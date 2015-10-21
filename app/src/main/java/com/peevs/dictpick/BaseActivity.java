package com.peevs.dictpick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.peevs.dictpick.settings.SettingsActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by zarrro on 14.10.2015 г..
 *
 * Logic common for all DictPick activities
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected Language srcLang = null;
    protected Language targetLang = null;
    protected long questionWordId = -1;

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


    public File getSpeechFile(String text, Language lang) {
        StringBuilder sb = new StringBuilder();
        sb.append("tts-");
        sb.append(text.hashCode());
        sb.append(lang.toString().hashCode());
        sb.append(".mp3");
        return new File(this.getFilesDir(), sb.toString());
    }

    protected void sayQuestion(String srcText) {
        new TextToSpeechTask(srcText, srcLang, getSpeechFile(srcText, srcLang)).execute();
    }

    public void sayQuestion(View v) {
        if (questionWordId != -1 && srcLang != null) {
            sayQuestion(questionWordId);
        } else {
            Log.d(TAG, String.format(
                    "sayQuestion - question word is not set: questionWordId = %s, srcLang = %s",
                    questionWordId, srcLang));
        }
    }
}
