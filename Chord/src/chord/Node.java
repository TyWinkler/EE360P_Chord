package chord;

import java.util.ArrayList;
import java.util.Random;


public class Node {
	
	private Hasher hasher = new Hasher();
	private int nodeID;
	private Finger finger[];
	private IP ipAddress;
	private int successor = -1;
	private int predecessor = -1;
	int m;
	
	
	public Node(String ipAddress, int port, int m){
		this.ipAddress = new IP(ipAddress, port);
		this.nodeID = this.ipAddress.hashCode();
		this.m = m;
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
		return String.valueOf(nodeID);
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
	
	//Subject to change
	public void updateOthers(){
		for(int i = 1; i < fingertable.size(); i++){
			//find last node p whose ith finger might be n
//			Node p = find_predecessor(this.nodeID - (int)Math.pow(2, (i-1)));
//			p.updateFingerTable(this,i);
		}
	}
	
	//Subject to change
	public void updateFingerTable(Node s, int i){
//		for(int j = this.nodeID; j < i; j++){
//			if(s.getID() == this.fingertable.get(j).getID()){
//				//Does this give me a pointer to this node or just a copy of it?
//				Node p = this.fingertable.get(j);
//				p = s;
//				p.updateFingerTable(s, i);
//			}
//		}

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
