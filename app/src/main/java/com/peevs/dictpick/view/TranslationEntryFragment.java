package com.peevs.dictpick.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.R;
import com.peevs.dictpick.logic.PostSaveTranslation;
import com.peevs.dictpick.logic.SaveTranslationEntryTask;
import com.peevs.dictpick.model.TranslationEntry;

/**
 * Created by zarrro on 11.2.2016 г..
 */
public class TranslationEntryFragment extends Fragment {

    private TranslationEntry teInstance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //TODO: using the standard databinding android library might remove this code
        View v = inflater.inflate(R.layout.translation_entry, container, false);

        if(teInstance != null) {
            ((TextView) v.findViewById(R.id.te_fragment_src_lang)).setText(
                    teInstance.getSrcText().getLang().toString());
            ((TextView) v.findViewById(R.id.te_fragment_src_text)).setText(
                    teInstance.getSrcText().getVal());
            ((TextView) v.findViewById(R.id.te_fragment_target_lang)).setText(
                    teInstance.getTargetText().getLang().toString());
            EditText editTargetText = ((EditText) v.findViewById(R.id.te_fragment_target_text));
            editTargetText.setText(
                    teInstance.getTargetText().getVal());
            editTargetText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    teInstance.getTargetText().setVal(s.toString());
                }
            });


            ((Button) v.findViewById(R.id.btn_save_translation_entry)).setOnClickListener(
                    new SaveTranslationEntryTask(getActivity(), teInstance,
                            new PostSaveTranslation() {
                                @Override
                                public void onPostExecute(AsyncTask<?, ?, Long> task, Long result) {
                                    super.onPostExecute(task, result);
                                    getActivity().finish();
                                }
                            }));
        }
        return v;
    }

    public TranslationEntry getTeInstance() {
        return teInstance;
    }

    public void setTeInstance(TranslationEntry teInstance) {
        this.teInstance = teInstance;
    }

}
