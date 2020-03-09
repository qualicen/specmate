package com.specmate.migration.internal.services;

import java.sql.Connection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.dbprovider.api.IDBProvider;
import com.specmate.dbprovider.api.migration.IAttributeToSQLMapper;
import com.specmate.migration.api.IMigrator;

@Component(property = "sourceVersion=20200228", service = IMigrator.class)
public class Migrator20200228 implements IMigrator {

	private IDBProvider dbProvider;

	@Override
	public String getSourceVersion() {
		return "20200228";
	}

	@Override
	public String getTargetVersion() {
		return "20200309";
	}

	@Override
	public void migrate(Connection connection) throws SpecmateException {
		createIsRecycledAttribute("model/base", "Folder");
		createIsRecycledAttribute("model/base", "IModelConnection");
		createIsRecycledAttribute("model/base", "IModelNode");
		createIsRecycledAttribute("model/requirements", "Requirement");
		createIsRecycledAttribute("model/requirements", "CEGModel");
		createIsRecycledAttribute("model/requirements", "CEGNode");
		createIsRecycledAttribute("model/requirements", "CEGConnection");
		createIsRecycledAttribute("model/testspecification", "TestSpecification");
		createIsRecycledAttribute("model/testspecification", "TestParameter");
		createIsRecycledAttribute("model/testspecification", "TestCase");
		createIsRecycledAttribute("model/testspecification", "ParameterAssignment");
		createIsRecycledAttribute("model/testspecification", "TestProcedure");
		createIsRecycledAttribute("model/testspecification", "TestStep");
		createIsRecycledAttribute("model/processes", "Process");
		createIsRecycledAttribute("model/processes", "ProcessStep");
		createIsRecycledAttribute("model/processes", "ProcessDecision");
		createIsRecycledAttribute("model/processes", "ProcessConnection");
		createIsRecycledAttribute("model/processes", "ProcessStart");
		createIsRecycledAttribute("model/processes", "ProcessEnd");
	}

	private void createIsRecycledAttribute(String packageName, String objectName) throws SpecmateException {
		IAttributeToSQLMapper aMapper = dbProvider.getAttributeToSQLMapper(packageName, getSourceVersion(),
				getTargetVersion());
		aMapper.migrateNewBooleanAttribute(objectName, "isRecycled", false);
		aMapper.migrateNewBooleanAttribute(objectName, "hasRecycledChildren", false);
	}

	@Reference
	public void setDBProvider(IDBProvider dbProvider) {
		this.dbProvider = dbProvider;
	}

}