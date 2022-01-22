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

/*
This class handles the speech recognition using the Vosk library. The recognizer is initialized
and after that can be put on pause or reactivated. Whenever the recognizer has a result it calls
the onResult method that checks if the recognized word is contained in the set of the accepted keywords
(that can be set with the method setAcceptedKeywords).

The method first check if the recognized word is contained as is. If not, it checks if the hypothesis
is a set of words that are individually contained into the set. This is useful when the answer to a
question is a long sequence of words (i.e. a melody).
If the recognized word is contained among the accepted keywords, the recognizer calls the callback function
on theacher by passing the keyword.
 */

public class Vosk implements RecognitionListener {

    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;
    private final Context context;
    private final VoskListener voskListener;
    private HashSet<String> acceptedKeywords;
    private boolean isListening;

    public Vosk(Context context, VoskListener voskListener){
        this.context = context;
        this.voskListener = voskListener;
        isListening = false;
        initModel();
    }

    @Override
    public void onResult(String hypothesis) {
        if(isListening){
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
        voskListener.onVoskError(e.getMessage());
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
                (exception) -> voskListener.onVoskError("Failed to unpack the model" + exception.getMessage()));
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
                voskListener.onVoskError("Failed to start microphone recognizer" + e.getMessage());
            }
        }
    }

    public void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
        }
        isListening = !checked;
        Log.i("VOSKa","PAUSE " + String.valueOf(checked));
    }

    public void quitVosk(){
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }

        if (speechStreamService != null) {
            speechStreamService.stop();
        }
    }
}
