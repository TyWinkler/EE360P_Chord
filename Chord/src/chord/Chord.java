package chord;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
            System.err.println("Server ready");
            System.err.println(node.getNode2(port));            
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }


	    
		
	}
	
}
