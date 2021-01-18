package com.specmate.test.integration.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.specmate.common.exception.SpecmateException;
import com.specmate.connectors.api.ConnectorBase;
import com.specmate.connectors.api.IConnector;
import com.specmate.connectors.api.IProject;
import com.specmate.connectors.api.IProjectService;
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
		return new ConnectorBase() {

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
			public Set<IProject> authenticate(String username, String password, IProject project,
					IProjectService projectService) throws SpecmateException {
				return new HashSet<IProject>(Arrays.asList(project));
			}

			@Override
			public Requirement getRequirementById(String id) throws SpecmateException {
				return null;
			}
		};
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

	@Override
	public List<String> getLibraryFolders() {
		return null;
	}
}
