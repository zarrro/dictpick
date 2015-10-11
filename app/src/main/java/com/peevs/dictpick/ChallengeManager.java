package com.peevs.dictpick;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by zarrro on 5.10.2015 г..
 */
public class ChallengeManager {

    private static final String TAG = ChallengeManager.class.getSimpleName();

    public enum ChallengeFrequency {
        NONE(-1), RARE(7200000), NORMAL(3600000), AGRESSIVE(60000), DEBUG(1000);

        public int value;

        ChallengeFrequency(int value) {
            this.value = value;
        }
    }

    public static final int DEBUG_NOTIFICA = 1000;
    private final Context context;

    public ChallengeManager(Context context) {
        if (context == null)
            throw new IllegalArgumentException("context is null");
        this.context = context;
    }

    public void setRecurringChallenge(String frequency) {
        setRecurringChallenge(ChallengeFrequency.valueOf(frequency));
    }

    public void setRecurringChallenge(ChallengeFrequency frequency) {
        Log.i(TAG, "setRecurringChallenge frequency: " + frequency);
        if (frequency != ChallengeFrequency.NONE) {
            setRecurringChallenge(frequency.value);
        } else {
            disableRecurringChallange();
        }
    }

    public void setRecurringChallenge(int delay) {
        Log.i(TAG, "setRecurringChallenge delay: " + delay);

        // AlarmManager will fire intent for the NotificationPublisher after specified time elapses

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delay, delay, createPendingChallangeNotification());
    }

    private void disableRecurringChallange() {
        Log.i(TAG, "disableRecurringChallange invoked");
        // AlarmManager will fire intent for the NotificationPublisher after specified time elapses
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createPendingChallangeNotification());
    }

    private PendingIntent createPendingChallangeNotification() {
        // intent to invoke the NotificationPublisher
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, 0, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
