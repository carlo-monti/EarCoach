package org.vosk.earcoach;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class Teacher extends Thread implements VoskListener, SpeechListener, SynthListener {

    private final Vosk vosk;
    private final Speech speech;
    private final Synth synth;
    private final Main main;
    private final Context context;
    private final SingleLock lock;
    private String firstKeyword;
    private Fsm currentState = Fsm.HOME;
    private Exercise currentExercise;
    private Thread fsmThread;
    private boolean isFirstStart = true;

    public Teacher(Main main){
        this.main = main;
        context = main.getApplicationContext();
        // Create lock, recognition, text to speech and midi player objects
        lock = new SingleLock();
        vosk = new Vosk(this.context,this);
        synth = new Synth(this);
        lock.getLock();
        speech = new Speech(this.context,this);
        vosk.pause(true);
        // Initialize Words to create localized strings and alternatives for word recognition
        Words.generateStaticVariablesFromResources(this.getContext());
        Words.initAlternatives();

        String startedExercise = getStringPreferences("STARTED_EXERCISE");
        if(startedExercise.equals("NONE")){
            currentState = Fsm.HOME;
            firstKeyword = Words.HOME;
        }else{
            currentState = Fsm.HOME;
            firstKeyword = startedExercise;
        }
    }

    @Override
    public void run() {
        playEarcon();
        updateFSM(firstKeyword);
    }

    private void updateFSM(String keyword){
        // Whenever a keyword is recognized it is used for the execution of the current state */
        Log.i("VOSKa","NOW UPDATING FSM WITH KEYWORD: " + keyword);
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
        Log.i("VOSKa","CURRENT STATE:" + currentState);
        Log.i("VOSKa","LISTENING FOR:");
        for(String s : keywords){
            Log.i("VOSKa",s);
        }
        // Play the sound and start listening for the next keyword
        playEarcon();
        listen(keywords);
    }

    /*

    Methods for speaking, listen and playing

    */

    private void listen(HashSet<String> keywords){

        // Start the voice recognition bounded to a list of possible keywords

        lock.getLock();
        //main.runOnUiThread(main::isListening);
        Log.i("VOSKa","INIZIO A ASCOLTARE");
        vosk.setAcceptedKeywords(keywords);
        vosk.pause(false);
    }

    public void play(int[][] chordSequence,int duration){

        // Play the chord sequence at a certain speed

        lock.getLock();
        //main.runOnUiThread(main::isPlaying);
        synth.play(chordSequence,duration);
    }

    public void speak(String string){

        // Convert the string to speech

        lock.getLock();
        //main.runOnUiThread(main::isSpeaking);
        speech.speak(string);
    }

    public void playEarcon(){

        // Plays a recognizable sound to identify the starting of listening

        speech.playEarcon();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*

    Methods for speach, synth and vosk listeners/callbacks

    */

    @Override
    public void synthHasEnded() {
        lock.releaseLock();
    }

    @Override
    public void speechHasEnded() {
        lock.releaseLock();
    }

    @Override
    public void onResultFromVosk(String result) {

        // Whenever the recognizer has a result a new thread is started and the fsm is updated

        vosk.pause(true);
        lock.releaseLock();
        fsmThread = new Thread(() -> updateFSM(result));
        fsmThread.start();
    }

    /*

    Methods and "bindings" for handling exercise execution and parameter
    setting to avoid passing context between objects

     */

    public void createNewExercise(ExerciseType exerciseType) {

        try {
            Constructor c = Class.forName(exerciseType.getClassName()).getConstructor(Teacher.class);
            currentExercise = (Exercise) c.newInstance(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
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

    public boolean hasAlreadyGivenAnswer(){ return currentExercise.hasAlreadyGivenAnswer(); }

    public boolean hasAskedQuestion(){ return currentExercise.hasAskedQuestion(); }

    /*

    Methods for handling the activity lifecycle

     */

    public void stopApplication(){
        main.finishAndRemoveTask();
    }

    public void onResume(){
        if(isFirstStart){
            isFirstStart = false;
            Log.i("VOSKa","PRIMO AVVIO teach");
        }else{
            Log.i("VOSKa","ALTREO AVVIO teach");
            fsmThread = new Thread(() -> {
                Log.i("VOSKa","RIAVVIAO");
            });
            fsmThread.start();
        }

    }

    public void onPause(){
        if(fsmThread != null){
            fsmThread.interrupt();
        }
        vosk.pause(true);
        speech.stop();
        synth.stopWhatIsCurrentlyPlaying();
    }

    public void onDestroy(){
        vosk.onDestroy();
    }
}
