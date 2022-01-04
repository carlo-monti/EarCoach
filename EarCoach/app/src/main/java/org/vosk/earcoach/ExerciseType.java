package org.vosk.earcoach;

import androidx.annotation.NonNull;

public enum ExerciseType {
    INTERVALS("org.vosk.earcoach.Interval",Words.INTERVALS),
    MELODY("org.vosk.earcoach.Melody",Words.MELODY);

    private final String className;
    private final String name;

    ExerciseType(String className, String name){
        this.className = className;
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public String getClassName(){
        return className;
    }
}
