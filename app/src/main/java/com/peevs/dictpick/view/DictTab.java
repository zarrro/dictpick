package com.peevs.dictpick.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.Language;
import com.peevs.dictpick.R;
import com.peevs.dictpick.TabFragmentHost;
import com.peevs.dictpick.TextToSpeechTask;
import com.peevs.dictpick.Translator;
import com.peevs.dictpick.model.Text;
import com.peevs.dictpick.model.TranslationEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DictTab extends Fragment {

    private static final String TAG = "MainActivity";
    private ClipboardManager clipboard;

    private Language translateSrcLang;
    private Language translateTargetLang;
    private static final String S_LANG_KEY = "translateSrcLang";
    private static final String T_LANG_KEY = "translateTargetLang";

    // fragment view elements
    private EditText sourceTextInput;
    private ListView translationsView;
    private ImageButton pasteButton;
    private TabFragmentHost parentActivity;

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        this.parentActivity = (TabFragmentHost) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // default initialization if created for the 1st time
        translateSrcLang = parentActivity.getForeignLanguage();
        translateTargetLang = parentActivity.getNativeLanguage();

        // updated if it wasn't the 1st creation, there should be saved state
        if (savedInstanceState != null) {
            String tmp = savedInstanceState.getString(S_LANG_KEY);
            if (tmp != null) {
                translateSrcLang = Language.val(tmp);
            }
            tmp = savedInstanceState.getString(T_LANG_KEY);
            if (tmp != null) {
                translateTargetLang = Language.val(tmp);
            }
        }

        clipboard = (ClipboardManager)
                parentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dict, container, false);
        initFragmentViewMembers(v);
        attachButtonListeners(v);
        attachEditTextEventListeners();
        updatePasteActionState();
        updateViewsOnSwapLanguage(v);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(S_LANG_KEY, translateSrcLang.toString());
        outState.putString(T_LANG_KEY, translateTargetLang.toString());
    }

    private void initFragmentViewMembers(View v) {
        sourceTextInput = (EditText) v.findViewById(R.id.edit_srcText);
        pasteButton = (ImageButton) v.findViewById(R.id.btn_paste_clip);
        translationsView = (ListView) v.findViewById(R.id.layout_translation);
    }

    private void attachButtonListeners(View v) {
        ((ImageButton) v.findViewById(R.id.btn_listen_dict)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sayQuestion();
                    }
                });

        ((ImageButton) v.findViewById(R.id.swap_langs)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swapLanguages();
                    }
                });

        ((ImageButton) v.findViewById(R.id.btn_paste_clip)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pasteAndTranslate();
                    }
                });
    }

    private void translate() {
        // check if there is any translations (i.e. if Translate is not already clicked)
        if (translationsView.getChildCount() == 0) {
            String val = getSrcText();

            if (val != null && !(val = val.trim()).isEmpty()) {
                new TranslateTask(this.translateSrcLang, this.translateTargetLang).execute(val);
            }
        }
    }

    private void sayQuestion() {
        // you can listen only the foreign lang
        if (translateSrcLang == parentActivity.getForeignLanguage()) {
            String val = getSrcText();
            if (val != null && !val.isEmpty()) {
                new TextToSpeechTask(val, parentActivity.getForeignLanguage(),
                        parentActivity.getFilesDir()).execute();
            }
        }
    }

    private void pasteAndTranslate() {
        // Examines the item on the clipboard. If getVal() does not return null, the clip item
        // contains the text. Assumes that this application can only handle one item at a time.
        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

        // Gets the clipboard as text.
        CharSequence pasteData = item.getText();

        // If the string contains data, then the paste operation is done
        if (pasteData != null && sourceTextInput != null) {
            sourceTextInput.setText(pasteData);
            translate();

            // Non text clipboard content is not handled currently
        } else {
            Uri pasteUri = item.getUri();

            // If the URI contains something, try to get text from it
            if (pasteUri != null) {

                // calls a routine to resolve the URI and get data from it. This routine is not
                // presented here.
                Log.w(TAG, "Resolving clipboard URIs is not implemented");
            } else {

                // Something is wrong. The MIME type was plain text, but the clipboard does not
                // contain either text or a Uri. Report an error.
                Log.e(TAG, "Clipboard contains an invalid data type");
            }
        }
    }

    private void updatePasteActionState() {
        // If the clipboard doesn't contain data, disable the paste menu item.
        // If it does contain data, decide if you can handle the data.

        if (pasteButton != null && !(clipboard.hasPrimaryClip())) {
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

    public void swapLanguages() {
        Language tmp = translateSrcLang;
        translateSrcLang = translateTargetLang;
        translateTargetLang = tmp;
        updateViewsOnSwapLanguage(getView());
    }

    /**
     * @param v - the root view group for this fragment
     */
    private void updateViewsOnSwapLanguage(View v) {
        ((TextView) v.findViewById(R.id.translate_src_lang)).setText(translateSrcLang.toString());
        ((TextView) v.findViewById(R.id.translate_target_lang)).setText(translateTargetLang.toString());
        View listenBtn = v.findViewById(R.id.btn_listen_dict);
        boolean canListen = translateSrcLang == parentActivity.getForeignLanguage();
        listenBtn.setEnabled(canListen);
        listenBtn.setVisibility(canListen ? View.VISIBLE : View.GONE);
    }

    private String getSrcText() {
        if (sourceTextInput != null) {
            return sourceTextInput.getText().toString();
        }
        return null;
    }

    private void attachEditTextEventListeners() {
        // on the next text change result will be cleared
        sourceTextInput.
                addTextChangedListener(new ClearTranslationsListener());
        sourceTextInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    // makes the keyboard hidden on key enter press
                    InputMethodManager in = (InputMethodManager)
                            parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                    // NOTE: In the author's example, he uses an identifier
                    // called searchBar. If setting this code on your EditText
                    // then use v.getWindowToken() as a reference to your
                    // EditText is passed into this callback as a TextView

                    in.hideSoftInputFromWindow(v.getApplicationWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    translate();
                    return true;
                } else {
                    return false;
                }
            }
        });

        // renames virtual keyboard enter key
        sourceTextInput.setImeActionLabel(parentActivity.getResources().
                getString(R.string.enter_key_name), KeyEvent.KEYCODE_ENTER);


        // other event listeners could follow
    }

    class TranslateTask extends AsyncTask<String, Void, List<TranslationEntry>> {

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
        protected List<TranslationEntry> doInBackground(String... params) {

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
            ExamDbFacade examDbFacade = new ExamDbFacade(new ExamDbHelper(getActivity()));

            Map<String, Integer> existingInDb =
                    examDbFacade.filterExisting(srcText, translations, translateSrcLang,
                            translateTargetLang);

            List<TranslationEntry> translationItems = new ArrayList<>(10);
            TranslationEntry item;
            int id;
            assert translations != null;
            for (String s : translations) {
                if (existingInDb.containsKey(s)) {
                    id = existingInDb.get(s);
                } else {
                    id = ExamDbFacade.ID_NOT_EXISTS;
                }
                item = new TranslationEntry(id,
                        new Text(this.srcText, this.srcLang),
                        new Text(s, this.targetLang));
                translationItems.add(item);

            }
            return translationItems;
        }

        @Override
        protected void onPostExecute(List<TranslationEntry> wordEntries) {
            if (wordEntries != null) {
                DictTranslationsListAdapter translationsAdapter =
                        new DictTranslationsListAdapter(parentActivity,
                                wordEntries.toArray(new TranslationEntry[wordEntries.size()]));
                translationsView.setAdapter(translationsAdapter);
            } else if (errorMessage != null) {
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                errorMessage = null;
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
            if (translationsView.getAdapter() != null &&
                    translationsView.getAdapter().getCount() > 0) {
                translationsView.setAdapter(null);
            }
        }
    }
}
