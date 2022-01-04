package org.vosk.earcoach;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class EarCoach extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences("EAR_COACH",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("STARTED_EXERCISE","NONE");
        editor.apply();
    }

}
