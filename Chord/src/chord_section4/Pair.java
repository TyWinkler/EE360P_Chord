package chord;

import java.io.Serializable;

public class Pair implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5965260089628068384L;
	int start;
	int end;
	
	public Pair(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}
	
	public void setStart(int start) {
		this.start = start;
	}
	
	public void setEnd(int end) {
		this.end = end;
	}
	

}
