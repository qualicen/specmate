package com.specmate.export.api;

public abstract class TestExporterBase implements ITestExporter {

	private String language;
	private String projectName;

	public TestExporterBase(String language, String projectName) {
		super();
		this.language = language;
		this.projectName = projectName;
	}

	public TestExporterBase(String language) {
		this(language, null);
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public void setProjectName(String project) {
		projectName = project;
	}

	@Override
	public String getProjectName() {
		return projectName;
	}

}
