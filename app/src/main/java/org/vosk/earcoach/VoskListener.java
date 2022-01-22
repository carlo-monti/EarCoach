package org.vosk.earcoach;

public interface VoskListener {
    void onResultFromVosk(String result);
    void onUnidentifiedResultFromVosk(String result);
    void onVoskError(String message);
}
