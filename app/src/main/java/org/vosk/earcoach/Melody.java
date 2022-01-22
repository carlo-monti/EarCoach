package org.vosk.earcoach;

import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Random;

enum Notes{
    ROOT(Words.ROOT,0),
    SECOND(Words.SECOND,2),
    THIRD(Words.THIRD,4),
    FOURTH(Words.FOURTH,5),
    FIFTH(Words.FIFTH,7),
    SIXTH(Words.SIXTH,9),
    SEVENTH(Words.SEVENTH,11),
    OCTAVE(Words.OCTAVE,12),
    NINTH(Words.NINTH,14);

    private final String name;
    private final int semitones;

    Notes(String name, int semitones){
        this.name = name;
        this.semitones = semitones;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }

    public int getSemitones(){
        return this.semitones;
    }
}

public class Melody extends Exercise{

    private LinkedHashSet<String> possibleAnswers = new LinkedHashSet<>();
    Length length = new Length(this);
    IncludedNotes includedNotes = new IncludedNotes(this);
    int[][] currentQuestion;
    Notes[] currentQuestionScheme;

    public Melody(Teacher teacher){
        super(teacher);
        addParameter(length);
        addParameter(includedNotes);
    }

    public void getNewQuestion() {
        Random random = new Random(System.currentTimeMillis());
        int root = random.nextInt(12) + 40;
        int noteRange = includedNotes.currentIncludedNotes.second.length;
        currentQuestionScheme = new Notes[length.currentLength.second];
        currentQuestion = new int[currentQuestionScheme.length + 2][];
        possibleAnswers = new LinkedHashSet<>();
        for(Notes n : includedNotes.currentIncludedNotes.second){
            possibleAnswers.add(n.toString());
        }
        currentQuestion[0] = new int[]{root, root + 4, root + 7}; // build root chord
        currentQuestion[1] = new int[]{}; // makes a pause
        Log.i("VOSKa","Melodia");
        for(int i=0; i<currentQuestionScheme.length; i++){ // writes melody
            int newNoteIndex = random.nextInt(noteRange);
            currentQuestionScheme[i] = includedNotes.currentIncludedNotes.second[newNoteIndex];
            currentQuestion[i+2] = new int[]{currentQuestionScheme[i].getSemitones() + root};
            Log.i("VOSKa",String.valueOf(currentQuestionScheme[i]));
        }
    }

    public void askCurrentQuestion(){
        play(currentQuestion);
    }

    public void tellAnswer() {
    }

    public void checkThisAnswer(String keyword) {
        String[] splittedKeywords = keyword.split(" ");
        boolean isCorrect = true;
        for(int i=0; i<splittedKeywords.length; i++){
            if(!currentQuestionScheme[i].toString().equals(splittedKeywords[i])){
                isCorrect = false;
            }
        }
        if(isCorrect){
            speak(Words.CORRECT_ANSWER);
        }else{
            speak(Words.WRONG_ANSWER);
            for(Notes n : currentQuestionScheme){
                speak(n.toString());
            }
            int[][] onlyMelody = Arrays.copyOfRange(currentQuestion, 2, currentQuestion.length);
            play(onlyMelody);
        }
    }

    @Override
    public String getExerciseInstructions() {
        return Words.MELODY_INSTRUCTIONS;
    }

    @Override
    public LinkedHashSet<String> getPossibleAnswer() {
        return possibleAnswers;
    }
}

class Length implements ExerciseParameter{

    LinkedHashMap<String,Integer> numberNameToInt;
    Pair<String,Integer> currentLength;

    public Length(Exercise owner){
        numberNameToInt = new LinkedHashMap<>();
        numberNameToInt.put(Words.ONE,1);
        numberNameToInt.put(Words.TWO,2);
        numberNameToInt.put(Words.THREE,3);
        numberNameToInt.put(Words.FOUR,4);
        numberNameToInt.put(Words.FIVE,5);
        numberNameToInt.put(Words.SIX,6);
        String storedCurrentTypeOfInterval = owner.getStoredValueForParameter(this);
        if(!numberNameToInt.containsKey(storedCurrentTypeOfInterval)){
            storedCurrentTypeOfInterval = Words.FOUR;
        }
        currentLength = new Pair<>(storedCurrentTypeOfInterval, numberNameToInt.get(storedCurrentTypeOfInterval));
        setValue(currentLength.first);
    }

    @Override
    public String getParameterName() {
        return Words.LENGTH;
    }

    @Override
    public LinkedHashSet<String> getParameterValueNames() {
        return new LinkedHashSet<>(numberNameToInt.keySet());
    }

    @Override
    public void setValue(String value) {
        currentLength = new Pair<>(value, numberNameToInt.get(value));
    }

    @Override
    public String getCurrentValueName() {
        return currentLength.first;
    }
}

class IncludedNotes implements ExerciseParameter{

    LinkedHashMap<String,Notes[]> numberNameToNotesArray;
    Pair<String,Notes[]> currentIncludedNotes;

    public IncludedNotes(Exercise owner){
        numberNameToNotesArray = new LinkedHashMap<>();
        numberNameToNotesArray.put(Words.UP_TO_THIRDS,new Notes[]{Notes.ROOT,Notes.SECOND,Notes.THIRD});
        numberNameToNotesArray.put(Words.UP_TO_FOURTH,new Notes[]{Notes.ROOT,Notes.SECOND,Notes.THIRD,Notes.FOURTH});
        numberNameToNotesArray.put(Words.UP_TO_FIFTH,new Notes[]{Notes.ROOT,Notes.SECOND,Notes.THIRD,Notes.FOURTH,Notes.FIFTH});
        numberNameToNotesArray.put(Words.UP_TO_SIXTHS,new Notes[]{Notes.ROOT,Notes.SECOND,Notes.THIRD,Notes.FOURTH,Notes.FIFTH,Notes.SIXTH});
        numberNameToNotesArray.put(Words.UP_TO_SEVENTHS,new Notes[]{Notes.ROOT,Notes.SECOND,Notes.THIRD,Notes.FOURTH,Notes.FIFTH,Notes.SIXTH,Notes.SEVENTH});
        numberNameToNotesArray.put(Words.UP_TO_OCTAVE,new Notes[]{Notes.ROOT,Notes.SECOND,Notes.THIRD,Notes.FOURTH,Notes.FIFTH,Notes.SIXTH,Notes.SEVENTH,Notes.OCTAVE});
        numberNameToNotesArray.put(Words.UP_TO_NINTHS,new Notes[]{Notes.ROOT,Notes.SECOND,Notes.THIRD,Notes.FOURTH,Notes.FIFTH,Notes.SIXTH,Notes.SEVENTH,Notes.OCTAVE,Notes.NINTH});
        String storedCurrentIncludedNotes = owner.getStoredValueForParameter(this);
        if(!numberNameToNotesArray.containsKey(storedCurrentIncludedNotes)){
            storedCurrentIncludedNotes = Words.UP_TO_THIRDS;
        }
        currentIncludedNotes = new Pair<>(storedCurrentIncludedNotes, numberNameToNotesArray.get(storedCurrentIncludedNotes));
        setValue(currentIncludedNotes.first);
    }

    @Override
    public String getParameterName() {
        return Words.INCLUDED_NOTES;
    }

    @Override
    public LinkedHashSet<String> getParameterValueNames() {
        return new LinkedHashSet<>(numberNameToNotesArray.keySet());
    }

    @Override
    public void setValue(String value) {
        currentIncludedNotes = new Pair<>(value, numberNameToNotesArray.get(value));
    }

    @Override
    public String getCurrentValueName() {
        return currentIncludedNotes.first;
    }
}