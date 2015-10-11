package com.peevs.dictpick.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by zarrro on 1.10.2015 г..
 */
public class SettingsActivity extends AppCompatActivity {

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
