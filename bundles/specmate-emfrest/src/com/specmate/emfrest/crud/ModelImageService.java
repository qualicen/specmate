package com.specmate.emfrest.crud;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;

import com.specmate.common.exception.SpecmateException;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.model.base.ModelImage;
import com.specmate.model.processes.Process;
import com.specmate.model.requirements.CEGModel;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.rest.RestResult;

@Component(immediate = true, service = IRestService.class)
public class ModelImageService extends RestServiceBase {

	@Override
	public String getServiceName() {
		return "listModelImage";
	}

	@Override
	public boolean canGet(Object target) {
		return (target instanceof CEGModel || target instanceof Process);
	}

	@Override
	public RestResult<?> get(Object target, MultivaluedMap<String, String> queryParams, String token)
			throws SpecmateException {
		List<EObject> children = SpecmateEcoreUtil.getChildren(target);
		List<ModelImage> images = SpecmateEcoreUtil.pickInstancesOf(children, ModelImage.class);
		if (images.size() > 0) {
			return new RestResult<>(Response.Status.OK, images.get(0));
		}
		return new RestResult<>(Response.Status.OK, null);
	}
}
