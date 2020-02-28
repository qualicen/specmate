package com.specmate.dummydata;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import com.specmate.common.exception.SpecmateException;
import com.specmate.connectors.api.IProject;
import com.specmate.connectors.api.IRequirementsSource;
import com.specmate.export.api.ExporterBase;
import com.specmate.model.base.IContainer;
import com.specmate.model.requirements.Requirement;
import com.specmate.model.export.Export;

/**
 * A project definition for the test-data that authorizes every user.
 *
 * @author junkerm
 */
@Component(immediate = true)
public class DummyProject implements IProject {

	/* package */ static final String TEST_DATA_PROJECT = "test-data";

	@Override
	public String getID() {
		return TEST_DATA_PROJECT;
	}

	/** Returns a connector that does nothing and authorizes every user. */
	@Override
	public IRequirementsSource getConnector() {
		return new IRequirementsSource() {

			@Override
			public Collection<Requirement> getRequirements() throws SpecmateException {
				return null;
			}

			@Override
			public String getId() {
				return TEST_DATA_PROJECT;
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
			public boolean isAuthorizedToExport(String username, String password) {
				return false;
			}

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
		};
	}

	@Override
	public List<String> getLibraryFolders() {
		return null;
	}

}
