package chord;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class NodeClient extends Thread{
	
	NodeClient() { }
	
	public void run(){
		int host = 5000;
        try {
            Registry registry = LocateRegistry.getRegistry("localhost",host);
            NodeRMIInterface stub = (NodeRMIInterface) registry.lookup("Hello");
            String response = stub.hello();
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}

}
