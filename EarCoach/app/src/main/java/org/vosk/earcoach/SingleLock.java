package org.vosk.earcoach;

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
