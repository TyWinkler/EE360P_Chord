package chord_section4;

import org.apache.commons.codec.digest.DigestUtils;

public class Hasher {
	
	public static String hash(String port) {
		return DigestUtils.sha1Hex(port);
	}
	
}
