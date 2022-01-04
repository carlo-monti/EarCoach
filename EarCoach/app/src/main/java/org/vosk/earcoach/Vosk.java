package org.vosk.earcoach;

import android.content.Context;
import android.util.Log;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

import java.io.IOException;
import java.util.HashSet;

public class Vosk implements RecognitionListener {

    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;
    private final Context context;
    private final VoskListener voskListener;
    private HashSet<String> acceptedKeywords;

    public Vosk(Context context, VoskListener voskListener){
        this.context = context;
        this.voskListener = voskListener;
        initModel();
    }

    @Override
    public void onResult(String hypothesis) {
        Log.i("VOSKa",hypothesis);
        hypothesis = hypothesis.substring(14,hypothesis.length()-3).toLowerCase();
        Log.i("VOSKa","-----------------------------> " + hypothesis);
        if(acceptedKeywords.contains(hypothesis)){
            voskListener.onResultFromVosk(hypothesis);
            return;
        }
        String[] manyWords = hypothesis.split(" ");
        boolean isAnAllowedSequence = true;
        for(String s : manyWords){
            if(!acceptedKeywords.contains(s)){
                isAnAllowedSequence = false;
            }
        }
        if(isAnAllowedSequence){
            voskListener.onResultFromVosk(hypothesis);
            return;
        }
    }

    public void setAcceptedKeywords(HashSet<String> keywords){
        acceptedKeywords = keywords;
    }

    @Override
    public void onFinalResult(String hypothesis) {
        if (speechStreamService != null) {
            speechStreamService = null;
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
    }

    @Override
    public void onError(Exception e) {
        Log.i("VOSKa",e.getMessage());
    }

    @Override
    public void onTimeout() {
    }

    public void initModel() {
        StorageService.unpack(context, "model-it", "model",
                (model) -> {
                    this.model = model;
                    recognizeMicrophone();
                },
                (exception) -> Log.i("VOSKa","Failed to unpack the model" + exception.getMessage()));
    }

    public void recognizeMicrophone() {
        if (speechService != null) {
            speechService.stop();
            speechService = null;
        } else {
            try {
                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {

            }
        }
    }

    public void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
        }
    }

    public void onDestroy(){
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }

        if (speechStreamService != null) {
            speechStreamService.stop();
        }
    }
}
