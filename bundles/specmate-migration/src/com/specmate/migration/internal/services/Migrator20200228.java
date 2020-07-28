package com.specmate.migration.internal.services;

import java.sql.Connection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.dbprovider.api.IDBProvider;
import com.specmate.dbprovider.api.migration.IAttributeToSQLMapper;
import com.specmate.migration.api.IMigrator;
import com.specmate.model.base.BasePackage;

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
		createRecycledAttributes("model/base", "Folder");
		createRecycledAttributes("model/base", "IModelConnection");
		createRecycledAttributes("model/base", "IModelNode");
		createRecycledAttributes("model/requirements", "Requirement");
		createRecycledAttributes("model/requirements", "CEGModel");
		createRecycledAttributes("model/requirements", "CEGNode");
		createRecycledAttributes("model/requirements", "CEGConnection");
		createRecycledAttributes("model/testspecification", "TestSpecification");
		createRecycledAttributes("model/testspecification", "TestParameter");
		createRecycledAttributes("model/testspecification", "TestCase");
		createRecycledAttributes("model/testspecification", "ParameterAssignment");
		createRecycledAttributes("model/testspecification", "TestProcedure");
		createRecycledAttributes("model/testspecification", "TestStep");
		createRecycledAttributes("model/processes", "Process");
		createRecycledAttributes("model/processes", "ProcessStep");
		createRecycledAttributes("model/processes", "ProcessDecision");
		createRecycledAttributes("model/processes", "ProcessConnection");
		createRecycledAttributes("model/processes", "ProcessStart");
		createRecycledAttributes("model/processes", "ProcessEnd");
	}

	private void createRecycledAttributes(String packageName, String objectName) throws SpecmateException {
		IAttributeToSQLMapper aMapper = dbProvider.getAttributeToSQLMapper(packageName, getSourceVersion(),
				getTargetVersion());
		aMapper.migrateNewBooleanAttribute(objectName, BasePackage.Literals.IRECYCLED__RECYCLED.getName(), false);
		aMapper.migrateNewBooleanAttribute(objectName, BasePackage.Literals.IRECYCLED__HAS_RECYCLED_CHILDREN.getName(),
				false);
		aMapper.adaptContainingFeatureIndex(objectName, 2);
	}

	@Reference
	public void setDBProvider(IDBProvider dbProvider) {
		this.dbProvider = dbProvider;
	}

}