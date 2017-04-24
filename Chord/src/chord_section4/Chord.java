package chord_section4;

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
	@Parameter(names={"--new", "-n"})
	int firstPort;

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
		
		if(port != firstPort){
			node.join(port);
		} else {
			node.setSuccessor(node.getID());
			node.setPredecessor(node.getID());
			Finger[] table = node.getFingerTable();
			for(int i = 1; i <= node.m; i ++) {
				Finger f = table[i];
				f.node = node.getID();
				table[i] = f;
			}
		}
		
		Thread stablizer = new Stablizer(node);
		stablizer.start();
		
		Thread fingerer = new Fingerer(node);
		fingerer.start();
		
		Scanner sc = new Scanner (System.in);
		while(sc.hasNextLine()) {
	        String cmd = sc.nextLine();
	        String[] tokens = cmd.split(" ");
	        if(tokens[0].equals("getNode")){
	        	if(tokens[1] != null){
	        		System.out.println(node.getNode(Integer.parseInt(tokens[1])).toString());
	        	} else {
		        	System.err.println("Unrecognized Command");
		        }
	        } else if(tokens[0].equals("put")){
	        	if(tokens[1] != null && tokens[2] != null){
	        		node.put(tokens[1],tokens[2]);
	        	} else {
		        	System.err.println("Unrecognized Command");
		        }
	        } else if(tokens[0].equals("get")){
	        	if(tokens[1] != null){
	        		System.out.println(node.get(tokens[1]));
	        	} else {
		        	System.err.println("Unrecognized Command");
		        }
	        } else if(tokens[0].equals("quit")){
	        	break;
	        } else {
	        	System.err.println("Unrecognized Command");
	        }
	    }
		sc.close();

	}
	
}
