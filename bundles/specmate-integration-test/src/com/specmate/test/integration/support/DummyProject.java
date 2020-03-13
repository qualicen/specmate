package com.specmate.test.integration.support;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.specmate.common.exception.SpecmateException;
import com.specmate.connectors.api.IProject;
import com.specmate.connectors.api.IRequirementsSource;
import com.specmate.export.api.ExporterBase;
import com.specmate.model.base.IContainer;
import com.specmate.model.requirements.Requirement;
import com.specmate.model.export.Export;

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
	public IRequirementsSource getConnector() {
		return new IRequirementsSource() {

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
			public boolean authenticate(String username, String password) throws SpecmateException {
				return true;
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
