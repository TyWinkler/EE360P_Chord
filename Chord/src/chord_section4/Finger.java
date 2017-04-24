package chord_section4;

import java.io.Serializable;

public class Finger implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int start;
	Node node = null;
	
	
	public Finger(int m, int n, int k) {
		start = (int) ((n + Math.pow(2, k -1)) % Math.pow(2, m));
	}	
}
