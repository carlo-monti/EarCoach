package org.vosk.earcoach;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Random;

enum Intervals{
    UNISON(Words.UNISON),
    MINOR_SECOND(Words.MINOR_SECOND),
    MAJOR_SECOND(Words.MAJOR_SECOND),
    MINOR_THIRD(Words.MINOR_THIRD),
    MAJOR_THIRD(Words.MAJOR_THIRD),
    FOURTH(Words.FOURTH),
    AUGMENTED_FOURTH(Words.AUGMENTED_FOURTH),
    FIFTH(Words.FIFTH),
    MINOR_SIXTH(Words.MINOR_SIXTH),
    MAJOR_SIXTH(Words.MAJOR_SIXTH),
    MINOR_SEVENTH(Words.MINOR_SEVENTH),
    MAJOR_SEVENTH(Words.MAJOR_SEVENTH),
    OCTAVE(Words.OCTAVE),
    MINOR_NINTH(Words.MINOR_NINTH),
    MAJOR_NINTH(Words.MAJOR_NINTH);

    private final String name;

    Intervals(String name){
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}

public class Interval extends Exercise{

    private Intervals currentAnswer;
    private final int HIGHEST_NOTE = 96;
    private final int LOWEST_NOTE = 36;
    protected int[][] currentQuestion;
    protected int[][] currentAnswerScore;
    protected String currentDirection;
    private LinkedHashSet<String> possibleAnswers = new LinkedHashSet<>();
    HarmonicOrMelodic typeOfInterval = new HarmonicOrMelodic(this);
    Direction direction = new Direction(teacher);
    Range range = new Range(teacher);

    public Interval(Teacher teacher){
        super(teacher);
        addParameter(typeOfInterval);
        addParameter(direction);
        addParameter(range);
    }

    @Override
    public void getNewQuestion(){
        int wholeRange = HIGHEST_NOTE - LOWEST_NOTE;
        Random random = new Random(System.currentTimeMillis());
        int valueAddRange = range.currentRange.second.second - range.currentRange.second.first + 1;
        int valueToBeAddedToStartingNote = range.currentRange.second.first + random.nextInt(valueAddRange);
        currentAnswer = Intervals.values()[valueToBeAddedToStartingNote];
        int lowNote = random.nextInt(wholeRange - valueToBeAddedToStartingNote) + LOWEST_NOTE;
        int highNote = lowNote + valueToBeAddedToStartingNote;

        if(typeOfInterval.getType() == typeOfInterval.HARMONIC){
            currentQuestion = new int[][]{{lowNote,highNote}};
            currentAnswerScore = new int[][]{{lowNote},{highNote},{},{lowNote,highNote}};
        }else if(typeOfInterval.getType() == typeOfInterval.MELODIC){
            currentDirection = direction.currentDirectionSetting.first;
            if(currentDirection.equals(Words.BOTH)){
                currentDirection = random.nextBoolean() ? Words.ASCENDING : Words.DESCENDING;
            }
            if(currentDirection.equals(Words.ASCENDING)){
                currentQuestion = new int[][]{{lowNote},{highNote}};
            }else{
                currentQuestion = new int[][]{{highNote},{lowNote}};
            }
            currentAnswerScore = currentQuestion;
        }else if(typeOfInterval.getType() == typeOfInterval.SING){
            currentDirection = direction.currentDirectionSetting.first;
            if(currentDirection.equals(Words.BOTH)){
                currentDirection = random.nextBoolean() ? Words.ASCENDING : Words.DESCENDING;
            }
            if(currentDirection.equals(Words.ASCENDING)){
                currentQuestion = new int[][]{{lowNote}};
                currentAnswerScore = new int[][]{{highNote},{},{lowNote},{highNote}};
            }else{
                currentQuestion = new int[][]{{highNote}};
                currentAnswerScore = new int[][]{{lowNote},{},{highNote},{lowNote}};
            }
        }
        possibleAnswers = new LinkedHashSet<>();
        int minInterval = range.currentRange.second.first;
        int maxInterval = range.currentRange.second.second;
        possibleAnswers.add(Words.ANSWER);
        if(typeOfInterval.getType() != typeOfInterval.SING){
            for(int i=minInterval; i<=maxInterval; i++){
                possibleAnswers.add(Intervals.values()[i].toString());
            }
        }
    }

    @Override
    public void askCurrentQuestion() {
        if(typeOfInterval.getType() == typeOfInterval.SING){
            speak(currentAnswer.toString().toLowerCase().replace("_"," ") + " " + currentDirection);
        }
        play(currentQuestion);
    }

    @Override
    public void tellAnswer() {
        speak(currentAnswer.toString().toLowerCase().replace("_"," "));
        play(currentAnswerScore);
    }

    @Override
    public void checkThisAnswer(String keyword) {
        if(keyword.equalsIgnoreCase(Words.ANSWER) && typeOfInterval.getType() == typeOfInterval.SING){
            play(currentAnswerScore);
            return;
        }
        if(keyword.equalsIgnoreCase(currentAnswer.toString())){
            speak(Words.CORRECT_ANSWER);
        }else{
            speak(Words.WRONG_ANSWER);
            speak(currentAnswer.toString());
            play(currentAnswerScore);
        }
    }

    @Override
    public String getExerciseInstructions() {
        if(typeOfInterval.getType() == typeOfInterval.SING){
            return Words.get(R.string.INTERVAL_SING_INSTRUCTION);
        }else{
            return Words.get(R.string.INTERVAL_INSTRUCTION);
        }
    }

    @Override
    public LinkedHashSet<String> getPossibleAnswer(){
        return possibleAnswers;
    }
}

class HarmonicOrMelodic implements ExerciseParameter{

    public final int SING = 2;
    public final int MELODIC = 1;
    public final int HARMONIC = 0;

    LinkedHashMap<String,Integer> melodicOrHarmonicToInt;
    Pair<String,Integer> currentTypeOfInterval;

    public HarmonicOrMelodic(Exercise owner){
        melodicOrHarmonicToInt = new LinkedHashMap<>();
        melodicOrHarmonicToInt.put(Words.SING,SING);
        melodicOrHarmonicToInt.put(Words.MELODIC,MELODIC);
        melodicOrHarmonicToInt.put(Words.HARMONIC,HARMONIC);
        String storedCurrentTypeOfInterval = owner.getStoredValueForParameter(this);
        if(!melodicOrHarmonicToInt.containsKey(storedCurrentTypeOfInterval)){
            storedCurrentTypeOfInterval = Words.MELODIC;
        }
        currentTypeOfInterval = new Pair<>(storedCurrentTypeOfInterval, melodicOrHarmonicToInt.get(storedCurrentTypeOfInterval));
        setValue(currentTypeOfInterval.first);
    }

    @Override
    public String getParameterName() {
        return Words.INTERVAL_TYPE;
    }

    @Override
    public LinkedHashSet<String> getParameterValueNames() {
        return new LinkedHashSet<>(melodicOrHarmonicToInt.keySet());
    }

    @Override
    public void setValue(String value) {
        currentTypeOfInterval = new Pair<>(value, melodicOrHarmonicToInt.get(value));
    }

    @Override
    public String getCurrentValueName() {
        return currentTypeOfInterval.first;
    }

    public int getType(){ return currentTypeOfInterval.second; }
}

class Direction implements ExerciseParameter{

    LinkedHashMap<String,Integer> ascendingOrDescendingToInt;
    Pair<String,Integer> currentDirectionSetting;
    Teacher teacher;

    public Direction(Teacher teacher){
        this.teacher = teacher;
        ascendingOrDescendingToInt = new LinkedHashMap<>();
        ascendingOrDescendingToInt.put(Words.ASCENDING,0);
        ascendingOrDescendingToInt.put(Words.DESCENDING,1);
        ascendingOrDescendingToInt.put(Words.BOTH,2);
        String storedCurrentTypeOfInterval = teacher.getStringPreferences(this.getClass().getSimpleName() + getParameterName());
        if(!ascendingOrDescendingToInt.containsKey(storedCurrentTypeOfInterval)){
            storedCurrentTypeOfInterval = Words.ASCENDING;
        }
        currentDirectionSetting = new Pair<>(storedCurrentTypeOfInterval, ascendingOrDescendingToInt.get(storedCurrentTypeOfInterval));
        setValue(currentDirectionSetting.first);
    }

    @Override
    public String getParameterName() {
        return Words.DIRECTION;
    }

    @Override
    public LinkedHashSet<String> getParameterValueNames() {
        return new LinkedHashSet<>(ascendingOrDescendingToInt.keySet());
    }

    @Override
    public void setValue(String value) {
        currentDirectionSetting = new Pair<>(value, ascendingOrDescendingToInt.get(value));
        teacher.setPreferences(this.getClass().getSimpleName() + getParameterName(),value);
    }

    @Override
    public String getCurrentValueName() {
        return currentDirectionSetting.first;
    }

}

class Range implements ExerciseParameter{

    LinkedHashMap<String,Pair<Integer,Integer>> rangeToMinMax;
    Pair<String,Pair<Integer,Integer>> currentRange;
    Teacher teacher;

    public Range(Teacher teacher){
        this.teacher = teacher;
        rangeToMinMax = new LinkedHashMap<>();
        rangeToMinMax.put(Words.SECONDS,new Pair<>(1,2));
        rangeToMinMax.put(Words.THIRDS,new Pair<>(3,4));
        rangeToMinMax.put(Words.FOURTH_TO_FIFTH,new Pair<>(5,7));
        rangeToMinMax.put(Words.SIXTHS,new Pair<>(8,9));
        rangeToMinMax.put(Words.SEVENTHS,new Pair<>(10,11));
        rangeToMinMax.put(Words.NINTHS,new Pair<>(13,14));
        rangeToMinMax.put(Words.UP_TO_THIRDS,new Pair<>(1,4));
        rangeToMinMax.put(Words.UP_TO_FIFTH,new Pair<>(1,7));
        rangeToMinMax.put(Words.UP_TO_SIXTHS,new Pair<>(1,9));
        rangeToMinMax.put(Words.UP_TO_SEVENTHS,new Pair<>(1,11));
        rangeToMinMax.put(Words.UP_TO_NINTHS,new Pair<>(1,14));
        String storedCurrentTypeOfInterval = teacher.getStringPreferences(this.getClass().getSimpleName() + getParameterName());
        if(!rangeToMinMax.containsKey(storedCurrentTypeOfInterval)){
            storedCurrentTypeOfInterval = Words.UP_TO_NINTHS;
        }
        currentRange = new Pair<>(storedCurrentTypeOfInterval, rangeToMinMax.get(storedCurrentTypeOfInterval));
        setValue(currentRange.first);
    }

    @Override
    public String getParameterName() {
        return Words.RANGE;
    }

    @Override
    public LinkedHashSet<String> getParameterValueNames() {
        return new LinkedHashSet<>(rangeToMinMax.keySet());
    }

    @Override
    public void setValue(String value) {
        currentRange = new Pair<>(value, rangeToMinMax.get(value));
        teacher.setPreferences(this.getClass().getSimpleName() + getParameterName(),value);
    }

    @Override
    public String getCurrentValueName() {
        return currentRange.first;
    }

    public int[] getRange(){
        return new int[]{currentRange.second.first,currentRange.second.second};
    }
}