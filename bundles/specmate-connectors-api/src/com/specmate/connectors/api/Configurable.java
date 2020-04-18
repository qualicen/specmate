package com.specmate.connectors.api;

import java.util.Dictionary;
import java.util.Hashtable;

public class Configurable {

	private String pid;
	private Dictionary<String, Object> config;

	public Dictionary<String, Object> getConfig() {
		return config;
	}

	public void setConfig(Hashtable<String, Object> configTable) {
		config = configTable;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void addConfigValue(String key, Object value) {
		config.put(key, value);
	}

	public Object getConfigValue(String key) {
		return config.get(key);
	}

}
