package chord;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeRMIInterface extends Remote{
	String hello() throws RemoteException;
}
