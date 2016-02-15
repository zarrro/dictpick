package com.peevs.dictpick.logic;

import android.os.AsyncTask;

/**
 * Created by zarrro on 13.2.2016 г..
 */
public interface TaskPostExecuteAction<T> {
    void onPostExecute(AsyncTask<?, ?, T> instance, T result);
}
