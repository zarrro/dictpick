package com.peevs.dictpick;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.view.View.OnClickListener;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private String errorMessage = null;
    private Toolbar toolbar;
    private ClipboardManager clipboard;

    private Language translateSrcLang;
    private Language translateTargetLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActionBar((Toolbar) findViewById(R.id.toolbar));

        clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        attachEditTextEventListeners();
        updatePasteActionState();
        updateTranslateLanguages();
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
                new TranslateTask(this.translateSrcLang, this.translateTargetLang).execute(val);
            }
        }
    }

    public void sayQuestion(View v) {
        // you can listen only the foreign lang
        if (translateSrcLang == foreignLang) {
            String val = getSrcText();
            if (val != null && !val.isEmpty()) {
                sayQuestion(val);
            }
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

    private void updateTranslateLanguages() {
        // by default translation is from the foreign to the native lang
        translateSrcLang = foreignLang;
        translateTargetLang = nativeLang;
        updateViewsOnSwapLanguage();
    }

    private void updateViewsOnSwapLanguage() {
        ((TextView) findViewById(R.id.translate_src_lang)).setText(translateSrcLang.toString());
        ((TextView) findViewById(R.id.translate_target_lang)).setText(translateTargetLang.toString());
        ((ImageButton) findViewById(R.id.btn_listen_main)).setEnabled(translateSrcLang == foreignLang);
    }

    public void swapLanguages(View v) {
        Language tmp = translateSrcLang;
        translateSrcLang = translateTargetLang;
        translateTargetLang = tmp;
        updateViewsOnSwapLanguage();
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


    class TranslationItem {

        private final String srcText;
        private final String targetText;
        private final Language srcLang;
        private final Language targetLang;

        private long dbId = ExamDbFacade.ID_NOT_EXISTS;
        private TextView textView;
        private ImageButton star;

        TranslationItem(String srcText, String targetText, Language srcLang, Language targetLang) {
            this.srcText = srcText;
            this.targetText = targetText;
            this.srcLang = srcLang;
            this.targetLang = targetLang;
        }

        View initView() {
            initTextView();
            initStar();

            RelativeLayout layout = new RelativeLayout(MainActivity.this);
            layout.addView(textView);
            layout.addView(star);

            ((RelativeLayout.LayoutParams) textView.getLayoutParams()).addRule(
                    RelativeLayout.ALIGN_PARENT_LEFT);
            ((RelativeLayout.LayoutParams) star.getLayoutParams()).addRule(
                    RelativeLayout.ALIGN_PARENT_RIGHT);
            return layout;
        }

        /**
         * Update existing translations view.
         * - to be invoked on the UI thread only
         * - to be invoked only after initView()
         */
        void updateView() {
            if(star == null)
                throw new IllegalStateException("star is not set. Invoke initView.");
            updateStarView();
            // other view updates to be added here when needed
        }

        private void initStar() {
            star = new ImageButton(MainActivity.this);
            star.setBackgroundColor(Color.TRANSPARENT);
            updateStarView();
        }

        private void updateStarView() {
            if (this.dbId != ExamDbFacade.ID_NOT_EXISTS) {
                star.setImageResource(R.drawable.ic_star_enabled);
                star.setEnabled(false);
            } else {
                star.setImageResource(R.drawable.ic_star_disabled);
                star.setOnClickListener(createSaveTranslationTask());
            }
        }

        private void initTextView() {
            textView = new TextView(MainActivity.this);
            textView.setText(this.targetText);
            textView.setTextAppearance(MainActivity.this, R.style.TranslationTextStyle);
            textView.setPaddingRelative(0, 5, 0, 0);
        }

        private OnClickListener createSaveTranslationTask() {
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // save the translation clicked to the exam DB
                    new SaveTranslationItemTask(TranslationItem.this).execute();
                }
            };
        }

        void setDbId(long dbId) {
            this.dbId = dbId;
        }

        long getDbId() {
            return dbId;
        }

        String getTargetText() {
            return targetText;
        }

        String getSrcText() {
            return srcText;
        }

        Language getSrcLang() {
            return srcLang;
        }

        Language getTargetLang() {
            return targetLang;
        }
    }

    class TranslationItemOnClickListener implements OnClickListener {

        private final SaveTranslationItemTask saveTranslationItemTask;

        TranslationItemOnClickListener(SaveTranslationItemTask saveTranslationItemTask) {
            this.saveTranslationItemTask = saveTranslationItemTask;
        }

        @Override
        public void onClick(View v) {
            saveTranslationItemTask.execute();
        }
    }

    class TranslateTask extends AsyncTask<String, Void, List<TranslationItem>> {

        private static final String TAG = "GenerateTestTask";
        private final Language srcLang;
        private final Language targetLang;
        private String srcText = null;
        private String errorMessage = null;

        TranslateTask(Language srcLang, Language targetLang) {
            this.srcLang = srcLang;
            this.targetLang = targetLang;
        }

        @Override
        protected List<TranslationItem> doInBackground(String... params) {

            Log.d(TAG, String.format("doInBackground - srcText = %s", srcText));
            if (params == null || params.length != 1 || params[0] == null || params[0].isEmpty()) {
                Log.e(TAG, "doInBackground invoked with invalid params");
                return null;
            }

            srcText = params[0].trim().toLowerCase();
            List<String> translations = null;
            try {
                translations = Translator.translate(srcText, srcLang.toString().toLowerCase(),
                        targetLang.toString().toLowerCase());
            } catch (IOException e) {
                errorMessage = "Translation service invocation failed...";
                Log.e(TAG, errorMessage, e);
            }

            // check already existing to DB
            ExamDbFacade examDbFacade = new ExamDbFacade(new ExamDbHelper(MainActivity.this));

            Map<String, Integer> existingInDb =
                    examDbFacade.filterExisting(srcText, translations, translateSrcLang,
                            translateTargetLang);

            List<TranslationItem> translationItems = new ArrayList<TranslationItem>(10);
            TranslationItem item;
            for (String s : translations) {
                item = new TranslationItem(this.srcText, s, this.srcLang, this.targetLang);

                translationItems.add(item);
                if (existingInDb.containsKey(s)) {
                    item.setDbId(existingInDb.get(s));
                }
            }
            return translationItems;
        }

        @Override
        protected void onPostExecute(List<TranslationItem> translationItems) {
            if (translationItems != null) {
                ViewGroup translationLayout = MainActivity.this.getTranslationsLayout();
                for (TranslationItem ti : translationItems) {
                    translationLayout.addView(ti.initView());
                }
            } else if (errorMessage != null) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                errorMessage = null;
            }
        }
    }

    class SaveTranslationItemTask extends AsyncTask<Void, Void, Long> {

        private static final String TAG = "SaveTranslationItemTask";
        private final TranslationItem ti;

        SaveTranslationItemTask(TranslationItem ti) {
            this.ti = ti;
        }

        @Override
        protected Long doInBackground(Void... params) {
            long wordId = ExamDbFacade.ID_NOT_EXISTS;

            // save to DB
            ExamDbFacade examDbFacade = new ExamDbFacade(new ExamDbHelper(MainActivity.this));
            try {
                wordId = examDbFacade.saveTranslation(ti.getSrcText(), ti.getTargetText(),
                        ti.getSrcLang().toString().toLowerCase(),
                        ti.getTargetLang().toString().toLowerCase());
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
            } else if (result.intValue() == ExamDbFacade.UNIQUE_CONTRAINT_FAILED_ERR_CODE) {
                Toast.makeText(MainActivity.this, "Translation is already saved.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, String.format("Saved translation number %s", result),
                        Toast.LENGTH_SHORT).show();

                ti.setDbId(result);
                ti.updateView();
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
