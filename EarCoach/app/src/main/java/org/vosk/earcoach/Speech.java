package org.vosk.earcoach;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

public class Speech {
    TextToSpeech tts;
    SpeechListener listener;

    public Speech(Context context, SpeechListener speechListener) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.ITALIAN);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("VOSKa", "Language not supported");
                } else {
                    speechListener.speechHasEnded();
                    Log.e("VOSKa", "Initialization ok");
                }
            } else {
                Log.e("VOSKa", "Initialization failed");
            }
        });
        listener = speechListener;
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                speechListener.speechHasEnded();
            }

            @Override
            public void onError(String utteranceId) {
                speechListener.speechHasEnded();
            }
        });
        tts.addEarcon("beep", "org.vosk.demo", R.raw.beep_1);
    }

    public void playEarcon(){
        tts.playEarcon("beep",TextToSpeech.QUEUE_ADD,null,"0");
    }

    public void speak(String s) {
        Log.i("VOSKa","speech.Speak()" + Thread.currentThread().getName());
        tts.speak(s, TextToSpeech.QUEUE_ADD, null, "1");
    }

    public void stop(){
        tts.stop();
    }
}
