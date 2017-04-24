package chord_section4;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Random;


public class Node implements NodeRMIInterface{

	private int nodeID;
	private Finger finger[];
	private IP ipAddress;
	private int successor = -1;
	private int predecessor = -1;
	public final int m = 12;
	private HashMap<Integer, String> map = new HashMap<Integer, String>();
	
	
	public Node(String ipAddress, int port){
		this.ipAddress = new IP(ipAddress, port);
		this.nodeID = this.ipAddress.hash(m);
		
		// initialize finger table's start 
		// NOTE: the actual node the finger points to, 
		//       is NOT initialized. This should be done later
		this.finger = new Finger[m + 1];
		for(int i = 1; i < m + 1; i++) {
			finger[i] = new Finger(m, nodeID, i);
		}
	}
	
	public Node(){
	}

	@Override
	public boolean equals(Object node){
		if(!(node instanceof Node)){
			return false;
		} 
		if(((Node) node).nodeID == this.nodeID){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString(){
		return Integer.toString(nodeID);
	}
	
	/**
	 * This method sets the successor of THIS node
	 * @param i
	 */
	public void setSuccessor(int i){
		this.successor = i;
	}
	
	/**
	 * This method sets the predecessor of THIS node
	 * @param i
	 */
	public void setPredecessor(int i){
		this.predecessor = i;
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
	
	public IP getIP(){
		return this.ipAddress;
	}
	
	public int getID(){
		return nodeID;
	}
	
	public int getSuccessor() {
		return this.successor;
	}
	
	public int getPredecessor() {
		return this.predecessor;
	}
	
	public Finger[] getFingerTable() {
		/*FingerTableRMI table;
		try {
			table = new FingerTableRMI();
			table.setTable(finger);
			return table;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return finger;
	}
	
	public void join(int node){
		Node n = getNode(node);
		this.successor = n.find_successor(this.getID()).getID();
	}
	
	public synchronized void stabilize() {
		Node successorNode = getNode(successor);
		Node x = getNode(successorNode.getPredecessor());
		int xID = x.getID();
			if(xID > this.getID() && xID < this.getSuccessor() ) {
			successor = xID;
		}
		x.notify(this);
	}

	public synchronized void notify(Node n) {
		int nID = n.getID();
		if(predecessor == -1 || (nID > predecessor && nID < this.getID())) {
			predecessor = nID;
		}
	}
	
	
	public synchronized void fix_fingers() {
		int i = new Random().nextInt(m) + 1;
		finger[i].node = find_successor(finger[i].start).getID();
	}
	
	
	public synchronized Node find_successor(int id) {
		Node nPrime = find_predecessor(id);
		return getNode(nPrime.successor);
	}
	
	/**
	 * This method finds the predecessor node of the given id
	 */
	public Node find_predecessor(int id) {
		Node nPrime = this;
		while(nodeIdIsNotAfterLeftBoundAndWithinUpToRightBound(id, nPrime.nodeID, nPrime.successor)) {
			nPrime = nPrime.closest_preceding_finger(id);
		}
		return nPrime;
	}
	
	/**
	 * This method checks if the given id does not lie after leftBound
	 * and up to rightBound
	 * @param id
	 * @param leftBound
	 * @param rightBound
	 * @return true if the finger is not within (leftBound, rightBound]
	 */
	private boolean nodeIdIsNotAfterLeftBoundAndWithinUpToRightBound(int id, int leftBound, int rightBound){
		boolean afterLeftBound = false;
		boolean upToRightBound = false;
		
		if(leftBound <= rightBound){
			afterLeftBound = leftBound < id;
			upToRightBound = id <= rightBound;
		}
		else{		// leftBound > rightBound
			if(leftBound < id){
				rightBound = rightBound + ((int) Math.pow(2, this.m));
				afterLeftBound = leftBound < id;
				upToRightBound = id <= rightBound;
			}
			else{	// leftBound >= id
				leftBound = leftBound - ((int) Math.pow(2, this.m));
				afterLeftBound = leftBound < id;
				upToRightBound = id <= rightBound;
			}
		}
		return !(afterLeftBound && upToRightBound);
	}
	
	/**
	 * This method finds the finger of THIS node that is preceding
	 * of the argument ID
	 */
	public Node closest_preceding_finger(int id) {
		for(int i = m; i >= 1; i--){
			int thisFingerID = finger[i].node;
			
			if(fingerIsWithinNonInclusiveBoundsOf(thisFingerID, this.nodeID, id)) {
				return getNode(finger[i].node);
			}
			
		}
		return this;
	}
	
	/**
	 * This method checks if the given finger lies within the given
	 * non-inclusive bounds
	 * @param finger
	 * @param leftBound
	 * @param rightBound
	 * @return true if the finger lies within (leftBound, rightBound)
	 */
	private boolean fingerIsWithinNonInclusiveBoundsOf(int finger, int leftBound, int rightBound){
		boolean afterLeftBound = false;
		boolean beforeRightBound = false;
		
		if(leftBound <= rightBound){
			afterLeftBound = leftBound < finger;
			beforeRightBound = finger < rightBound;
		}
		else{		// leftBound > rightBound
			if(leftBound < finger){
				rightBound = rightBound + ((int) Math.pow(2, this.m));
				afterLeftBound = leftBound < finger;
				beforeRightBound = finger < rightBound;
			}
			else{	// leftBound >= finger
				leftBound = leftBound - ((int) Math.pow(2, this.m));
				afterLeftBound = leftBound < finger;
				beforeRightBound = finger < rightBound;
			}
		}
		return afterLeftBound && beforeRightBound;
	}
	
	public Node find(int keyID) {
		return find_successor(keyID);
		
	}
	
	public String getValue(int keyID){
		return map.get(keyID);
	}
	
	public String putValue(int keyID, String value){
		return map.put(keyID, value);
	}
	
	public String get(String key) {
		int keyID = (int) (Long.parseLong((Hasher.hash(key)).substring(0, 8),16) % Math.pow(2, m));
		Node n = find(keyID);
		return n.getValue(keyID);
	}
	
	public String put(String key, String value) {
		int keyID = (int) (Long.parseLong((Hasher.hash(key)).substring(0, 8),16) % Math.pow(2, m));
		Node n = find(keyID);
		return n.putValue(keyID, value);
	}
	
	
	public int[] getArray(){
		return new int[3];
	}
	
	public Node getNode(int id) {
		if(id == nodeID || id == -1) {
			return this;
		}
		try {
            Registry registry = LocateRegistry.getRegistry("localhost",id);
            NodeRMIInterface stub = (NodeRMIInterface) registry.lookup(Integer.toString(id));
            Node n = new Node();
            n.finger = stub.getFingerTable();
            n.predecessor = stub.getPredecessor();
            n.successor = stub.getSuccessor();
            n.nodeID = stub.getID();
            n.map = stub.getMap();
            return  n;
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
		return null;
	}
	
	public static int getNode2(int id) {
		try {
            Registry registry = LocateRegistry.getRegistry("localhost",id);
            NodeRMIInterface stub = (NodeRMIInterface) registry.lookup(Integer.toString(id));
            return stub.getPredecessor();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
		return -1;
	}
	
}
