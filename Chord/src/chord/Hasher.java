package chord;

import org.apache.commons.codec.digest.DigestUtils;

public class Hasher {
	
	public String hash(String port) {
		return DigestUtils.sha1Hex(port);
	}
	
}
