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
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.vosk.LibVosk;
import org.vosk.LogLevel;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Main extends Activity {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    Teacher teacher;
    TextView textView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle state) {

        super.onCreate(state);
        setContentView(R.layout.main_layout);
        textView = (TextView) findViewById(R.id.textView);
        LibVosk.setLogLevel(LogLevel.INFO);
        sharedPreferences = getSharedPreferences("EAR_COACH",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // get permissions and start Teacher or wait for callback onRequestPermissionsResult to start it
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            startTeacher();
        }
        SharedPreferences s = getSharedPreferences("EAR_COACH",MODE_PRIVATE);
        SharedPreferences.Editor e = s.edit();
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
        textView.setText("PAUSE");
        teacher.onPause();
    }

    @Override
    public void onDestroy() {
        teacher.onDestroy();
        textView.setText("DEstroy");
        super.onDestroy();
    }

    @Override
    public void onResume(){
        textView.setText("REsume");
        super.onResume();
        teacher.onResume();
    }

    public void isPlaying(){
        textView.setText("I'm playing");
    }

    public void isListening(){
        textView.setText("I'm listening");
    }

    public void isSpeaking(){
        textView.setText("I'm speaking");
    }

    public void savePreferences(String key, String value){
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringPreferences(String key){
        return sharedPreferences.getString(key,null);
    }
}
