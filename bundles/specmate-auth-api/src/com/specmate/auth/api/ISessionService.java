package com.specmate.auth.api;

import java.util.List;

import com.specmate.common.exception.SpecmateException;
import com.specmate.usermodel.AccessRights;
import com.specmate.usermodel.UserSession;

public interface ISessionService {
	public UserSession create(AccessRights alm, AccessRights ppm, String userName, String password, String projectName)
			throws SpecmateException;

	public UserSession create();

	public boolean isExpired(String token) throws SpecmateException;

	public boolean isAuthorized(String token, String path) throws SpecmateException;

	public void refresh(String token) throws SpecmateException;

	public String getUserName(String token) throws SpecmateException;

	public AccessRights getSourceAccessRights(String token) throws SpecmateException;

	public AccessRights getTargetAccessRights(String token) throws SpecmateException;

	public void delete(String token) throws SpecmateException;

	public void registerSessionListener(ISessionListener listener);

	public void removeSessionListener(ISessionListener listener);

	public List<String> getExporters(String userToken) throws SpecmateException;
}