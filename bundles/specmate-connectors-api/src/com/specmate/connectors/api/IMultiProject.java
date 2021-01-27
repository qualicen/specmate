package com.specmate.connectors.api;

public interface IMultiProject {

	/**
	 * @return the id of the multi project
	 */
	String getID();

	/**
	 * @return the defined multi connector for the project, or <code>null</code>.
	 */
	IMultiConnector getConnector();
}
