package chord;

import java.util.ArrayList;
import java.util.Random;

public class Node {
	
	private String nodeID;
	private ArrayList<Node> fingertable;
	private IP ipAddress;
	private Node successor;
	private Node predecessor;
	
	public Node(String ipAddress, int port){
		this.ipAddress = new IP(ipAddress, port);
		this.nodeID = this.ipAddress.hash();
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
		return nodeID;
	}
	
	public IP getIP(){
		return this.ipAddress;
	}
	
	public String getID(){
		return nodeID;
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
		Node x = successor.predecessor;
		if(x == this || x == predecessor) {
			successor = x;
		}
		successor.notify(this);
	}

	public void notify(Node n) {
		if(predecessor == null || (n == predecessor || n == this)) {
			predecessor = n;
		}
	}
	
	
	/**
	 * Assuming m = 4
	 */
	public void fix_fingers() {
		int i = new Random().nextInt(fingertable.size() - 1) + 1;
		Node n = fingertable.get(i);
		//n = find_successor((nodeID + Math.pow(2 , i - 1)) % 16);
		fingertable.set(i, n);
	}
	
	
}
