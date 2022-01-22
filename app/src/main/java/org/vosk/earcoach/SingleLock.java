package org.vosk.earcoach;

/*
This is a lock object used to synchronize (sequentially) the behaviour of the three methods of Teacher (Speak, Listen and
Play). Each method takes the lock when it is called and releases it when it has finished using a callback.
 */

public class SingleLock {
    private int state = 0;

    public synchronized void releaseLock(){
        state = 0;
        notifyAll();
    }

    public synchronized void getLock(){
        while (state != 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        state = 1;
    }
}
