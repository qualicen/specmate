package com.specmate.migration.internal.services;

import java.sql.Connection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.dbprovider.api.IDBProvider;
import com.specmate.dbprovider.api.migration.IAttributeToSQLMapper;
import com.specmate.migration.api.IMigrator;

@Component(property = "sourceVersion=20190125", service = IMigrator.class)
public class Migrator20190125 implements IMigrator {

	private IDBProvider dbProvider;

	@Override
	public String getSourceVersion() {
		return "20190125";
	}

	@Override
	public String getTargetVersion() {
		return "20190902";
	}

	@Override
	public void migrate(Connection connection) throws SpecmateException {
		createWidthHeightAttributes("model/base", "IModelNode");
		createWidthHeightAttributes("model/requirements", "CEGNode");
		createWidthHeightAttributes("model/processes", "ProcessDecision");
		createWidthHeightAttributes("model/processes", "ProcessEnd");
		createWidthHeightAttributes("model/processes", "ProcessStart");
		createWidthHeightAttributes("model/processes", "ProcessStep");
	}

	private void createWidthHeightAttributes(String packageName, String objectName) throws SpecmateException {

		IAttributeToSQLMapper aMapper = dbProvider.getAttributeToSQLMapper(packageName, getSourceVersion(),
				getTargetVersion());
		aMapper.migrateNewDoubleAttribute(objectName, "width", 0.0);
		aMapper.migrateNewDoubleAttribute(objectName, "height", 0.0);
	}

	@Reference
	public void setDBProvider(IDBProvider dbProvider) {
		this.dbProvider = dbProvider;
	}

}
