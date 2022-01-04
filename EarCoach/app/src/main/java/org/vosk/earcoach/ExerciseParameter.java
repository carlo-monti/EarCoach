package org.vosk.earcoach;

import java.util.HashSet;

public interface ExerciseParameter {

    String getParameterName();

    HashSet<String> getParameterValueNames();

    void setValue(String value);

    String getCurrentValueName();
}
