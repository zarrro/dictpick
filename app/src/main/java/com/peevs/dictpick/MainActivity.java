package com.peevs.dictpick;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.view.View.OnClickListener;
import static com.peevs.dictpick.Constants.S_LANG;
import static com.peevs.dictpick.Constants.T_LANG;
import static com.peevs.dictpick.ExamDbContract.UNIQUE_CONTRAINT_FAILED_ERR_CODE;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_5:
                scheduleNotification(8000);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void translate(View v) {
        EditText editTextSrc = (EditText) findViewById(R.id.edit_srcText);
        String val = editTextSrc.getText().toString();
        if (val != null && !(val = val.trim()).isEmpty()) {
            new TranslateTask().execute(val);
        }
    }

    public void startExamActivity(View v) {
        Intent intent = new Intent(this, ExamActivity.class);
        startActivity(intent);
    }

    public void clearTranslations(View v) {
        getTranslationsLayout().removeAllViews();
    }

    LinearLayout getTranslationsLayout() {
        return (LinearLayout) this.findViewById(R.id.layout_translation);
    }

    private void scheduleNotification(int delay) {
        // intent to invoke the NotificationPublisher
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // AlarmManager will fire intent for the NotificationPublisher after specified time elapses
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delay, delay, pendingIntent);
    }


    EditText getSrcTextBox() {
        return (EditText) this.findViewById(R.id.edit_srcText);
    }

    class TranslateTask extends AsyncTask<String, Void, List<String>> {

        private static final String TAG = "GenerateTestTask";
        private String srcText = null;

        @Override
        protected List<String> doInBackground(String... params) {
            if (params == null || params.length != 1) {
                Log.e(TAG, "doInBackground invoked with invalid params");
                return null;
            }

            srcText = params[0];
            Log.d(TAG, String.format("doInBackground - srcText = %s", srcText));
            return Translator.translate(params[0], S_LANG, T_LANG);
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if (result != null) {

                OnClickListener textMarker = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // save the translation clicked to the exam DB
                        new SaveWordTask().execute(srcText, ((TextView) v).getText().toString());
                    }
                };

                int index = 1000;
                for (String s : result) {
                    TextView textView = new TextView(MainActivity.this);
                    textView.setText(s);
                    textView.setOnClickListener(textMarker);
                    MainActivity.this.getTranslationsLayout().addView(textView);
                }

                // on the next text change result will be cleared
                MainActivity.this.getSrcTextBox().
                        addTextChangedListener(new ClearTranslationsListener());
            }
        }
    }

    class SaveWordTask extends AsyncTask<String, Void, Long> {

        private static final String TAG = "SaveWordTask";

        @Override
        protected Long doInBackground(String... params) {
            if (params == null || params.length != 2) {
                Log.e(TAG, "doInBackground invoked with invalid params, nothing stored...");
                return -1l;
            }

            // params[0] - the source text
            // params[1] - the target text
            ExamDbFacade examDbFacade =  new ExamDbFacade(new ExamDbHelper(MainActivity.this));
            return examDbFacade.saveTranslation(params[0], params[1], Constants.S_LANG,
                    Constants.T_LANG);
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result == null || result == -1) {
                Toast.makeText(MainActivity.this, "Error on saving...", Toast.LENGTH_SHORT).show();
            } else if (result.intValue() == UNIQUE_CONTRAINT_FAILED_ERR_CODE) {
                Toast.makeText(MainActivity.this, "Translation is already saved.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, String.format("Saved translation number %s", result),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    class ClearTranslationsListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            MainActivity.this.getTranslationsLayout().removeAllViews();
            // this text watcher removes itself, from the text
            // box after the existing translations are cleared
            MainActivity.this.getSrcTextBox().removeTextChangedListener(this);
        }
    }

}
