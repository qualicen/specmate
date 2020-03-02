package com.specmate.export.api;

/**
 * Base class for test exporters
 * 
 * @author junkerm
 *
 */
public abstract class ExporterBase implements IExporter {

	/** The export type, e.g. the format or system where to export to */
	private String type;

	/** The name of the project from which an export is requested */
	private String projectName;

	public ExporterBase(String type, String projectName) {
		super();
		this.type = type;
		this.projectName = projectName;
	}

	public ExporterBase(String type) {
		this(type, null);
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
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
