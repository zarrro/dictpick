package com.peevs.dictpick;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.IOException;
import java.util.List;

import static android.view.View.OnClickListener;
import static com.peevs.dictpick.ExamDbContract.UNIQUE_CONTRAINT_FAILED_ERR_CODE;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private String errorMessage = null;
    private Toolbar toolbar;
    private ClipboardManager clipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActionBar((Toolbar) findViewById(R.id.toolbar));

        clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        attachEditTextEventListeners();
        updatePasteActionState();
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
        if (val != null && !val.isEmpty()) {
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

    public void pasteAndTranslate(View v) {
        // Examines the item on the clipboard. If getText() does not return null, the clip item
        // contains the text. Assumes that this application can only handle one item at a time.
        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

        // Gets the clipboard as text.
        CharSequence pasteData = item.getText();

        // If the string contains data, then the paste operation is done
        if (pasteData != null) {
            EditText srcText = (EditText) findViewById(R.id.edit_srcText);
            srcText.setText(pasteData);
            translate(null);
            return;

        // Non text clipboard content is not handled currently
        } else {
            Uri pasteUri = item.getUri();

            // If the URI contains something, try to get text from it
            if (pasteUri != null) {

                // calls a routine to resolve the URI and get data from it. This routine is not
                // presented here.
                Log.w(TAG, "Resolving clipboard URIs is not implemented");
                return;
            } else {

                // Something is wrong. The MIME type was plain text, but the clipboard does not
                // contain either text or a Uri. Report an error.
                Log.e(TAG, "Clipboard contains an invalid data type");
                return;
            }
        }
    }

    private LinearLayout getTranslationsLayout() {
        return (LinearLayout) this.findViewById(R.id.layout_translation);
    }

    private String getSrcText() {
        EditText editTextSrc = (EditText) findViewById(R.id.edit_srcText);
        return editTextSrc.getText().toString();
    }

    private void updatePasteActionState() {
        // If the clipboard doesn't contain data, disable the paste menu item.
        // If it does contain data, decide if you can handle the data.

        ImageButton pasteButton = (ImageButton) findViewById(R.id.btn_paste_clip);

        if (!(clipboard.hasPrimaryClip())) {

            pasteButton.setEnabled(false);

        } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(
                ClipDescription.MIMETYPE_TEXT_PLAIN))) {

            // This disables the paste menu item, since the clipboard has data but it is not plain text
            pasteButton.setEnabled(false);
        } else {

            // This enables the paste menu item, since the clipboard contains plain text.
            pasteButton.setEnabled(true);
        }
    }

    private EditText getSrcTextBox() {
        return (EditText) this.findViewById(R.id.edit_srcText);
    }

    private void attachEditTextEventListeners() {
        // on the next text change result will be cleared
        this.getSrcTextBox().
                addTextChangedListener(new ClearTranslationsListener());

        // other event listeners could follow
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
                    textView.setTextAppearance(MainActivity.this, R.style.TranslationTextStyle);
                    textView.setElevation(8.0f);
                    textView.setPaddingRelative(0, 5, 0, 0);
                    textView.setOnClickListener(textMarker);
                    MainActivity.this.getTranslationsLayout().addView(textView);
                }

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
            } catch (ExamDbFacade.AlreadyExistsException e) {
                errorMessage = e.getMessage();
            }

            return wordId;
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result == null || result == -1) {
                if (errorMessage != null) {
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
            if (MainActivity.this.getTranslationsLayout().getChildCount() > 0) {
                MainActivity.this.getTranslationsLayout().removeAllViews();
            }
        }
    }

}
