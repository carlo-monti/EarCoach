package org.vosk.earcoach;

public interface SpeechListener {
    void speechHasEnded();
    void onSpeechError(String message);
}
