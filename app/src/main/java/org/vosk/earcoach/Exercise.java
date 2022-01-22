package org.vosk.earcoach;

import android.util.Log;
import android.util.Pair;
import org.billthefarmer.mididriver.GeneralMidiConstants;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;

/*
This abstract class represent an exercise and it must be extended by an actual class that specifies
the behaviour of the exercise. This class has a list of exerciseParameters (more precisely: a
map from String and exerciseParameters) and it handles the action of selecting the parameter and
setting the parameter value. A parameter (a class that implements the exerciseParameters interface)
can be added to this list calling the addParameter() method. There are two parameters already added:
instrument and speed.
 */

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
        addParameter(new Instrument(this, teacher));
        addParameter(new Speed(this));
        hasAskedQuestion = false;
    }

    //abstract methods that every subclass must implements:

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

    // protected method that the subclass can use to interact with the user

    protected void speak(String string){
        teacher.speak(string);
    }

    protected void play(int[][] chordSequence){
        teacher.play(chordSequence, duration);
    }

    // public methods that are called from Teacher

    public boolean hasAlreadyGivenAnswer(){ return hasAlreadyGivenAnswer; }

    public boolean hasAskedQuestion(){ return hasAskedQuestion;}

    // methods to handle the action of selecting the parameter and setting the parameter value.

    public LinkedHashSet<String> getExerciseParameters(){ return new LinkedHashSet<>(new LinkedHashSet<>(exerciseParameters.keySet())); }

    public LinkedHashSet<String> getParameterValues(){ return new LinkedHashSet<>(Objects.requireNonNull(exerciseParameters.get(selectedParameter)).getParameterValueNames()); }

    protected void addParameter(ExerciseParameter parameter){ exerciseParameters.put(parameter.getParameterName(),parameter); }

    public String getSelectedParameter(){
        return selectedParameter;
    }

    public void setSelectedParameter(String parameter){
        selectedParameter = parameter;
    }

    public String getSelectedValue(){
        return Objects.requireNonNull(exerciseParameters.get(selectedParameter)).getCurrentValueName();
    }

    protected String getStoredValueForParameter(ExerciseParameter parameter){
        return teacher.getStringPreferences(this.getClass().getSimpleName() + parameter.getParameterName());
    }

    public void setSelectedValue(String value){
        Objects.requireNonNull(exerciseParameters.get(selectedParameter)).setValue(value);
        teacher.setPreferences(this.getClass().getSimpleName() + selectedParameter,value);
        hasAskedQuestion = false; // force user to start a new question
    }

}

class Instrument implements ExerciseParameter{
    LinkedHashMap<String,Integer> instrumentMidiValue = new LinkedHashMap<>();
    Pair<String,Integer> currentInstrument;
    Exercise owner;
    Teacher teacher;

    public Instrument(Exercise owner, Teacher teacher){
        this.owner = owner;
        this.teacher = teacher;
        instrumentMidiValue.put(Words.PIANO,(int)GeneralMidiConstants.ACOUSTIC_GRAND_PIANO);
        instrumentMidiValue.put(Words.ORGAN,(int)GeneralMidiConstants.DRAWBAR_ORGAN);
        instrumentMidiValue.put(Words.ACOUSTIC_GUITAR,(int)GeneralMidiConstants.ACOUSTIC_GUITAR_STEEL);
        instrumentMidiValue.put(Words.VIOLIN,(int)GeneralMidiConstants.VIOLIN);
        instrumentMidiValue.put(Words.TRUMPET,(int)GeneralMidiConstants.TRUMPET);
        instrumentMidiValue.put(Words.FLUTE,(int)GeneralMidiConstants.FLUTE);
        String storedInstrumentValue = owner.getStoredValueForParameter(this);
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
    }

    @Override
    public String getCurrentValueName() {
        return currentInstrument.first;
    }
}

class Speed implements ExerciseParameter{
    LinkedHashMap<String,Integer> speedToMillis = new LinkedHashMap<>();
    Pair<String,Integer> currentSpeed;
    Exercise exercise;

    public Speed(Exercise owner){
        this.exercise = owner;
        speedToMillis.put(Words.ONE,300);
        speedToMillis.put(Words.TWO,500);
        speedToMillis.put(Words.THREE,700);
        speedToMillis.put(Words.FOUR,900);
        speedToMillis.put(Words.FIVE,1200);
        String storedSpeed = owner.getStoredValueForParameter(this);
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
    }

    @Override
    public String getCurrentValueName() {
        return currentSpeed.first;
    }
}