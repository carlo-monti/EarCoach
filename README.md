# EarCoach
Voice based ear-training app for Android

## About

**EarCoach** is an ear-training app that can be used without the need to touch the screen: it works just with voice commands. With your voice you can choose between different exercises, answer to the questions and even edit some settings. The app has a basical hierarchical structure (HOME->EXERCISE->SETTINGS) that you can navigate with your commands. You start at "home", than you can select an exercise (i.e. "intervals") and then you can start the exercise or set some parameters (i.e. "speed","instrument",...). At every point you can say "info" to have informations about your position and what can you do or where you can move.

## Install

Download the folder, open it in Android Studio and start it (remember to enable the microphone in the emulator).

## Documentation
The app code is structured as follows:
- **Main**: is the only activity and its work is mainly to update the graphical UI. It creates a new Teacher object that handles all the job.
- **Teacher**: is the real "main" class and it uses three objects to interact with the user speaking, playing and listening (Speech, Synth, Vosk).
- **Speech**: handles the text-to-speech job by using TextToSpeech library and to play an earcon (a simple sound to indicate that the speech has ended and that the listening is starting). It has few self-explanatory methods (speak,stop,playEarcon).
- **Synth**: handles the musical part of the job. It uses the [MidiDriver library](https://github.com/billthefarmer/mididriver) that simply send MIDI commands to the android internal general midi synth.
- **Vosk**: handles the speech-to-text job. It uses the [Vosk library](https://alphacephei.com/vosk/) and it has been modified to accept only some words that are set with the *setAcceptedKeywords* method.

The app is doing only one of these three actions at the same moment (i.e. they act sequentially). To handle this, each action is executed on a new thread. Whenever one action is called, it takes a lock and the current thread is forced to wait on the same lock. In this way the code can be written sequentially (i.e. speak(); play(); speak(); listen(); ...) having the following instruction waiting on the lock for the completion of the previous. Teacher implements three listener interfaces, one for every action and whenever an action is completed, a callback function is executed leaving the lock.

The app is implemented as a fsm with four states: HOME, EXERCISE, SETTINGS, PARAMETER and the states are represented with the Fsm enum. At every cycle the Teacher evaluates the keyword that has been received as input using the method *updateFSM*. This method calls the *execute* method on the current state of the fsm and it receives the new state and a list of available keywords. After that the app start listening for the given keywords and so on...

### Create a new exercise
The app is built in such a way that it can be extended with new exercises by simply adding a new class that extends the Exercise abstract class. Every exercise works by asking a question and checking an answer. The new class must override some methods:

- **getNewQuestion**: creates a new question inside the exercise
- **askCurrentQuestion**: ask the question to the user speaking and playing something
- **tellAnswer**: reveal to the user the correct answer
- **checkThisAnswer**: checks whether the received answer is correct and tells it to the user
- **tellExerciseInstructions**: tells the user about how the exercise works
- **getPossibleAnswers**: returns the accepted answers (i.e. "major second", "major third", etc.)

To interact with the user the exercise can call the methods *speak* and *play*. The first takes a String as an argument and the second takes an array (of array) in which every item represents the MIDI notes that must be executed at the same moment. For example, the structure:

```java
{{60},{},{50,55}}
```
is a sequence of a single note (60), followed by a pause, followed by a chord composed of two notes (50,55).

### Parameters

Every exercise can have multiple parameters. Every parameter is represented by a new Class that must implement the ExerciseParameter interface. Every parameter can have multiple values (i.e. the parameter "instrument" can have values such as "piano", "guitar", ...) and every value must have a String that represents it allowing the user to select it by voice. There are two "built-in" parameters that are available for every exercise: "Instrument" and "Speed".

Inside the parameter class you can handle the values anyway you like it. I found it useful to use a LinkedHashMap to convert the String to the value needed (i.e. String "ONE" to Int value) and a Pair to store the current value (i.e. as a pair of String and Int value).

A parameter value can be saved to permanent storage (with SharedPreferences). This is automatically done within the Exercise abstract class. To retrieve the stored value, the parameter class must have a constructor that takes a reference to the Exercise owner and call the method *owner.getStoredValueForParameter(this)* passing itself as argument.

A parameter Class must be added to the exercise class with the method addParameter().

A new exercise looks like this:

```java
public class NewExercise extends Exercise{

    Parameter parameter = new Parameter(this);

    public Interval(Teacher teacher){
        super(teacher);
        addParameter(parameter);
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
	currentValue = value;
    }

    @Override
    public String getCurrentValueName() {
        return currentValue;
    }
}
```

After the class has been created, a new value for the enum ExerciseType must be added. It must contain the class name with reference to the package (a String) and the name you want the exercise to be called (a String).

```java
public enum ExerciseType {
    INTERVALS("org.vosk.earcoach.Interval",Words.INTERVALS),
    MELODY("org.vosk.earcoach.Melody",Words.MELODY);
    // Insert the new exercise class here
}
```

### Locale
Every accepted word is taken by the string.xml file, so the app can be localizable with ease. To make the access faster, every string is converted to a static string variable within the Words object with the static method *generateStaticVariablesFromResource* that is called at runtime every time the app is launched. To insert a new string:
- Insert the string into string.xml
- Create a constant into Words
- Instantiate the variable with the value taken from string.xml with context.getString().
- To use it just call Words.NEW_STRING

This obj can also be used to retrieve the string at runtime using the static method *get* with the reference (es. *R.string.DESIDERED_STRING*) as parameter.

It is also possible to add several alternatives for a given keyword by putting a new entry within the initAlternatives method, indicating the alternative string and the keyword it belongs. This is useful to handle misrecognitions in the speech-to-text action.

## To do
Currently the app has to keep the screen on to avoid shutting down the internal synth. This has the disadvantage of battery consumption. The app should be modified easily to work in background by using Wake locks. The problem is that there is no documentation about how to use the internal synth and how avoid its shutdown!
