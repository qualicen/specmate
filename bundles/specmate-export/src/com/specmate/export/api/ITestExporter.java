package com.specmate.export.api;

import java.util.Optional;

import com.specmate.model.testspecification.TestSpecificationSkeleton;

public interface ITestExporter {

	/** getter for language */
	String getLanguage();

	/** Generates an export for the test specification */
	Optional<TestSpecificationSkeleton> export(Object object);

	/** Signals that this exporter can export test specifications */
	boolean canExportTestSpecification();

	/** Signals that this exporter can export test procedures */
	boolean canExportTestProcedure();

}