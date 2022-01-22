package org.vosk.earcoach;

import android.util.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

/*
Every entry in this enum represents a state for the finite state machine. Every state implements two abstract methods:

getPossibleKeywords(): returns the allowed keywords for the current state (i.e. the words that the recognizer accepts).
    The set of the possible keywords contains general keywords as INFO, CLOSE_APP, etc. and specific
    keywords for the current exercise or current parameter values.

executeKeyword(): is the main method for the state. Given a keyword, the method chooses what has to be done.
    For example: if the keyword is INFO, the method tells the user about what he can do given the current
    position. Or if the keyword is an answer to a question, the method ask the exercise to check the answer.
    The method then returns the next state.

The main method execute() takes a keyword as input, checks for keywords alternatives and then calls
the executeKeyword method on the current state. It returns the next state and a list of available
keywords to listen to.
 */

public enum Fsm {
    HOME {
        @Override
        LinkedHashSet<String> getPossibleKeywords(Teacher teacher){
            // adds general keywords
            LinkedHashSet<String> result = new LinkedHashSet<>(Arrays.asList(Words.INFO, Words.CLOSE_APP));
            // adds the keywords for all exercise class
            for(ExerciseType e : ExerciseType.values()){
                result.add(e.toString());
            }
            return result;
        }
        @Override
        Fsm executeKeyword(String keyword, Teacher teacher){
            Fsm nextState = this;
            // checks whether the user asked to start an exercise and starts it
            for(ExerciseType e : ExerciseType.values()){
                if(keyword.equals(e.toString())){
                    if(teacher.getBooleanPreferences(EarCoach.IS_A_RESTART)){
                        teacher.speak(Words.WELCOME_AGAIN);
                        teacher.speak(Words.RESUME_FROM + " " + e.toString());
                        teacher.setPreferences(EarCoach.IS_A_RESTART,false);
                    }else{
                        teacher.speak(Words.STARTING_EXERCISE + " " + e.toString());
                    }
                    teacher.createNewExercise(e);
                    teacher.setPreferences(EarCoach.STARTED_EXERCISE,e.toString());
                    nextState = Fsm.EXERCISE;
                    return nextState;
                }
            }
            if (keyword.equals(Words.INFO)) {
                teacher.speak(Words.YOU_CAN_CHOOSE + " " + Words.AMONG_EXERCISES);
                for(ExerciseType e : ExerciseType.values()){
                    teacher.speak(e.toString());
                }
                teacher.speak(Words.OR + " " + Words.YOU_CAN_CHOOSE);
                teacher.speak(Words.CLOSE_APP);
            } else if (keyword.equals(Words.CLOSE_APP)) {
                teacher.speak(Words.GOODBYE);
                teacher.stopApplication();
            } else {
                if (teacher.getBooleanPreferences(EarCoach.WELCOME_AGAIN)) {
                    teacher.speak(Words.WELCOME_AGAIN);
                }else{
                    teacher.speak(Words.WELCOME);
                    if(!teacher.getBooleanPreferences(EarCoach.HAS_ALREADY_BEEN_STARTED)){
                        teacher.speak(Words.get(R.string.SAY_INFO, teacher.getContext()));
                        teacher.setPreferences(EarCoach.HAS_ALREADY_BEEN_STARTED,true);
                    }
                    teacher.setPreferences(EarCoach.WELCOME_AGAIN,true);
                }
            }
            return nextState;
        }
    },
    EXERCISE {
        LinkedHashSet<String> possibleAnswersOfExercise;
        @Override
        LinkedHashSet<String> getPossibleKeywords(Teacher teacher){
            // adds general keywords
            LinkedHashSet<String> possibleKeywords = new LinkedHashSet<>(Arrays.asList(
                    Words.NEW,Words.END_EXERCISE,Words.INFO,Words.REPEAT,Words.TUTORIAL,Words.SETTINGS, Words.CLOSE_APP));
            // adds REPEAT keyword only if the exercise has already been started
            if(teacher.hasAskedQuestion()){
                possibleKeywords.add(Words.REPEAT);
            }
            // adds keywords (answers) for the current exercise
            if(possibleAnswersOfExercise != null){
                possibleKeywords.addAll(possibleAnswersOfExercise);
            }
            return possibleKeywords;
        }
        @Override
        Fsm executeKeyword(String keyword, Teacher teacher){
            Fsm nextState = this;
            if (Words.TUTORIAL.equals(keyword)) {
                String instructions = teacher.getExerciseInfo();
                if(instructions != null){
                    teacher.speak(instructions);
                }
            } else if (keyword.equals(Words.INFO)) {
                // checks if the exercise has been started
                if(teacher.hasAskedQuestion()){
                    teacher.speak(Words.YOU_CAN_CHOOSE);
                    teacher.speak(Words.REPEAT);
                }else{
                    teacher.speak(Words.YOU_CAN_CHOOSE);
                    teacher.speak(Words.NEW);
                    teacher.speak(Words.TO_START);
                }
                teacher.speak(Words.OR + " " + Words.YOU_CAN_CHOOSE);
                teacher.speak(Words.END_EXERCISE);
                teacher.speak(Words.SETTINGS);
                teacher.speak(Words.TUTORIAL);
                teacher.speak(Words.CLOSE_APP);
            } else if (Words.NEW.equals(keyword)) {
                possibleAnswersOfExercise = teacher.getNewQuestion();
                teacher.speak(Words.NEW_QUESTION);
                teacher.askQuestion();
            } else if (keyword.equals(Words.CLOSE_APP)) {
                teacher.speak(Words.GOODBYE);
                teacher.stopApplication();
            } else if (Words.REPEAT.equals(keyword)) {
                if(teacher.hasAlreadyGivenAnswer()){
                    teacher.tellAnswer();
                }else{
                    if(!teacher.hasAskedQuestion()){
                        teacher.speak(Words.NEW_QUESTION);
                        teacher.getNewQuestion();
                    }
                    teacher.askQuestion();
                }
            } else if (Words.END_EXERCISE.equals(keyword)) {
                teacher.speak(Words.END_EXERCISE);
                nextState = Fsm.HOME;
            } else if (Words.SETTINGS.equals(keyword)) {
                teacher.speak(Words.CHOOSE_PARAMETER_TO_CHANGE);
                nextState = Fsm.SETTINGS;
            } else {
                teacher.checkAnswer(keyword);
            }
            return nextState;
        }
    },
    SETTINGS{
        LinkedHashSet<String> parameterList;
        @Override
        LinkedHashSet<String> getPossibleKeywords(Teacher teacher){
            // adds general keywords
            LinkedHashSet<String> possibleKeywords = new LinkedHashSet<>(Arrays.asList(Words.INFO,Words.BACK_TO_EXERCISE,Words.CLOSE_APP));
            // adds keywords for the parameters
            parameterList = teacher.getExerciseParameters();
            if(parameterList != null){
                possibleKeywords.addAll(parameterList);
            }
            return possibleKeywords;
        }
        @Override
        Fsm executeKeyword(String keyword, Teacher teacher){
            Fsm nextState = this;
            if (keyword.equals(Words.INFO)) {
                teacher.speak(Words.YOU_CAN_CHOOSE);
                // tells the user about which parameter can be selected
                for (String s : parameterList) {
                    teacher.speak(s);
                }
                teacher.speak(Words.OR + " " + Words.YOU_CAN_CHOOSE);
                teacher.speak(Words.BACK_TO_EXERCISE);
                teacher.speak(Words.CLOSE_APP);
            } else if (Words.BACK_TO_EXERCISE.equals(keyword)) {
                teacher.speak(Words.I_M_BACK_TO_EXERCISE);
                nextState = Fsm.EXERCISE;
            } else if (keyword.equals(Words.CLOSE_APP)) {
                teacher.speak(Words.GOODBYE);
                teacher.stopApplication();
            } else {
                teacher.setSelectedParameter(keyword);
                teacher.speak(Words.CHOOSE_VALUE_FOR_PARAMETER + " " + keyword);
                nextState = Fsm.PARAMETER_CHANGE;
            }
            return nextState;
        }
    },
    PARAMETER_CHANGE{
        HashSet<String> possibleSettingsKeywords;
        @Override
        LinkedHashSet<String> getPossibleKeywords(Teacher teacher){
            // adds general keywords
            LinkedHashSet<String> possibleKeywords = new LinkedHashSet<>(Arrays.asList(Words.INFO,Words.BACK_TO_EXERCISE,Words.BACK_TO_SETTINGS,Words.CLOSE_APP));
            // adds keywords for the parameter values
            possibleSettingsKeywords = teacher.getParametersValue();
            if(possibleSettingsKeywords != null){
                possibleKeywords.addAll(possibleSettingsKeywords);
            }
            return possibleKeywords;
        }
        @Override
        Fsm executeKeyword(String keyword, Teacher teacher){
            Fsm nextState = this;
            if (keyword.equals(Words.INFO)) {
                teacher.speak(teacher.getSelectedParameter() +  " " + Words.IS_CURRENTLY_AT +  " " + teacher.getSelectedValue());
                teacher.speak(Words.YOU_CAN_CHOOSE);
                // tells the user about which parameter values can be selected
                for(String s : possibleSettingsKeywords){
                    teacher.speak(s);
                }
                teacher.speak(Words.OR + " " + Words.YOU_CAN_CHOOSE);
                teacher.speak(Words.BACK_TO_EXERCISE);
                teacher.speak(Words.BACK_TO_SETTINGS);
                teacher.speak(Words.CLOSE_APP);
            } else if (Words.BACK_TO_EXERCISE.equals(keyword)) {
                nextState = Fsm.EXERCISE;
            } else if (Words.BACK_TO_SETTINGS.equals(keyword)) {
                nextState = Fsm.SETTINGS;
            } else if (keyword.equals(Words.CLOSE_APP)) {
                teacher.speak(Words.GOODBYE);
                teacher.stopApplication();
            } else {
                teacher.setSelectedValue(keyword);
                teacher.speak(teacher.getSelectedParameter() +  " " + Words.IS_CURRENTLY_AT +  " " + teacher.getSelectedValue());
                nextState = Fsm.SETTINGS;
            }
            return nextState;
        }
    };

    Pair<Fsm, LinkedHashSet<String>> execute(String keyword, Teacher teacher){
        // This method execute the keyword on the current state and returns the next state
        // and the available keywords
        keyword = Words.getKeywordFromAlternatives(keyword);
        Fsm nextState = this.executeKeyword(keyword.toLowerCase(), teacher);
        LinkedHashSet<String> possibleKeywords = nextState.getPossibleKeywords(teacher);
        return new Pair<>(nextState, possibleKeywords);
    }

    abstract Fsm executeKeyword(String keyword, Teacher teacher);
    abstract LinkedHashSet<String> getPossibleKeywords(Teacher teacher);
}
