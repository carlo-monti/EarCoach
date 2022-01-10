## EarCoach
Voice based ear-training app for Android

**EarCoach** is an ear-training app that can be used without the need to touch the screen: it works just with voice commands. With your voice you can choose between different exercises, answer to the questions and even set some parameters. The app has a hierarchical structure that you can navigate with your commands. Basically you can select an exercise (i.e. "intervals") and than you can start the exercise or set some parameters (i.e. "speed","instrument",...). At every point you can say "info" to have informations about your position and what can you do or where you can move. 

## Documentation
The app code is structured as follows:
- **Main**: is the only activity and its work is just to update the graphical UI. It creates a new Teacher object that handles all the job.
- **Teacher**: is the real "main" class and it uses three objects to interact with the user speaking, playing and listening (Speech, Synth, Vosk). 
- **Speech**: handles the text-to-speech job by using TextToSpeech library and to play an earcon (a simple sound to indicate that the speech has ended and that the listening is starting). It has few self-explanatory methods (speak,stop,playEarcon).
- **Synth**: handles the musical playing part of the job. It uses the MidiDriver library that simply send MIDI commands to the android internal general midi synth.
- **Vosk**: handles the speech-to-text job. It uses the Vosk library and it has been modified to accept only some words that are set with the setAcceptedKeywords() method.

Given that the app is doing only one of these three actions at the same moment, every action is executed on a new thread. Whenever one action is called, it takes a lock and the current thread is forced to wait on the same lock. In this way the code can be written sequentially (i.e. speak(); play(); speak(); listen(); ...) having the following instruction waiting on the lock for the completion of the previous. Teacher implements three listener interfaces, one for every action and whenever an action is completed, a callback function is executed freeing the lock.

The app is implemented as a fsm with four states: HOME, EXERCISE, SETTINGS, PARAMETER (states are represented with the Fsm enum). At every cycle the Teacher evaluates the keyword that has been received as input using the method updateFSM(). This method calls the execute() method on the current state of the fsm and it receives the new state and a list of available keywords. After that the app start listening for the given keywords and so on...

## Create a new exercise
The app is built in such a way that it can be extended with new exercises by simply adding a new class that extends the Exercise abstract class. The new class must override some methods. Every exercise works by asking a question and checking an answer. To interact with the user the exercise can call the methods speak() and play(). The first takes a String as an argument and the second takes an array (of array) in which every item represents the MIDI notes that must be executed at the same moment (i.e. {{60},{},{50,55}} is a sequence of a single note [60], followed by a pause, followed by a chord composed of two notes [50,55]). 

Every exercise can have multiple parameters. Every parameter is represented by a new Class that must implement the ExerciseParameter interface. Every parameter can have multiple values (i.e. the parameter "instrument" can have values such as "piano", "guitar", ...) and every value must have a String that represent it. 

A parameter value can be saved to permanent storage (with SharedPreferences). This is automatically done and to retrieve the stored value the parameter class must have a constructor that takes the reference to the Exercise and call the method exercise.getStoredValueForParameter(this) passing itself as argument. A new exercise looks like this:

```java
public class NewExercise extends Exercise{

    Parameter parameter = new Parameter(this);

    public Interval(Teacher teacher){
        super(teacher);
        addParameter(parameter1);
    }

    @Override
    public void getNewQuestion(){
		
    }

    @Override
    public void askQuestion() {
		speak("guess this");
		play(new int[][]{{60},{40,50}});
    }

    @Override
    public void tellAnswer() {
        speak("this is the answer");
    }

    @Override
    public void checkThisAnswer(String keyword) {
		if(keyword.equals("correct guess")){
			speak("right");
		}else{
			speak("wrong");
		}
    }

    @Override
    public String getExerciseInstructions() {
        return "here are the exercise instructions";
    }

    @Override
    public LinkedHashSet<String> getPossibleAnswer(){
        return new LinkedHashSet<>(Arrays.asList("correct guess","wrong guess"));
    }
}

class Parameter implements ExerciseParameter{

	String currentValue; // can be anything, not only a String

    public Parameter(Exercise owner){
        String storedCurrentValue = owner.getStoredValueForParameter(this);
        if(storedCurrentValue == null){
            storedCurrentValue = "DEFAULT_VALUE";
        }
        setValue(storedCurrentValue);
    }

    @Override
    public String getParameterName() {
        return "parameter";
    }

    @Override
    public LinkedHashSet<String> getParameterValueNames() {
        return new LinkedHashSet<>(Arrays.asList("first value","second value"));
    }

    @Override
    public void setValue(String value) {
		currentValue = value
    }

    @Override
    public String getCurrentValueName() {
        return currentValue;
    }
}
```

After the class has been created, a new value for the enum ExerciseType must be created. It must contain the class name with reference to the package (String) and the name you want the exercise to be called (String).

```java
NEW_EXERCISE("org.vosk.earcoach.NewExercise","Exercise Name"),
```
