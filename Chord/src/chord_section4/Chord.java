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
		// send messages out about this node
		Node node = new Node(ipAddress,port);
		System.out.println("Running Section 4!");
		System.out.println("Node: " + node.getID() + " created with ip: " + node.getIP().toString());
		
		// setup this RMI Server
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
		
		// decide if we are the first node, and act accordingly:
		
		// if NOT the first node, then join through the specified node
		if(port != firstPort){
			node.join(port);
			
		// ... otherwise (we ARE the first node):
		} else {
			node.setSuccessor(node.getID());		// we set ourselves as our own successor
			node.setPredecessor(node.getID());		// we set ourselves as our own predecessor
			
			// now we initialize our own finger table
			// NOTE: since we are the only node, we make all
			//       of our fingers point to ourself
			Finger[] table = node.getFingerTable();
			for(int i = 1; i <= node.m; i ++) {
				table[i].node = node.getID();
			}
		}
		
		// now we just wait for user commands
		Scanner sc = new Scanner (System.in);
		while(sc.hasNextLine()) {
	        String cmd = sc.nextLine();
	        String[] tokens = cmd.split(" ");
	        
	        // getNode command. Second argument should be the hashValue of the node
	        if(tokens[0].equals("getNode")){
	        	if(tokens[1] != null){
	        		System.out.println(node.getNode(Integer.parseInt(tokens[1])).toString());
	        	} else {
		        	System.err.println("Unrecognized Command");
		        }
	        } 
	        
	        // put command. provide the key and value
	        else if(tokens[0].equals("put")){
	        	if(tokens[1] != null && tokens[2] != null){
	        		node.put(tokens[1],tokens[2]);
	        	} else {
		        	System.err.println("Unrecognized Command");
		        }
	        } 
	        
	        // get command. provide the key and get he value
	        else if(tokens[0].equals("get")){
	        	if(tokens[1] != null){
	        		System.out.println(node.get(tokens[1]));
	        	} else {
		        	System.err.println("Unrecognized Command");
		        }
	        } 
	        
	        // quit the app!
	        else if(tokens[0].equals("quit")){
	        	break;
	        } 
	        
	        // unrecognized!
	        else {
	        	System.err.println("Unrecognized Command");
	        }
	    }
		sc.close();

	}
	
}
