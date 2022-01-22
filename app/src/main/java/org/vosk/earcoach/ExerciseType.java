package org.vosk.earcoach;

import androidx.annotation.NonNull;

/*
Every entry in this enum represent an exercise class. Whenever a new exercise is created, a reference
to its class must be added to the enum with the complete class name and the exercise name.
 */

public enum ExerciseType {
    INTERVALS("org.vosk.earcoach.Interval",Words.INTERVALS),
    MELODY("org.vosk.earcoach.Melody",Words.MELODY);
    // put the reference for the new exercise class here

    private final String className;
    private final String name;

    ExerciseType(String className, String name){
        this.className = className;
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() { return name;
    }

    public String getClassName(){
        return className;
    }
}
