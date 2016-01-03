package com.peevs.dictpick.view;

import android.app.Activity;
import android.content.Context;

import com.peevs.dictpick.Language;

/**
 * Created by zarrro on 31.12.15.
 */
public interface DictPickTabFragmentHost {
    Language getForeignLanguage();

    Language getNativeLanguage();

    boolean getAutoSayQuestion();
}
