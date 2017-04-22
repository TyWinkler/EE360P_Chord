package chord;

import org.apache.commons.codec.digest.DigestUtils;

public class IP {

	private String ipAddress;
	private int port;
	
	public IP (String ipAddress, int port){
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public String hash() {
		return DigestUtils.sha1Hex(Integer.toString(port));
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
