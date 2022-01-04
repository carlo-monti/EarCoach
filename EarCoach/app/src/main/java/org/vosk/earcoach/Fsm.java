package org.vosk.earcoach;

import android.util.Log;
import android.util.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public enum Fsm {
    HOME {
        @Override
        LinkedHashSet<String> getPossibleKeywords(Teacher teacher){
            LinkedHashSet<String> result = new LinkedHashSet(Arrays.asList(Words.INFO, Words.CLOSE_APP));
            for(ExerciseType e : ExerciseType.values()){
                result.add(e.toString());
            }
            return result;
        }
        @Override
        Fsm executeKeyword(String keyword, Teacher teacher){
            Fsm nextState = this;
            for(ExerciseType e : ExerciseType.values()){
                if(keyword.equals(e.toString())){
                    teacher.speak(Words.STARTING_EXERCISE + " " + e.toString());
                    teacher.createNewExercise(e);
                    teacher.setPreferences("STARTED_EXERCISE",e.toString());
                    nextState = Fsm.EXERCISE;
                    return nextState;
                }
            }
            if (keyword.equals(Words.INFO)) {
                teacher.speak(Words.YOU_CAN_CHOOSE);
                for(ExerciseType e : ExerciseType.values()){
                    teacher.speak(e.toString());
                }
                teacher.speak(Words.OR + " " + Words.YOU_CAN_CHOOSE);
                teacher.speak(Words.CLOSE_APP);
            } else if (Words.RESUME.equalsIgnoreCase(keyword)) {
            } else if (keyword.equals(Words.CLOSE_APP)) {
                teacher.speak(Words.GOODBYE);
                teacher.stopApplication();
            } else {
                Log.i("VOSKa",keyword);

                teacher.speak(Words.WELCOME);
            }
            return nextState;
        }
    },
    EXERCISE {
        LinkedHashSet<String> possibleAnswersOfExercise;
        @Override
        LinkedHashSet<String> getPossibleKeywords(Teacher teacher){
            LinkedHashSet<String> possibleKeywords = new LinkedHashSet<>(Arrays.asList(
                    Words.NEW,Words.END_EXERCISE,Words.INFO,Words.REPEAT,Words.TUTORIAL,Words.SETTINGS, Words.CLOSE_APP));
            if(possibleAnswersOfExercise != null){
                possibleKeywords.addAll(possibleAnswersOfExercise);
            }
            return possibleKeywords;
        }
        @Override
        Fsm executeKeyword(String keyword, Teacher teacher){
            Fsm nextState = this;
            if (Words.TUTORIAL.equals(keyword)) {
                teacher.speak(teacher.getExerciseInfo());
            } else if (keyword.equals(Words.INFO)) {
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
                    }
                    teacher.askQuestion();
                }
            } else if (Words.RESUME.equalsIgnoreCase(keyword)) {
            } else if (Words.END_EXERCISE.equals(keyword)) {
                nextState = Fsm.HOME;
            } else if (Words.SETTINGS.equals(keyword)) {
                nextState = Fsm.SETTINGS;
            } else {
                Log.i("VOSKa",keyword);

                teacher.checkAnswer(keyword);
            }
            return nextState;
        }
    },
    SETTINGS{
        LinkedHashSet<String> parameterList;
        @Override
        LinkedHashSet<String> getPossibleKeywords(Teacher teacher){
            parameterList = teacher.getExerciseParameters();
            LinkedHashSet<String> possibleKeywords = new LinkedHashSet<>(Arrays.asList(Words.INFO,Words.BACK_TO_EXERCISE,Words.CLOSE_APP));
            if(parameterList != null){
                possibleKeywords.addAll(parameterList);
            }
            for(String i : possibleKeywords){
                Log.i("VOSKa",i);
            }
            return possibleKeywords;
        }
        @Override
        Fsm executeKeyword(String keyword, Teacher teacher){
            Fsm nextState = this;
            if (keyword.equals(Words.INFO)) {
                teacher.speak(Words.YOU_CAN_CHOOSE);
                for (String s : parameterList) {
                    teacher.speak(s);
                }
                teacher.speak(Words.OR + " " + Words.YOU_CAN_CHOOSE);
                teacher.speak(Words.BACK_TO_EXERCISE);
                teacher.speak(Words.CLOSE_APP);
            } else if (Words.BACK_TO_EXERCISE.equals(keyword)) {
                nextState = Fsm.EXERCISE;
            } else if (Words.RESUME.equalsIgnoreCase(keyword)) {
            } else {
                Log.i("VOSKa",keyword);

                teacher.setSelectedParameter(keyword);
                nextState = Fsm.PARAMETER_CHANGE;
            }
            return nextState;
        }
    },
    PARAMETER_CHANGE{
        HashSet<String> possibleSettingsKeywords;
        @Override
        LinkedHashSet<String> getPossibleKeywords(Teacher teacher){
            possibleSettingsKeywords = teacher.getParametersValue();
            LinkedHashSet<String> possibleKeywords = new LinkedHashSet<>(Arrays.asList(Words.INFO,Words.BACK_TO_EXERCISE,Words.BACK_TO_SETTINGS,Words.CLOSE_APP));
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
            } else if (Words.RESUME.equalsIgnoreCase(keyword)) {
            } else {
                Log.i("VOSKa",keyword);
                teacher.setSelectedValue(keyword);
                teacher.speak(teacher.getSelectedParameter() +  " " + Words.IS_CURRENTLY_AT +  " " + teacher.getSelectedValue());
                nextState = Fsm.SETTINGS;
            }
            return nextState;
        }
    };

    Pair<Fsm, LinkedHashSet<String>> execute(String keyword, Teacher teacher){
        keyword = Words.getKeywordFromAlternatives(keyword);
        Fsm nextState = this.executeKeyword(keyword.toLowerCase(), teacher);
        LinkedHashSet<String> possibleKeywords = nextState.getPossibleKeywords(teacher);
        return new Pair<>(nextState, possibleKeywords);
    }

    abstract Fsm executeKeyword(String keyword, Teacher teacher);
    abstract LinkedHashSet<String> getPossibleKeywords(Teacher teacher);
}
