package org.vosk.earcoach;

import android.app.Application;
import android.content.SharedPreferences;

/*
This class represents the whole app. It is only used to set a Shared Preference to indicate
that the app has just been started and that no exercise has already been started.
*/

public class EarCoach extends Application {

    public static String EAR_COACH = "EAR_COACH";
    public static String IS_A_RESTART = "IS_A_RESTART";
    public static String HAS_ALREADY_BEEN_STARTED = "HAS_ALREADY_BEEN_STARTED";
    public static String STARTED_EXERCISE = "STARTED_EXERCISE";
    public static String WELCOME_AGAIN = "WELCOME_AGAIN";
    public static String NONE = "NONE";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences(EAR_COACH,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(IS_A_RESTART,false);
        editor.putBoolean(WELCOME_AGAIN,false);
        editor.putString(STARTED_EXERCISE,NONE);
        editor.apply();
    }
}
