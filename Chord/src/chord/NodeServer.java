package chord;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NodeServer extends Thread implements NodeRMIInterface{

	@Override
	public String hello() throws RemoteException {
		return "Hello";
	}
	
	public void run(){
		try {
            NodeServer obj = new NodeServer();
            NodeRMIInterface stub = (NodeRMIInterface) UnicastRemoteObject.exportObject(obj, 5000);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry("localhost",5000);
            registry.bind("NodeRMIInterface", stub);

            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
	}

}
