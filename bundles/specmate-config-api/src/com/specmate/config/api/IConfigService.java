package com.specmate.config.api;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface IConfigService {
	public String getConfigurationProperty(String key);

	/**
	 * Retrieves the value of a config entry with the given key, or the default
	 * value if the key was not found
	 */
	public String getConfigurationProperty(String key, String defaultValue);

	/**
	 * Retreives the value of a config entry with the given key as integer, or null.
	 */
	public Integer getConfigurationPropertyInt(String key);

	/**
	 * Retrieves the value of a config entry with the given key as integer, or the
	 * default value if the key was not found
	 */
	public Integer getConfigurationPropertyInt(String key, int defaultValue);

	/** Retreives all configured properties with a given prefix */
	Set<Entry<Object, Object>> getConfigurationProperties(String prefix);

	/** Adds the given entries to the config. */
	public void addUpdateConfigurationProperties(Map<String, String> entries);

	/** Adds a single entry to the config. */
	public void addUpdateConfigurationProperty(String key, String value);

	/** Retreives the value of a config property as string array. */
	String[] getConfigurationPropertyArray(String key);

	/**
	 * Retreives the value of a config property as string array, or the devault
	 * value of the key was not found.
	 */
	String[] getConfigurationPropertyArray(String key, String[] defaultValue);
}
