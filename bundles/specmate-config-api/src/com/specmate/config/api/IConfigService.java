package com.specmate.config.api;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface IConfigService {
	public String getConfigurationProperty(String key);

	public String getConfigurationProperty(String key, String defaultValue);

	public Integer getConfigurationPropertyInt(String key);

	public Integer getConfigurationPropertyInt(String key, int defaultValue);

	Set<Entry<Object, Object>> getConfigurationProperties(String prefix);
	
	public void addUpdateConfigurationProperties(Map<String, String> entries);
	
	public void addUpdateConfigurationProperty(String key, String value);

	String[] getConfigurationPropertyArray(String key);
	
	String[] getConfigurationPropertyArray(String key, String[] defaultValue);
}
