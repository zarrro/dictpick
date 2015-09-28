package com.peevs.dictpick;

import android.app.Application;

import java.io.IOException;

/**
 * Created by zarrro on 22.8.2015 г..
 */
public class DictPick extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ExamDbPrepared db = new ExamDbPrepared(this);
        db.createDataBase();
    }
}
