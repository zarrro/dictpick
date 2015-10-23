package com.peevs.dictpick;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import static android.view.View.OnClickListener;
import static com.peevs.dictpick.ExamDbContract.UNIQUE_CONTRAINT_FAILED_ERR_CODE;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private String errorMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void translate(View v) {
        // check if there is any translations (i.e. if Translate is not already clicked)
        if (getTranslationsLayout().getChildCount() == 0) {
            String val = getSrcText();
            if (val != null && !(val = val.trim()).isEmpty()) {
                new TranslateTask().execute(val);
            }
        }
    }

    public void sayQuestion(View v) {
        String val = getSrcText();
        if(val != null && !val.isEmpty()) {
            sayQuestion(val);
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

    private String getSrcText() {
        EditText editTextSrc = (EditText) findViewById(R.id.edit_srcText);
        return editTextSrc.getText().toString();
    }

    EditText getSrcTextBox() {
        return (EditText) this.findViewById(R.id.edit_srcText);
    }

    class TranslateTask extends AsyncTask<String, Void, List<String>> {

        private static final String TAG = "GenerateTestTask";
        private String srcText = null;
        private String errorMessage = null;

        @Override
        protected List<String> doInBackground(String... params) {
            if (params == null || params.length != 1 || params[0] == null || params[0].isEmpty()) {
                Log.e(TAG, "doInBackground invoked with invalid params");
                return null;
            }

            srcText = params[0].trim().toLowerCase();

            Log.d(TAG, String.format("doInBackground - srcText = %s", srcText));
            List<String> result = null;
            try {
                result = Translator.translate(params[0],
                        MainActivity.this.srcLang.toString().toLowerCase(),
                        MainActivity.this.targetLang.toString().toLowerCase());
            } catch (IOException e) {
                errorMessage = "Translation service invocation failed...";
                Log.e(TAG, errorMessage, e);
            }
            return result;
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
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 16);
                    textView.setOnClickListener(textMarker);
                    MainActivity.this.getTranslationsLayout().addView(textView);
                }

                // on the next text change result will be cleared
                MainActivity.this.getSrcTextBox().
                        addTextChangedListener(new ClearTranslationsListener());
            } else if (errorMessage != null) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                errorMessage = null;
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

            final String sourceText = params[0];
            final String targetText = params[1];

            long wordId = -1;

            // save to DB
            ExamDbFacade examDbFacade = new ExamDbFacade(new ExamDbHelper(MainActivity.this));
            try {
                wordId = examDbFacade.saveTranslation(sourceText, targetText,
                        MainActivity.this.srcLang.toString().toLowerCase(),
                        MainActivity.this.targetLang.toString().toLowerCase());
            }catch (ExamDbFacade.AlreadyExistsException e) {
                errorMessage = e.getMessage();
            }

            return wordId;
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result == null || result == -1) {
                if(errorMessage != null) {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    errorMessage = null;
                } else {
                    Toast.makeText(MainActivity.this, "Error on saving...", Toast.LENGTH_SHORT).show();
                }
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
