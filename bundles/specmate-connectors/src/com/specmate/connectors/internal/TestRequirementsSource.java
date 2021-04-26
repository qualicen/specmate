package com.specmate.connectors.internal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import com.specmate.common.exception.SpecmateException;
import com.specmate.connectors.api.IConnector;
import com.specmate.connectors.api.IProject;
import com.specmate.model.base.BaseFactory;
import com.specmate.model.base.IContainer;
import com.specmate.model.requirements.Requirement;
import com.specmate.model.requirements.RequirementsFactory;

@Component(immediate = true)
public class TestRequirementsSource implements IConnector {

	private IContainer folder;

	@Override
	public Collection<Requirement> getRequirements() {
		List<Requirement> requirements = new LinkedList<>();
		for (int i = 1; i < 1000; i++) {
			Requirement r = RequirementsFactory.eINSTANCE.createRequirement();
			r.setName("Requirement" + i);
			r.setDescription("Requirement" + i);
			r.setExtId(Integer.toString(i));
			r.setId("req" + Integer.toString(i));
			requirements.add(r);
		}
		return requirements;
	}

	@Override
	public String getId() {
		return "test-source";
	}

	@Override
	public IContainer getContainerForRequirement(Requirement requirement) throws SpecmateException {
		if (folder == null) {
			folder = BaseFactory.eINSTANCE.createFolder();
			folder.setId("test-sorce-folder");
		}
		return folder;
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	public void setProject(IProject project) {
		// ignored
	}

	@Override
	public Requirement getRequirementById(String id) throws com.specmate.common.exception.SpecmateException {
		return null;
	}

	@Override
	public Set<IProject> authenticate(String username, String password)
			throws com.specmate.common.exception.SpecmateException {
		return null;
	}

}