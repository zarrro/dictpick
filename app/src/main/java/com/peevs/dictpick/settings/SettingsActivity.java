package com.peevs.dictpick.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by zarrro on 1.10.2015 г..
 */
public class SettingsActivity extends Activity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Settings())
                .commit();
    }
}
