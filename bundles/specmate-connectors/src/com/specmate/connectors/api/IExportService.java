package com.specmate.connectors.api;

import org.eclipse.emf.ecore.EObject;

import com.specmate.common.exception.SpecmateException;

public interface IExportService {

	void export(EObject exportTarget) throws SpecmateException;

	boolean isAuthorizedToExport(String username, String password);
	
	boolean canExportTestProceure();
	
	boolean canExportTextSpecification();
}
