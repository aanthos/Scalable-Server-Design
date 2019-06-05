package cs455.scaling.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * 
 */
public class Hash {
	
	/*
	 * Computes the SHA-1 hash of a byte array
	 * 
	 * @param data Byte array with the data
	 * @return String hex string representing 
	 */
	public static String SHA1FromBytes(byte[] data) {
		MessageDigest digest = null;
		
		try {
			digest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not find algorithm SHA1: " + e);
			return null;
		}
		
		byte[] hash = digest.digest(data);
		BigInteger hashInt = new BigInteger(1, hash);
		return hashInt.toString(16);
	}
}
