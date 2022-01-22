package org.vosk.earcoach;

import android.content.Context;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

/*
This class is used to retrieve words from string.xml file. Every word can be loaded as a static variable
just by creating it and instantiating it inside the method generateStaticVariablesFromResources() that
is called when app is loaded. Alternatively a word can be retrieved at runtime with the get() method
directly from the xml.
It is also possible to insert various alternatives for a given keyword by adding an entry on the
initAlternatives() method. This is used to overcome errors in the speech recognitions.
 */

public class Words {
    private static HashMap<String,String> alternatives;

    public static String INFO;
    public static String SETTINGS;
    public static String YOU_CAN_CHOOSE;
    public static String IS_CURRENTLY_AT;
    public static String BACK_TO_EXERCISE;
    public static String BACK_TO_SETTINGS;
    public static String NEW;
    public static String REPEAT;
    public static String CHOOSE_EXERCISE;
    public static String TUTORIAL;
    public static String PIANO;
    public static String ORGAN;
    public static String ACOUSTIC_GUITAR;
    public static String VIOLIN;
    public static String TRUMPET;
    public static String FLUTE;
    public static String INSTRUMENT;
    public static String END_EXERCISE;
    public static String INTERVALS;
    public static String WELCOME;
    public static String MELODIC;
    public static String HARMONIC;
    public static String INTERVAL_TYPE;
    public static String CORRECT_ANSWER;
    public static String WRONG_ANSWER;
    public static String UNISON;
    public static String MINOR_SECOND;
    public static String MAJOR_SECOND;
    public static String MINOR_THIRD;
    public static String MAJOR_THIRD;
    public static String FOURTH;
    public static String AUGMENTED_FOURTH;
    public static String FIFTH;
    public static String MINOR_SIXTH;
    public static String MAJOR_SIXTH;
    public static String MINOR_SEVENTH;
    public static String MAJOR_SEVENTH;
    public static String OCTAVE;
    public static String MINOR_NINTH;
    public static String MAJOR_NINTH;
    public static String DIRECTION;
    public static String ASCENDING;
    public static String DESCENDING;
    public static String BOTH;
    public static String SECONDS;
    public static String THIRDS;
    public static String FOURTH_TO_FIFTH;
    public static String SIXTHS;
    public static String SEVENTHS;
    public static String NINTHS;
    public static String UP_TO_THIRDS;
    public static String UP_TO_FOURTH;
    public static String UP_TO_FIFTH;
    public static String UP_TO_SIXTHS;
    public static String UP_TO_SEVENTHS;
    public static String UP_TO_NINTHS;
    public static String UP_TO_OCTAVE;
    public static String RANGE;
    public static String CLOSE_APP;
    public static String GOODBYE;
    public static String OR;
    public static String STARTING_EXERCISE;
    public static String ONE;
    public static String TWO;
    public static String THREE;
    public static String FOUR;
    public static String FIVE;
    public static String SPEED;
    public static String HOME;
    public static String SING;
    public static String NEW_QUESTION;
    public static String ANSWER;
    public static String TO_START;
    public static String MELODY;
    public static String SIX;
    public static String ROOT;
    public static String SECOND;
    public static String THIRD;
    public static String SEVENTH;
    public static String SIXTH;
    public static String NINTH;
    public static String LENGTH;
    public static String INCLUDED_NOTES;
    public static String RESUME;
    public static String RESUME_FROM;
    public static String WELCOME_AGAIN;
    public static String MELODY_INSTRUCTIONS;
    public static String INTERVAL_INSTRUCTIONS;
    public static String INTERVAL_SING_INSTRUCTIONS;
    public static String YOU_ARE_AT;
    public static String MODIFY_PARAMETER;
    public static String CHOOSE_VALUE_FOR_PARAMETER;
    public static String CHOOSE_PARAMETER_TO_CHANGE;
    public static String AMONG_EXERCISES;
    public static String I_M_BACK_TO_EXERCISE;

    static void generateStaticVariablesFromResources(Context context){
        INFO = context.getString(R.string.INFO);
        SETTINGS = context.getString(R.string.SETTINGS);
        YOU_CAN_CHOOSE = context.getString(R.string.YOU_CAN_CHOOSE);
        IS_CURRENTLY_AT = context.getString(R.string.IS_CURRENTLY_AT);
        BACK_TO_EXERCISE = context.getString(R.string.BACK_TO_EXERCISE);
        BACK_TO_SETTINGS = context.getString(R.string.BACK_TO_SETTINGS);
        NEW = context.getString(R.string.NEW);
        REPEAT = context.getString(R.string.REPEAT);
        CHOOSE_EXERCISE = context.getString(R.string.CHOOSE_EXERCISE);
        TUTORIAL = context.getString(R.string.TUTORIAL);
        PIANO = context.getString(R.string.PIANO);
        TRUMPET = context.getString(R.string.TRUMPET);
        VIOLIN = context.getString(R.string.VIOLIN);
        ORGAN = context.getString(R.string.ORGAN);
        ACOUSTIC_GUITAR = context.getString(R.string.ACOUSTIC_GUITAR);
        FLUTE = context.getString(R.string.FLUTE);
        INSTRUMENT = context.getString(R.string.INSTRUMENT);
        END_EXERCISE = context.getString(R.string.END_EXERCISE);
        INTERVALS = context.getString(R.string.INTERVALS);
        WELCOME = context.getString(R.string.WELCOME);
        MELODIC = context.getString(R.string.MELODIC);
        HARMONIC = context.getString(R.string.HARMONIC);
        INTERVAL_TYPE = context.getString(R.string.INTERVAL_TYPE);
        CORRECT_ANSWER = context.getString(R.string.CORRECT_ANSWER);
        UNISON = context.getString(R.string.UNISON);
        MINOR_SECOND = context.getString(R.string.MINOR_SECOND);
        MAJOR_SECOND = context.getString(R.string.MAJOR_SECOND);
        MINOR_THIRD = context.getString(R.string.MINOR_THIRD);
        MAJOR_THIRD = context.getString(R.string.MAJOR_THIRD);
        FOURTH = context.getString(R.string.FOURTH);
        AUGMENTED_FOURTH = context.getString(R.string.AUGMENTED_FOURTH);
        FIFTH = context.getString(R.string.FIFTH);
        MINOR_SIXTH = context.getString(R.string.MINOR_SIXTH);
        MAJOR_SIXTH = context.getString(R.string.MAJOR_SIXTH);
        MINOR_SEVENTH = context.getString(R.string.MINOR_SEVENTH);
        MAJOR_SEVENTH = context.getString(R.string.MAJOR_SEVENTH);
        OCTAVE = context.getString(R.string.OCTAVE);
        MINOR_NINTH = context.getString(R.string.MINOR_NINTH);
        MAJOR_NINTH = context.getString(R.string.MAJOR_NINTH);
        DIRECTION = context.getString(R.string.DIRECTION);
        ASCENDING = context.getString(R.string.ASCENDING);
        DESCENDING = context.getString(R.string.DESCENDING);
        BOTH = context.getString(R.string.BOTH);
        SECONDS = context.getString(R.string.SECONDS);
        THIRDS = context.getString(R.string.THIRDS);
        FOURTH_TO_FIFTH = context.getString(R.string.FOURTH_TO_FIFTH);
        SIXTHS = context.getString(R.string.SIXTHS);
        SEVENTHS = context.getString(R.string.SEVENTHS);
        NINTHS = context.getString(R.string.NINTHS);
        UP_TO_THIRDS = context.getString(R.string.UP_TO_THIRDS);
        UP_TO_FOURTH = context.getString(R.string.UP_TO_FOURTH);
        UP_TO_FIFTH = context.getString(R.string.UP_TO_FIFTH);
        UP_TO_SIXTHS = context.getString(R.string.UP_TO_SIXTHS);
        UP_TO_SEVENTHS = context.getString(R.string.UP_TO_SEVENTHS);
        UP_TO_NINTHS = context.getString(R.string.UP_TO_NINTHS);
        UP_TO_OCTAVE = context.getString(R.string.UP_TO_OCTAVE);
        RANGE = context.getString(R.string.RANGE);
        WRONG_ANSWER = context.getString(R.string.WRONG_ANSWER);
        CORRECT_ANSWER = context.getString(R.string.CORRECT_ANSWER);
        CLOSE_APP = context.getString(R.string.CLOSE_APP);
        GOODBYE = context.getString(R.string.GOODBYE);
        OR = context.getString(R.string.OR);
        STARTING_EXERCISE = context.getString(R.string.STARTING_EXERCISE);
        ONE = context.getString(R.string.ONE);
        TWO = context.getString(R.string.TWO);
        THREE = context.getString(R.string.THREE);
        FOUR = context.getString(R.string.FOUR);
        FIVE = context.getString(R.string.FIVE);
        SPEED = context.getString(R.string.SPEED);
        HOME = context.getString(R.string.HOME);
        MELODY = context.getString(R.string.MELODY);
        SING = context.getString(R.string.SING);
        NEW_QUESTION = context.getString(R.string.NEW_QUESTION);
        ANSWER = context.getString(R.string.ANSWER);
        TO_START = context.getString(R.string.TO_START);
        SIX = context.getString(R.string.SIX);
        ROOT = context.getString(R.string.ROOT);
        SECOND = context.getString(R.string.SECOND);
        THIRD = context.getString(R.string.THIRD);
        SIXTH = context.getString(R.string.SIXTH);
        SEVENTH = context.getString(R.string.SEVENTH);
        NINTH = context.getString(R.string.NINTH);
        LENGTH = context.getString(R.string.LENGTH);
        INCLUDED_NOTES = context.getString(R.string.INCLUDED_NOTES);
        RESUME = "RESUME";
        RESUME_FROM = context.getString(R.string.RESUME_FROM);
        WELCOME_AGAIN = context.getString(R.string.WELCOME_AGAIN);
        INTERVAL_INSTRUCTIONS = context.getString(R.string.INTERVAL_INSTRUCTION);
        INTERVAL_SING_INSTRUCTIONS = context.getString(R.string.INTERVAL_SING_INSTRUCTION);
        MELODY_INSTRUCTIONS = context.getString(R.string.MELODY_INSTRUCTION);
        YOU_ARE_AT = context.getString(R.string.YOU_ARE_AT);
        MODIFY_PARAMETER = context.getString(R.string.MODIFY_PARAMETER);
        CHOOSE_VALUE_FOR_PARAMETER = context.getString(R.string.CHOOSE_VALUE_FOR_PARAMETER);
        CHOOSE_PARAMETER_TO_CHANGE = context.getString(R.string.CHOOSE_PARAMETER_TO_CHANGE);
        AMONG_EXERCISES = context.getString(R.string.AMONG_EXERCISES);
        I_M_BACK_TO_EXERCISE = context.getString(R.string.I_M_BACK_TO_EXERCISE);
    }

    static String get(int stringID, Context context){
        return context.getResources().getString(stringID);
    }

    static void initAlternatives(){
        Log.i("VOSKa", Locale.getDefault().getCountry());
        alternatives = new HashMap<>();
        alternatives.put("nonna minore",MINOR_NINTH);
        alternatives.put("non a minore",MINOR_NINTH);
        alternatives.put("non minore",MINOR_NINTH);
        alternatives.put("nonna maggiore",MAJOR_NINTH);
        alternatives.put("non a maggiore",MAJOR_NINTH);
        alternatives.put("non maggiore",MAJOR_NINTH);
        alternatives.put("torna all'esercizio",BACK_TO_EXERCISE);
        alternatives.put("più di applicazione",CLOSE_APP);
        alternatives.put("chiude applicazione",CLOSE_APP);
        alternatives.put("tu di applicazione",CLOSE_APP);
        alternatives.put("me lo dico",MELODIC);
        alternatives.put("risposto",ANSWER);
        alternatives.put("muovo",NEW);
        alternatives.put("me lo dia",MELODY);
        alternatives.put("tonico",ROOT);
        alternatives.put("notte incluse",INCLUDED_NOTES);
        alternatives.put("fino a l'ottava",UP_TO_OCTAVE);
        alternatives.put("fino al l'ottava",UP_TO_OCTAVE);
        alternatives.put("fino alle nonni",UP_TO_NINTHS);
        alternatives.put("fino alle non è",UP_TO_NINTHS);
        alternatives.put("fino alle feste",UP_TO_SIXTHS);
        alternatives.put("fino alle settimane",UP_TO_SEVENTHS);
        alternatives.put("fino alle sette me",UP_TO_SEVENTHS);
        alternatives.put("numero di notte",LENGTH);
        alternatives.put("fine dell'esercizio",END_EXERCISE);
    }

    static HashSet<String> getAlternativesFromKeyword(String keyword){
        HashSet<String> result = new HashSet<>();
        if(alternatives.containsValue(keyword)){
            for (Map.Entry<String, String> a : alternatives.entrySet()) {
                if(a.getValue().equals(keyword)) {
                    result.add(a.getKey());
                }
            }
        }
        return result;
    }

    static String getKeywordFromAlternatives(String alternative){
        String keyword;
        if(alternatives.containsKey(alternative)){
            keyword = alternatives.get(alternative);
        }else{
            keyword = alternative;
        }
        return keyword;
    }
}
