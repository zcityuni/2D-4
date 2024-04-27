// IN2011 Computer Networks
// Coursework 2023/2024
//
// Construct the hashID for a string

import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashID {

    public static byte [] computeHashID(String line) throws Exception {
	if (line.endsWith("\n")) {
	    // What this does and how it works is covered in a later lecture
	    MessageDigest md = MessageDigest.getInstance("SHA-256");
	    md.update(line.getBytes(StandardCharsets.UTF_8));
	    return md.digest();

	} else {
	    // 2D#4 computes hashIDs of lines, i.e. strings ending with '\n'
	    throw new Exception("No new line at the end of input to HashID");
	}
    }

	/*public static String hash(String nodeName) throws Exception {
		byte[] hashBytes = HashID.computeHashID(nodeName + "\n");
		StringBuilder convert = new StringBuilder();
		for(byte b: hashBytes){
			String hex = Integer.toHexString(0xff & b);
			if(hex.length() == 1){
				convert.append("0");
			}
			convert.append(hex);
		}
		return convert.toString();
	}

	public static void main(String[] args) throws Exception {
		String name = "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000";
		String hashed = hash(name);
		System.out.println(hashed);
	}*/
}
