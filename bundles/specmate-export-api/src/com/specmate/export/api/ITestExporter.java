package com.specmate.export.api;

import java.util.Optional;

import com.specmate.common.exception.SpecmateException;
import com.specmate.model.testspecification.TestSpecificationSkeleton;

public interface ITestExporter {

	/** getter for language */
	String getLanguage();

	/**
	 * Generates an export for the test specification
	 *
	 * @throws SpecmateException
	 */
	Optional<TestSpecificationSkeleton> export(Object object) throws SpecmateException;

	/** Signals that this exporter can export test specifications */
	boolean canExportTestSpecification();

	/** Signals that this exporter can export test procedures */
	boolean canExportTestProcedure();

	boolean isAuthorizedToExport(String username, String password);

	void setProjectName(String project);

	String getProjectName();

}