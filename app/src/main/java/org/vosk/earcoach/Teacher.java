package org.vosk.earcoach;

import android.content.Context;
import android.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashSet;

/*
This class is the heart of the application. It handles all the communications between the synth, the
tts engine, the voice recognizer and the exercises. Its working process alternates between the listening
and the action. Basically the app listen to a set of keyword and then execute it. To listen, the method
listen (from the Vosk obj) is called with a set of possible keywords. Whenever a keyword is identified, the
Vosk obj callsback on the listener onResultFromVosk() calling the updateFSM() method. This method execute the given action
for the current state and put the app back on listen. And so on...

There are three different object that are used to interact with the user: Vosk (the voice recognizer),
Synth and Speech. They are always used sequentially and their separation is guaranteed by the Lock obj.
Basically an obj method is called on a different Thread, it takes the Lock and the next instruction on
the current Thread is put on wait until the first Thread ends its action and callsback freeing the Lock.
 */

public class Teacher extends Thread implements VoskListener, SpeechListener, SynthListener {

    private final Vosk vosk;
    private final Speech speech;
    private final Synth synth;
    private final Main main;
    private final Context context;
    private final SingleLock lock;
    private String firstKeyword;
    private Fsm currentState;
    private Exercise currentExercise;
    private Thread fsmThread;

    public Teacher(Main main){
        this.main = main;
        context = main.getApplicationContext();
        // Create lock, recognition, text to speech and midi player objects
        lock = new SingleLock();
        vosk = new Vosk(this.context,this);
        vosk.pause(true);
        synth = new Synth(this);
        lock.getLock();
        speech = new Speech(this.context,this);
        // Initialize Words to create localized strings and alternatives for word recognition
        Words.generateStaticVariablesFromResources(this.getContext());
        Words.initAlternatives();
        // Loads the last started exercise (if any) during the current app execution
        String startedExercise = getStringPreferences(EarCoach.STARTED_EXERCISE);
        if(startedExercise.equals(EarCoach.NONE)){
            currentState = Fsm.HOME;
            firstKeyword = Words.HOME;
            setPreferences(EarCoach.IS_A_RESTART,false);
        }else{
            currentState = Fsm.HOME;
            firstKeyword = startedExercise;
        }
    }

    @Override
    public void run() {
        updateFSM(firstKeyword);
    }

    private void updateFSM(String keyword){
        // The keyword is used for the execution of the current state
        Pair<Fsm, LinkedHashSet<String>> nextState_availableAnswers = currentState.execute(keyword,this);
        // After the execution of the current state with the keyword the fsm returns the next state
        // and a list of the expected keywords to be recognized
        currentState = nextState_availableAnswers.first;
        LinkedHashSet<String> keywords = nextState_availableAnswers.second;
        HashSet<String> alternativeKeywords = new HashSet<>();
        for(String k : keywords){
            alternativeKeywords.addAll(Words.getAlternativesFromKeyword(k));
        }
        keywords.addAll(alternativeKeywords);
        // Play the sound and start listening for the next keyword
        playEarcon();
        listen(keywords);
    }

// Methods for speaking, listen and playing

    private void listen(HashSet<String> keywords){
        // Start the voice recognition bounded to a list of possible keywords
        lock.getLock();
        main.runOnUiThread(main::isListening);
        vosk.setAcceptedKeywords(keywords);
        vosk.pause(false);
    }

    public void play(int[][] chordSequence,int duration){
        // Play the chord sequence at a certain speed
        lock.getLock();
        main.runOnUiThread(main::isPlaying);
        synth.play(chordSequence,duration);
    }

    public void speak(String string){
        // Convert the string to speech
        lock.getLock();
        main.runOnUiThread(main::isSpeaking);
        speech.speak(string);
    }

    public void playEarcon(){
        // Plays a recognizable sound to identify the starting of listening
        lock.getLock();
        speech.playEarcon();
    }

// Methods for speach, synth and vosk listeners/callbacks

    @Override
    public void synthHasEnded() {
        lock.releaseLock();
    }

    @Override
    public void speechHasEnded() {
        lock.releaseLock();
    }

    @Override
    public void onSpeechError(String message) {
        main.onError(message);
    }

    @Override
    public void onResultFromVosk(String result) {
        // Whenever the recognizer has a result a new thread is started and the fsm is updated
        // and it is printed on the UI
        vosk.pause(true);
        lock.releaseLock();
        fsmThread = new Thread(() -> updateFSM(result));
        fsmThread.start();
    }

    @Override
    public void onUnidentifiedResultFromVosk(String result) {
        // Whenever the recognizer has a result that is not identified as a keyword
        // it is printed on the UI
        fsmThread = new Thread(() -> updateFSM(result));
    }

    @Override
    public void onVoskError(String message){
        main.onError(message);
    }

// Methods and "bindings" for handling exercise execution and parameter
// setting to avoid passing context between objects

    public void createNewExercise(ExerciseType exerciseType) {

        try {
            Constructor c = Class.forName(exerciseType.getClassName()).getConstructor(Teacher.class);
            currentExercise = (Exercise) c.newInstance(this);
        } catch (IllegalAccessException e) {
            main.onError("Illegal Access: " + e.getMessage());
        } catch (InstantiationException e) {
            main.onError("Instantiation: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            main.onError("Class Not Found: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            main.onError("No such method: " + e.getMessage());
        } catch (InvocationTargetException e) {
            main.onError("Invocation Target: " + e.getMessage());
        }
    }

    public LinkedHashSet<String> getNewQuestion(){
        // Start a new question for the selected exercise
        LinkedHashSet<String> possibleAnswers;
        currentExercise.newQuestion();
        possibleAnswers = currentExercise.getPossibleAnswer();
        return possibleAnswers;
    }

    public void askQuestion(){
        currentExercise.askQuestion();
    }

    public void checkAnswer(String keyword){
        currentExercise.checkAnswer(keyword);
    }

    public void tellAnswer(){
        currentExercise.tellAnswer();
    }

    public String getExerciseInfo(){
        return currentExercise.getExerciseInstructions();
    }

    public LinkedHashSet<String> getExerciseParameters(){ return currentExercise.getExerciseParameters(); }

    public LinkedHashSet<String> getParametersValue(){ return currentExercise.getParameterValues(); }

    public String getSelectedParameter(){ return currentExercise.getSelectedParameter(); }

    public void setSelectedParameter(String parameter){ currentExercise.setSelectedParameter(parameter); }

    public String getSelectedValue(){
        return currentExercise.getSelectedValue();
    }

    public void setSynthInstrument(int value){
        synth.changeInstrument(value);
    }

    public Context getContext(){
        return context;
    }

    public void setSelectedValue(String value){
        currentExercise.setSelectedValue(value);
    }

    public void setPreferences(String key, String value){
        main.savePreferences(key, value);
    }

    public String getStringPreferences(String key){
        return main.getStringPreferences(key);
    }

    public void setPreferences(String key, boolean value){ main.savePreferences(key, value); }

    public boolean getBooleanPreferences(String key){ return main.getBooleanPreferences(key); }

    public boolean hasAlreadyGivenAnswer(){ return currentExercise.hasAlreadyGivenAnswer(); }

    public boolean hasAskedQuestion(){ return currentExercise.hasAskedQuestion(); }

    // Methods for handling the activity lifecycle

    public void stopApplication(){
        lock.getLock();
        if(fsmThread != null){
            fsmThread.interrupt();
        }
        setPreferences(EarCoach.IS_A_RESTART,false);
        setPreferences(EarCoach.STARTED_EXERCISE,EarCoach.NONE);
        vosk.pause(true);
        speech.stop();
        synth.stopWhatIsCurrentlyPlaying();
        synth.stopSynth();
        main.finishAndRemoveTask();
    }

    public void onPause(){
        if(fsmThread != null){
            fsmThread.interrupt();
        }
        setPreferences(EarCoach.IS_A_RESTART,true);
        vosk.pause(true);
        speech.stop();
        synth.stopWhatIsCurrentlyPlaying();
        synth.stopSynth();
        vosk.quitVosk();
    }
}
