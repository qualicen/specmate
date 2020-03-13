package com.specmate.testspecification.internal.generators;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.specmate.model.processes.ProcessConnection;

@SuppressWarnings("serial")
public class ProcessWeightedGraphEdge extends DefaultWeightedEdge {
	private ProcessConnection connection;

	public ProcessConnection getConnection() {
		return connection;
	}

	public void setConnection(ProcessConnection connection) {
		this.connection = connection;
	}
}
