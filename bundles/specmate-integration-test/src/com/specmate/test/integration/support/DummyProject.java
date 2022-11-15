package com.specmate.test.integration.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.specmate.common.exception.SpecmateException;
import com.specmate.connectors.api.ConnectorBase;
import com.specmate.connectors.api.IConnector;
import com.specmate.connectors.api.IProject;
import com.specmate.export.api.ExporterBase;
import com.specmate.model.base.IContainer;
import com.specmate.model.export.Export;
import com.specmate.model.requirements.Requirement;

public class DummyProject implements IProject {
	private String projectId;

	public DummyProject(String projectId) {
		this.projectId = projectId;
	}

	@Override
	public String getID() {
		return projectId;
	}

	@Override
	public IConnector getConnector() {
		ConnectorBase conn = new ConnectorBase() {

			@Override
			public Collection<Requirement> getRequirements() throws SpecmateException {
				return null;
			}

			@Override
			public String getId() {
				return null;
			}

			@Override
			public IContainer getContainerForRequirement(Requirement requirement) throws SpecmateException {
				return null;
			}

			@Override
			public Set<IProject> authenticate(String username, String password) throws SpecmateException {
				return new HashSet<IProject>(Arrays.asList(getProject()));
			}

			@Override
			public Requirement getRequirementById(String id) throws SpecmateException {
				return null;
			}
		};
		conn.setProject(this);
		return conn;
	}

	@Override
	public ExporterBase getExporter() {
		return new ExporterBase("dummy") {

			@Override
			public boolean canExportTestProcedure() {
				return false;
			}

			@Override
			public boolean canExportTestSpecification() {
				return false;
			}

			@Override
			public Optional<Export> export(Object object) throws SpecmateException {
				return null;
			}

			@Override
			public boolean isAuthorizedToExport(String username, String password) {
				return false;
			}
		};
	}

}
