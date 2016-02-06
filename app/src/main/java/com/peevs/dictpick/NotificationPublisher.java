package com.peevs.dictpick;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.peevs.dictpick.model.TestQuestion;

public class NotificationPublisher extends BroadcastReceiver {

    private static final String TAG = BroadcastReceiver.class.getSimpleName();

    public static int NOTIFICATION_ID = 0;
    public static String QUESTION_FROM_NOTIFICATION = "question-from-notification";

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive invoked for intent : " + intent);

        ExamDbFacade examDbFacade = new ExamDbFacade(new ExamDbHelper(context));

        // create notification for random test question
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Language srcLang = Language.valueOf(sharedPrefs.getString("key_pref_src_lang", "EN"));
        Language targetLang = Language.valueOf(sharedPrefs.getString("key_pref_target_lang", "BG"));
        Notification notification = getNotification(context, examDbFacade.getRandomTestQuestion(
                srcLang, targetLang, TestQuestion.WRONG_OPTIONS_COUNT,
                ExamDbContract.WordsTable.DEFAULT_BOOK_ID));

        // notification is auto dismissed when its clicked
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        // display the notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification getNotification(Context context, TestQuestion q) {
        String notificationContent = q.getQuestion().getVal() + " ?";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context).setSmallIcon(
                        R.drawable.notify_01)
                        .setContentTitle("DictPick")
                        .setContentText(notificationContent);

        //// TODO: 5.2.2016 г. The MainActivity should handle start by intend and open the exam tab
        Intent resultIntent = new Intent(context, MainActivity.class);

        resultIntent.putExtra(QUESTION_FROM_NOTIFICATION, q);

        // prepare back stack, so back button leads to home screen
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        return mBuilder.build();
    }
}