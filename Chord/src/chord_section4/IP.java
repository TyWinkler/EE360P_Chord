package chord_section4;

import java.io.Serializable;

public class IP implements Serializable {

	private String ipAddress;
	private int port;
	
	public IP (String ipAddress, int port){
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public int hash(int m) {
		return (int) (Long.parseLong((Hasher.hash(Integer.toString(port))).substring(0, 8),16) % Math.pow(2, m));
	}
	
	@Override
	public String toString(){
		return this.ipAddress + ":" + Integer.toString(this.port);
	}
	
	public String setIP(String ip){
		return this.ipAddress = ip;
	}
	
	public int setPort(int port){
		return this.port = port;
	}
	
	public String getIP(){
		return this.ipAddress;
	}
	
	public int getPort(){
		return this.port;
	}
	
}
