package chord_section4;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface NodeRMIInterface extends Remote{
	public int getID() throws RemoteException;
	public Node getSuccessor() throws RemoteException;
	public Node getPredecessor() throws RemoteException;
	
	public Node find_successor(int id) throws RemoteException;
	public Node find_predecessor(int id) throws RemoteException;
	public Node closest_preceding_finger(int id) throws RemoteException;
	
	public String getValue(int keyID) throws RemoteException;
	public void putValue(int keyID, String value) throws RemoteException;
	
	public Finger[] getFingerTable() throws RemoteException;
	public HashMap<Integer, String> getMap() throws RemoteException;
	
	public void setPredecessor(Node newPredecessor) throws RemoteException;
	
	public void update_finger_table(Node candidateNode, int candidateRow) throws RemoteException;
	
	public HashMap<Integer,String> getKeysAfterLeftAndUpToRight(int left, int right) throws RemoteException;
	
	public void addNewNode(Node newNodeID) throws RemoteException;

	public void requestJoin(int requesterID) throws RemoteException;
	
	public void joinRequestGranted() throws RemoteException;
	
	public void reportJoinFinished() throws RemoteException;
	
	public void newLamportMsg(int srcId, String tag, int srcClk) throws RemoteException;
}
