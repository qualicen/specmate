package com.specmate.emfrest.crud;

import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.auth.api.IAuthenticationService;
import com.specmate.common.exception.SpecmateException;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.rest.RestResult;

@Component(immediate = true, service = IRestService.class)
public class RecycleService extends RestServiceBase {
	private IAuthenticationService authService;

	@Override
	public String getServiceName() {
		return "recycle";
	}

	@Override
	public boolean canPost(Object target, Object object) {
		return (target instanceof EObject);
	}

	@Override
	public RestResult<?> post(Object target, Object child, String token) throws SpecmateException {
		return CrudUtil.recycle(target, authService.getUserName(token));
	}
	
	@Reference
	public void setAuthService(IAuthenticationService authService) {
		this.authService = authService;
	}
}


