package org.vosk.earcoach;

import org.billthefarmer.mididriver.GeneralMidiConstants;
import org.billthefarmer.mididriver.MidiConstants;
import org.billthefarmer.mididriver.MidiDriver;
import org.billthefarmer.mididriver.ReverbConstants;

/*
This class controls the internal Android MIDI engine using the MidiDriver library. It has a public
method that allows to play a given structure of array of arrays that represents a sequence of chords.
Every item represents the MIDI notes that needs to be executed at the same moment.

For example, the structure:
{{60},{},{50,55}}
is a sequence of a single note (60), followed by a pause, followed by a chord composed of two notes (50,55).

There is also a method to change instrument using General Midi values to select it.
 */

public class Synth implements MidiDriver.OnMidiStartListener {

    protected MidiDriver midi;
    private int volume;
    private final SynthListener synthListener;
    private int[] currentlyPlaying;

    public Synth(SynthListener synthListener){
        volume = 100;
        midi = MidiDriver.getInstance(this);
        midi.start();
        midi.setReverb(ReverbConstants.OFF);
        this.synthListener = synthListener;
    }

    @Override
    public void onMidiStart() {
        changeInstrument(GeneralMidiConstants.ACOUSTIC_GRAND_PIANO);
    }

    public void changeInstrument(int instrument){
        sendMidi(MidiConstants.PROGRAM_CHANGE, instrument);
    }

    protected void sendMidi(int m, int n, int o)
    {
        byte[] msg = new byte[3];
        msg[0] = (byte) m;
        msg[1] = (byte) n;
        msg[2] = (byte) o;
        midi.write(msg);
    }

    protected void sendMidi(int m, int n)
    {
        byte[] msg = new byte[2];
        msg[0] = (byte) m;
        msg[1] = (byte) n;
        midi.write(msg);
    }

    public void play(int[][] chordSequence, int duration){
        boolean isLast = false;
        for(int i=0;i<chordSequence.length;i++){
            int[] notesInChord = chordSequence[i];
            if(i == chordSequence.length-1){
                isLast = true;
            }
            currentlyPlaying = notesInChord;
            for (int k : notesInChord) {
                sendMidi(MidiConstants.NOTE_ON, k, volume);
            }
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int k : notesInChord) {
                sendMidi(MidiConstants.NOTE_OFF, k, 0);
            }
            if(isLast){
                currentlyPlaying = new int[]{};
                synthListener.synthHasEnded();
            }
        }
    }

    public void stopWhatIsCurrentlyPlaying(){
        if(currentlyPlaying != null){
            for (int i : currentlyPlaying) {
                sendMidi(MidiConstants.NOTE_OFF, i, 0);
            }
        }
        synthListener.synthHasEnded();
    }

    public void stopSynth(){
        midi.stop();
    }
}
