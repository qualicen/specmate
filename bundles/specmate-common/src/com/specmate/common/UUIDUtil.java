package com.specmate.common;

import java.util.UUID;

public class UUIDUtil {
	
	// Generates a new id (we hope we do not get duplicates. We do not check for that.)
	public static String generateUUID() {
		UUID uuid = UUID.randomUUID();
        return uuid.toString().substring(24);
	}

}
