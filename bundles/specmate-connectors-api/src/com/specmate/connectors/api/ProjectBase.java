package com.specmate.connectors.api;

import com.specmate.model.auth.AuthFactory;
import com.specmate.model.auth.AuthProject;

public abstract class ProjectBase implements IProject {

	protected String oauthUrl;
	
	@Override
	public AuthProject getAuthProject() {
		AuthProject authProject = AuthFactory.eINSTANCE.createAuthProject();
		authProject.setName(this.getID());
		authProject.setOauthUrl(oauthUrl);
		return authProject;
	}

}
