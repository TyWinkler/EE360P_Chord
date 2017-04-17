package chord;

import java.util.ArrayList;

public class Node {
	
	private int nodeID;
	private ArrayList<Node> fingertable;
	
	public Node(int nodeID){
		this.nodeID = nodeID;
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
		return Integer.toString(this.nodeID);
	}
	
	public int getID(){
		return this.nodeID;
	}
	
	//Subject to change
	public void updateOthers(){
		for(int i = 1; i < fingertable.size(); i++){
			//find last node p whose ith finger might be n
			Node p = findPredecessor(this.nodeID - (int)Math.pow(2, (i-1)));
			p.updateFingerTable(this,i);
		}
	}
	
	//Subject to change
	public void updateFingerTable(Node s, int i){
		for(int j = this.nodeID; j < i; j++){
			if(s.getID() == this.fingertable.get(j).getID()){
				Node p = this.fingertable.get(j);
				p = s;
				p.updateFingerTable(s, i);
			}
		}
	}
	
}
