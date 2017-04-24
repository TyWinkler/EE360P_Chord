/*
 * LamportClock.java
 * (taken from Dr. Garg's notes)
 * EE 360P HW_4_Q1
 *
 * Aaron Babber (aab3456)
 * Santiago Echeverri (se7365)
 *
 */

package chord_section4;

public class LamportClock {
    int c;
    public LamportClock() {
        c = 1;
    }
    public int getValue() {
        return c;
    }
    public void tick() { // on internal events
        c = c + 1;
    }
    public void sendAction() {
       // include c in message
        c = c + 1;      
    }
    public void receiveAction(int sentValue) {
        c = Math.max(c, sentValue) + 1;
    }
}
