package chord;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
		Scanner sc = new Scanner(System.in);
	    while(sc.hasNextLine()) {
			String cmd = sc.nextLine();
			String[] tokens = cmd.split(" ");
			if (tokens[0].equals("get")) {
				//node.get();
			} else if (tokens[0].equals("put")) {
				//node.put();
			}
	    }
	    sc.close();
	    
	    Thread nodeServer = new NodeServer();
	    Thread nodeClient = new NodeClient();
	    nodeServer.start();
	    nodeClient.start();
		
	}
	
}
