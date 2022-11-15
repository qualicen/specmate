import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.util.Base64;
import java.security.SecureRandom;

public class HashGenerator {
	public static void main(String[] args){
		try{
			if(args.length != 1){
				System.out.println("Usage: java create-hash.java [password]");
				System.exit(1);
			}
			// Create salt
			SecureRandom random = new SecureRandom();
			byte[] salt = new byte[16];
			random.nextBytes(salt);
			PBEKeySpec spec = new PBEKeySpec(args[0].toCharArray(),salt,65536,128);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash  = factory.generateSecret(spec).getEncoded();

			String hashString = Base64.getEncoder().withoutPadding().encodeToString(hash);
			String saltString = Base64.getEncoder().withoutPadding().encodeToString(salt);
			
			System.out.println(saltString + ":" +hashString);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
