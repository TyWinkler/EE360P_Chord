package chord_section4;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Chord extends Thread{
	public static final boolean DEBUG = false;
	
	@Parameter(names={"--ip", "-i"})
	static String ipAddress;
	@Parameter(names={"--port", "-p"})
	static int port;
	@Parameter(names={"--new", "-n"})
	static int firstPort;
	
	// locks used for waiting 
	static ReentrantLock joinLock = new ReentrantLock();
	static Condition joinCond = joinLock.newCondition();
	static LamportMutex mutex = new LamportMutex();
	static Node node = null;
	static Registry registry = null;

	/**
	 * @param args
	 * @throws RemoteException 
	 */
	public static void main(String ... args) throws RemoteException {
		Chord main = new Chord();
		JCommander.newBuilder().addObject(main).build().parse(args);
	    new Chord().start();
	}
	
	public void run(){
		// send messages out about this node
		node = new Node(ipAddress,port);
		System.out.println("Running Section 4!");
		System.out.println("Node: " + node.getID() + " created with ip: " + node.getIP().toString());
		
		// setup this RMI Server
		NodeRMIInterface stub = null;
		try {
			//System.setProperty("java.rmi.server.hostname", "localhost");
			
			NodeRMIComm communicator = new NodeRMIComm(node);
			
            stub = (NodeRMIInterface) UnicastRemoteObject.exportObject(communicator, port);

            // Bind the remote object's stub in the registry
            if(port == firstPort){
            	registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            	registry.bind(Integer.toString(port), stub);
            }
            else{
            	registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
            }
            System.out.println("Server ready");
        } catch (Exception e) {
            System.out.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
		
		// decide if we are the first node, and act accordingly:
		
		// if NOT the first node, then join through the specified node
		if(port != firstPort){
			// join to the original node
			try {
				// request permission to join
				registry.bind(Integer.toString(port), stub);
				NodeRMIInterface originalStub = Node.getNodeRMIStub(new Node("localhost", firstPort));
				originalStub.requestJoin(node.getIP().getPort());
	
				
	
				// now join
				node.join(firstPort);
			
				
				// signal that you have finished joining
				originalStub.reportJoinFinished();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		// ... otherwise (we ARE the first node):
		} else {
			node.setSuccessor(node);		// we set ourselves as our own successor
			try {
				node.setPredecessor(node);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		// we set ourselves as our own predecessor
			
			// now we initialize our own finger table
			// NOTE: since we are the only node, we make all
			//       of our fingers point to ourself
			Finger[] table = null;
			try {
				table = node.getFingerTable();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i = 1; i <= node.m; i ++) {
				table[i].node = node;
			}
		}
		
		// now we just wait for user commands
		Scanner sc = new Scanner (System.in);
		while(sc.hasNextLine()) {
	        String cmd = sc.nextLine();
	        String[] tokens = cmd.split(" ");
	        
	        // getNode command. Second argument should be the hashValue of the node
	        if(tokens[0].equals("getNode")){
	        	System.out.println("This node is on port: " + node.getIP().getPort());
	        } 
	        
	        // put command. provide the key and value
	        else if(tokens[0].equals("put")){
	        	if(tokens[1] != null && tokens[2] != null){
	        		try {
						node.put(tokens[1],tokens[2]);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}       		
	        	} else {
		        	System.err.println("Unrecognized Command");
		        }
	        } 
	        
	        // get command. provide the key and get he value
	        else if(tokens[0].equals("get")){
	        	if(tokens[1] != null){
	        		try {
						System.out.println(node.get(tokens[1]));
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	} else {
		        	System.err.println("Unrecognized Command");
		        }
	        } 
	        
	        // quit the app!
	        else if(tokens[0].equals("quit")){
	        	
	        	break;
	        } 
	        
	        // some useful debugging commands
			else if (tokens[0].equals("getSuc")) {
				try {
					int thisNodeID = node.getID();
					Node successor = node.getSuccessor();
					int sucID = successor.getID();
					System.out.println("This node has ID " + node.getID() + " and its successor is node with ID " + sucID);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	        
	        // unrecognized!
	        else {
	        	System.err.println("Unrecognized Command");
	        }
	    }
		sc.close();

	}
	
	/**
	 * used for debugging print
	 */
	public static void debugPrint(String debugMessage){
		if(Chord.DEBUG){
			System.out.println(debugMessage);
			System.out.println("");
		}
	}
	
}
