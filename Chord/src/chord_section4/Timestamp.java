/*
 * Timestamp.java
 *
 * EE 360P HW_4_Q1
 *
 * Aaron Babber (aab3456)
 * Santiago Echeverri (se7365)
 *
 */

package chord_section4;

public class Timestamp {
    int logicalClock;
    int serverID;

	public Timestamp(int logicalClock, int pid) {
		super();
		this.logicalClock = logicalClock;
		this.serverID = pid;
	}
	public static int compare(Timestamp a, Timestamp b) {

		if (a.logicalClock > b.logicalClock)
			return 1;
		if (a.logicalClock <  b.logicalClock)
			return -1;
		if (a.serverID > b.serverID) return 1;
		if (a.serverID < b.serverID)
			return -1;

		return 0;
	}
	public int getLogicalClock() {
		return logicalClock;
	}
	public int getPid() {
		return serverID;
	}
}
