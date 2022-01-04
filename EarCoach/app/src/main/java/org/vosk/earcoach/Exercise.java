package org.vosk.earcoach;

import android.util.Log;
import android.util.Pair;

import org.billthefarmer.mididriver.GeneralMidiConstants;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public abstract class Exercise {
    protected LinkedHashMap<String,ExerciseParameter> exerciseParameters;
    protected Teacher teacher;
    protected int duration = 500;
    protected String selectedParameter;
    private boolean hasAskedQuestion;
    private boolean hasAlreadyGivenAnswer;

    public Exercise(Teacher teacher){
        this.teacher = teacher;
        exerciseParameters = new LinkedHashMap<>();
        addParameter(new Instrument(teacher));
        addParameter(new Speed(teacher,this));
        hasAskedQuestion = false;
    }

    public final void newQuestion(){
        hasAlreadyGivenAnswer = false;
        getNewQuestion();
    }

    abstract void getNewQuestion();

    public final void checkAnswer(String keyword){
        hasAlreadyGivenAnswer = true;
        checkThisAnswer(keyword);
    }

    abstract void checkThisAnswer(String keyword);

    public final void askQuestion(){
        hasAskedQuestion = true;
        askCurrentQuestion();
    }

    abstract void askCurrentQuestion();

    abstract void tellAnswer();
    abstract String getExerciseInstructions();

    abstract LinkedHashSet<String> getPossibleAnswer();

    public boolean hasAlreadyGivenAnswer(){ return hasAlreadyGivenAnswer; }

    public boolean hasAskedQuestion(){ return hasAskedQuestion;}

    public LinkedHashSet<String> getExerciseParameters(){ return new LinkedHashSet<>(new LinkedHashSet<>(exerciseParameters.keySet())); }

    public LinkedHashSet<String> getParameterValues(){ return new LinkedHashSet<>(exerciseParameters.get(selectedParameter).getParameterValueNames()); }

    protected void addParameter(ExerciseParameter parameter){ exerciseParameters.put(parameter.getParameterName(),parameter); }

    public String getSelectedParameter(){
        return selectedParameter;
    }

    public void setSelectedParameter(String parameter){
        selectedParameter = parameter;
    }

    public String getSelectedValue(){
        return exerciseParameters.get(selectedParameter).getCurrentValueName(); }

    protected String getStoredValueForParameter(Object parameter){
        return teacher.getStringPreferences(this.getClass().getSimpleName() + parameter.getClass().getSimpleName());
    }

    public void setSelectedValue(String value){
        exerciseParameters.get(selectedParameter).setValue(value);
        teacher.setPreferences(this.getClass().getSimpleName() + selectedParameter.getClass().getSimpleName(),value);
        hasAskedQuestion = false; // force user to start a new question
    }

    protected void speak(String string){
        teacher.speak(string);
    }

    protected void play(int[][] chordSequence){
        teacher.play(chordSequence, duration);
    }
}

class Instrument implements ExerciseParameter{
    LinkedHashMap<String,Integer> instrumentMidiValue = new LinkedHashMap<>();
    Pair<String,Integer> currentInstrument;
    Teacher teacher;

    public Instrument(Teacher teacher){
        this.teacher = teacher;
        instrumentMidiValue.put(Words.PIANO,(int)GeneralMidiConstants.ACOUSTIC_GRAND_PIANO);
        instrumentMidiValue.put(Words.ORGAN,(int)GeneralMidiConstants.DRAWBAR_ORGAN);
        instrumentMidiValue.put(Words.CLASSICAL_GUITAR,(int)GeneralMidiConstants.ACOUSTIC_GUITAR_NYLON);
        instrumentMidiValue.put(Words.ELECTRIC_GUITAR,(int)GeneralMidiConstants.ELECTRIC_GUITAR_CLEAN);
        instrumentMidiValue.put(Words.VIOLIN,(int)GeneralMidiConstants.VIOLIN);
        instrumentMidiValue.put(Words.TRUMPET,(int)GeneralMidiConstants.TRUMPET);
        instrumentMidiValue.put(Words.HARMONICA,(int)GeneralMidiConstants.HARMONICA);
        String storedInstrumentValue = teacher.getStringPreferences(getParameterName());
        if(!instrumentMidiValue.containsKey(storedInstrumentValue) || storedInstrumentValue == null){
            storedInstrumentValue = Words.PIANO;
        }
        currentInstrument = new Pair<>(storedInstrumentValue,instrumentMidiValue.get(storedInstrumentValue));
        setValue(currentInstrument.first);
    }

    @Override
    public String getParameterName() {
        return Words.INSTRUMENT;
    }

    @Override
    public LinkedHashSet<String> getParameterValueNames() {
        return new LinkedHashSet<>(new LinkedHashSet<>(instrumentMidiValue.keySet()));
    }

    @Override
    public void setValue(String value) {
        currentInstrument = new Pair<>(value,instrumentMidiValue.get(value));
        teacher.setSynthInstrument(currentInstrument.second);
        teacher.setPreferences(this.getClass().getSimpleName() + getParameterName(),value);
    }

    @Override
    public String getCurrentValueName() {
        return currentInstrument.first;
    }
}

class Speed implements ExerciseParameter{
    LinkedHashMap<String,Integer> speedToMillis = new LinkedHashMap<>();
    Pair<String,Integer> currentSpeed;
    Teacher teacher;
    Exercise exercise;

    public Speed(Teacher teacher,Exercise exercise){
        this.teacher = teacher;
        this.exercise = exercise;
        speedToMillis.put(Words.ONE,300);
        speedToMillis.put(Words.TWO,500);
        speedToMillis.put(Words.THREE,700);
        speedToMillis.put(Words.FOUR,900);
        speedToMillis.put(Words.FIVE,1200);

        String storedSpeed = teacher.getStringPreferences(getParameterName());
        if(!speedToMillis.containsKey(storedSpeed) || storedSpeed == null){
            storedSpeed = Words.THREE;
        }
        currentSpeed = new Pair<>(storedSpeed,speedToMillis.get(storedSpeed));
        setValue(currentSpeed.first);
    }

    @Override
    public String getParameterName() {
        return Words.SPEED;
    }

    @Override
    public LinkedHashSet<String> getParameterValueNames() {
        return new LinkedHashSet<>(new LinkedHashSet<>(speedToMillis.keySet()));
    }

    @Override
    public void setValue(String value) {
        currentSpeed = new Pair<>(value,speedToMillis.get(value));
        exercise.duration = currentSpeed.second;
        teacher.setPreferences(this.getClass().getSimpleName() + getParameterName(),value);
    }

    @Override
    public String getCurrentValueName() {
        return currentSpeed.first;
    }
}