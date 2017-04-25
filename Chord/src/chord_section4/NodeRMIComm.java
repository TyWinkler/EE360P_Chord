package chord_section4;

import java.rmi.RemoteException;
import java.util.HashMap;

public class NodeRMIComm implements NodeRMIInterface {
	Node commNode;
	
	public NodeRMIComm(Node commNode){
		this.commNode = commNode;
	}
	
	public int getID() throws RemoteException{
		return commNode.getID();
	}
	
	public Node getSuccessor() throws RemoteException{
		return commNode.getSuccessor();
	}
	
	public Node getPredecessor() throws RemoteException{
		return commNode.getPredecessor();
	}
	
	public Node find_successor(int id) throws RemoteException{
		return commNode.find_successor(id);
	}
	
	public Node find_predecessor(int id) throws RemoteException{
		return commNode.find_predecessor(id);
	}
	
	public Node closest_preceding_finger(int id) throws RemoteException{
		return commNode.closest_preceding_finger(id);
	}
	
	public String getValue(int keyID) throws RemoteException{
		return commNode.getValue(keyID);
	}
	
	public void putValue(int keyID, String value) throws RemoteException{
		commNode.putValue(keyID, value);
	}
	
	public Finger[] getFingerTable() throws RemoteException{
		return commNode.getFingerTable();
	}
	
	public HashMap<Integer, String> getMap() throws RemoteException{
		return commNode.getMap();
	}
	
	public void setPredecessor(Node newPredecessor) throws RemoteException{
		commNode.setPredecessor(newPredecessor);
	}
	
	public void update_finger_table(Node candidateNode, int candidateRow) throws RemoteException{
		commNode.update_finger_table(candidateNode, candidateRow);
	}
	
	public HashMap<Integer,String> getKeysAfterLeftAndUpToRight(int left, int right) throws RemoteException{
		return commNode.getKeysAfterLeftAndUpToRight(left, right);
	}
	
	public void addNewNode(Node newNodeID) throws RemoteException{
		commNode.addNewNode(newNodeID);
	}
	
	// ----------------------- MUTEX METHODS -------------------------
	public void requestJoin(int requesterID) throws RemoteException{
		//Thread requestThread = new Thread(() -> {
			// request the network permission to invite the requester
			Chord.mutex.requestCS(Chord.node.getIP().getPort());

			// tell the requester that her permission is granted
			NodeRMIInterface requesterStub = Node.getNodeRMIStub(new Node("localhost", requesterID));
			try {
				requesterStub.joinRequestGranted();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//});
		
		//requestThread.start();
	}
	
	public void joinRequestGranted() throws RemoteException{
		Chord.joinLock.lock();
		Chord.joinCond.signalAll();
		Chord.joinLock.unlock();
	}
	
	public void reportJoinFinished() throws RemoteException{
		// tell the network that the cs is finished
		Chord.mutex.releaseCS("releaseJoin");
	}
	
	public void newLamportMsg(int srcId, String tag, int srcClk) throws RemoteException{
		
		Chord.mutex.handleMsg(srcId, tag, srcClk);
	}

}
