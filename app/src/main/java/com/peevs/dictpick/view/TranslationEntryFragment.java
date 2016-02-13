package com.peevs.dictpick.view;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peevs.dictpick.R;

/**
 * Created by zarrro on 11.2.2016 г..
 */
public class TranslationEntryFragment extends Fragment {

    private TranslationEntryFragment teInstance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //TODO: using the standard databinding android library might remove this code
        View v = inflater.inflate(R.layout.translation_entry, container, false);

        ((TextView) v.findViewById(R.id.te_fragment_src_lang)).setText(teInstance.get);
        return v;
    }

    public TranslationEntryFragment getTeInstance() {
        return teInstance;
    }

    public void setTeInstance(TranslationEntryFragment teInstance) {
        this.teInstance = teInstance;
    }
}
