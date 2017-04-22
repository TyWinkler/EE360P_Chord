package chord;

import java.util.ArrayList;
import java.util.Random;


public class Node {

	private int nodeID;
	private Finger finger[];
	private IP ipAddress;
	private int successor = -1;
	private int predecessor = -1;
	private int m = 12;
	
	
	public Node(String ipAddress, int port){
		this.ipAddress = new IP(ipAddress, port);
		this.nodeID = this.ipAddress.hash(m);
		this.finger = new Finger[m + 1];
		for(int i = 1; i < m + 1; i++) {
			finger[i] = new Finger(m, nodeID, i);
		}
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
		return finger;
	}
	
	public void stabilize() {
		Node successorNode = getNode(successor);
		Node x = getNode(successorNode.getPredecessor());
		int xID = x.getID();
			if(xID > this.getID() && xID < this.getSuccessor() ) {
			successor = xID;
		}
		x.notify(this);
	}

	public void notify(Node n) {
		int nID = n.getID();
		if(predecessor == -1 || (nID > predecessor && nID < this.getID())) {
			predecessor = nID;
		}
	}
	
	
	public void fix_fingers() {
		int i = new Random().nextInt(m) + 1;
		finger[i].node = find_successor(finger[i].start).getID();
	}
	
	
	public Node find_successor(int id) {
		return getNode(find_predecessor(id).getSuccessor());
	}
	
	public Node find_predecessor(int id) {
		Node predecessor = this;
		while(id <= predecessor.nodeID && id > getNode(predecessor.getSuccessor()).getID()) {
			predecessor = predecessor.closest_preceding_finger(id);
		}
		return predecessor;
	}
	
	public Node closest_preceding_finger(int id) {
		for( int i = m; i >= 1; i --) {
			if(finger[i].node == -1) {
				continue;
			}
			if(finger[i].node > this.getID() &&  finger[i].node < id) {
				return getNode(finger[i].node);
			}
		}
		return this;
	}
	
	
	public static Node getNode(int id) {
		
	}
	
}
