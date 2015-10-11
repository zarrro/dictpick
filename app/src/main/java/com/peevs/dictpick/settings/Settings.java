package com.peevs.dictpick.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.peevs.dictpick.ChallengeManager;
import com.peevs.dictpick.R;

/**
 * Created by zarrro on 1.10.2015 г..
 */
public class Settings extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = Settings.class.getSimpleName();
    public static final String PREF_KEY_RECURRING_CHALLENGE_FREQUENCY =
            "key_pref_challenge_notifications";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(PREF_KEY_RECURRING_CHALLENGE_FREQUENCY)) {
            ChallengeManager cm = new ChallengeManager(getActivity());
            cm.setRecurringChallenge(
                    sharedPreferences.getString(PREF_KEY_RECURRING_CHALLENGE_FREQUENCY, "NONE"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
