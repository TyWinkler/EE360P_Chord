package chord;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Chord {
	@Parameter(names={"--ip", "-i"})
	String ipAddress;
	@Parameter(names={"--port", "-p"})
	int port;

	/**
	 * @param args
	 */
	public static void main(String ... args) {
		Chord main = new Chord();
		JCommander.newBuilder().addObject(main).build().parse(args);
	    main.run();
	}
	
	
	
	
	public void run() {
		Node node = new Node(ipAddress,port);
		System.out.println("Node: " + node.getID() + " created with ip: " + node.getIP().toString());

		
		try {
            NodeRMIInterface stub = (NodeRMIInterface) UnicastRemoteObject.exportObject(node, port);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind(Integer.toString(port), stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.out.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
		
		Scanner sc = new Scanner (System.in);
		while(sc.hasNextLine()) {
	        String cmd = sc.nextLine();
	        String[] tokens = cmd.split(" ");
	        if(tokens[0].equals("get")){
	        	if(tokens[1] != null){
	        		System.out.println(node.getNode(Integer.parseInt(tokens[1])).toString());
	        	}
	        } else if(tokens[0].equals("put")){
	        	
	        } else if(tokens[0].equals("quit")){
	        	break;
	        } else {
	        	System.err.println("Unrecognized Command");
	        }
	    }
		sc.close();

	}
	
}
