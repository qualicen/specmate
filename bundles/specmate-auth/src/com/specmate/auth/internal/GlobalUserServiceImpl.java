package com.specmate.auth.internal;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.auth.api.IGlobalUserService;
import com.specmate.config.api.IConfigService;

@Component(immediate = true, service = IGlobalUserService.class)
public class GlobalUserServiceImpl implements IGlobalUserService {

	/** key length of the hashing algorithm */
	private static final int HASH_KEY_LENGTH = 128;

	/** number of iterations done by the hashing algorithm */
	private static final int HASH_NUM_ITERATIONS = 65536;

	/** prefix of the config entries for global users */
	private static final String CONFIG_PREFIX = "globalusers.";

	/** Map to save global users and passwords/salts */
	Map<String, String[]> globalUsers = new HashMap<>();

	@Reference
	private IConfigService configService;

	@Reference(service = LoggerFactory.class)
	private Logger logger;

	@Activate
	public void activate() {
		Set<Entry<Object, Object>> globalUserConfigs = configService.getConfigurationProperties(CONFIG_PREFIX);
		for (Entry<Object, Object> entry : globalUserConfigs) {
			String key = (String) entry.getKey();
			String userName = key.substring(CONFIG_PREFIX.length());
			String passwordHash = (String) entry.getValue();
			String[] saltAndHash = passwordHash.split(":");
			if (saltAndHash.length != 2) {
				logger.warn("Invalid password spec for user " + userName);
			}

			globalUsers.put(userName, saltAndHash);
		}
	}

	@Override
	public boolean authenticate(String username, String password) {
		String[] saltAndHash = globalUsers.get(username);
		if (saltAndHash == null) {
			return false;
		}

		String salt = saltAndHash[0];
		String actualPasswordHash = saltAndHash[1];

		String computedPasswordHash = hash(salt, password);
		return computedPasswordHash.equals(actualPasswordHash);
	}

	/** Hashes the given password using the given salt. */
	private String hash(String salt, String password) {
		byte[] saltBytes = Base64.getDecoder().decode(salt);

		KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, HASH_NUM_ITERATIONS, HASH_KEY_LENGTH);
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] encoded = factory.generateSecret(spec).getEncoded();
			String base64encoded = Base64.getEncoder().withoutPadding().encodeToString(encoded);
			return base64encoded;

		} catch (NoSuchAlgorithmException e) {
			logger.error("Cannot instantiate PBKDF2 algorithm.");
			return null;
		} catch (InvalidKeySpecException e) {
			logger.error("Error while hashing password.");
			return null;
		}
	}

}
