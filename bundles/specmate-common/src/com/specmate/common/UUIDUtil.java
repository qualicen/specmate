package com.specmate.common;

import java.util.UUID;

public class UUIDUtil {
	public static String generateUUID() {
		UUID uuid = UUID.randomUUID();
        return uuid.toString();
	}

}
