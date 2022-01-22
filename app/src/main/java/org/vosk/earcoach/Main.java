// Copyright 2019 Alpha Cephei Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.vosk.earcoach;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.vosk.LibVosk;
import org.vosk.LogLevel;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/*
This class represents the only Activity and handles the UI. It asks permissions and then creates the
Teacher object starting its method as a new thread.s
 */

public class Main extends Activity {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    Teacher teacher;
    TextView textView;
    TextView textViewInfo;
    Button closeApp;
    ImageView imageView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main_layout);
        textView = (TextView) findViewById(R.id.textView);
        textViewInfo = (TextView) findViewById(R.id.textViewInfo);
        String t = getResources().getString(R.string.WHEN_YOU_ARE_STUCK_SAY) + " " + getResources().getString(R.string.INFO);
        textViewInfo.setText(t);
        closeApp = (Button) findViewById(R.id.closeApp);
        imageView = (ImageView) findViewById(R.id.imageView);
        closeApp.setOnClickListener(v -> {
            teacher.onPause();
            finishAndRemoveTask();
        });
        LibVosk.setLogLevel(LogLevel.INFO);
        sharedPreferences = getSharedPreferences(EarCoach.EAR_COACH,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        // get permissions and start Teacher or wait for callback onRequestPermissionsResult to start it
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            startTeacher();
        }
        setText("");
    }

    private void startTeacher(){
        teacher = new Teacher(this);
        teacher.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTeacher();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        teacher.onPause();
    }

    @Override
    public void onDestroy() {
        teacher.onDestroy();
        super.onDestroy();
    }

    public void isPlaying(){
        imageView.setImageResource(R.drawable.music);
    }

    public void isListening(){
        imageView.setImageResource(R.drawable.listen);
    }

    public void isSpeaking(){
        imageView.setImageResource(R.drawable.speak);
    }

    public void setText(String message){
        textView.setText(message);
    }

    public void onError(String message){
        setText(message);
    }

    public void savePreferences(String key, String value){
        editor.putString(key, value);
        editor.apply();
    }

    public void savePreferences(String key, boolean value){
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBooleanPreferences(String key){ return sharedPreferences.getBoolean(key,false); }

    public String getStringPreferences(String key){
        return sharedPreferences.getString(key,null);
    }
}
