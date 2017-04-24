package chord_section4;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface NodeRMIInterface extends Remote{
	public int getID() throws RemoteException;
	public int getSuccessor() throws RemoteException;
	public int getPredecessor() throws RemoteException;
	public Node find_successor(int id) throws RemoteException;
	public Node find_predecessor(int id) throws RemoteException;
	public Node closest_preceding_finger(int id) throws RemoteException;
	public String getValue(int keyID) throws RemoteException;
	public String putValue(int keyID, String value) throws RemoteException;
	public Finger[] getFingerTable() throws RemoteException;
	public HashMap<Integer, String> getMap() throws RemoteException;
	public int[] getArray() throws RemoteException;
}
