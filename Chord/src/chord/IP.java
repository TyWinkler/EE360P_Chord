package chord;

public class IP {

	private int ipAddress;
	private int port;
	
	public IP (int ipAddress, int port){
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	@Override
	public String toString(){
		return Integer.toString(this.ipAddress) + ":" + Integer.toString(this.port);
	}
	
	public int setIP(int ip){
		return this.ipAddress = ip;
	}
	
	public int setPort(int port){
		return this.port = port;
	}
	
	public int getIP(){
		return this.ipAddress;
	}
	
	public int getPort(){
		return this.port;
	}
	
}
