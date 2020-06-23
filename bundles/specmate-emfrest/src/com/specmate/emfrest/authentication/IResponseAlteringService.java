package com.specmate.emfrest.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.specmate.rest.RestResult;

public interface IResponseAlteringService {
	public Response getResponse(HttpServletRequest request, RestResult<?> result);
}
