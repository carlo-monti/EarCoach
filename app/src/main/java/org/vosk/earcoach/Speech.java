package org.vosk.earcoach;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

/*
This class handles the text to speech function using the Google tts library. It has two methods,
one for translating a string to speech and another to play a recognizable sound (earcon).
 */

public class Speech {

    private final float speed = (float) 1.3;
    private TextToSpeech tts;
    private SpeechListener listener;


    public Speech(Context context, SpeechListener speechListener) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.ITALIAN);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    speechListener.onSpeechError("err");
                } else {
                    tts.setSpeechRate(speed);
                    speechListener.speechHasEnded();
                }
            } else {
                speechListener.onSpeechError("eerr");
            }
        });
        listener = speechListener;
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

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
        tts.speak("", TextToSpeech.QUEUE_ADD, null, "1");
    }

    public void speak(String s) {
        tts.speak(s, TextToSpeech.QUEUE_ADD, null, "1");
    }

    public void stop(){
        tts.stop();
    }
}
