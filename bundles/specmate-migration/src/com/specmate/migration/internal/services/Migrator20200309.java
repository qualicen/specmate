package com.specmate.migration.internal.services;

import java.sql.Connection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.dbprovider.api.IDBProvider;
import com.specmate.dbprovider.api.migration.IAttributeToSQLMapper;
import com.specmate.dbprovider.api.migration.IObjectToSQLMapper;
import com.specmate.migration.api.IMigrator;
import com.specmate.model.base.BasePackage;

@Component(property = "sourceVersion=20200309", service = IMigrator.class)
public class Migrator20200309 implements IMigrator {
	private IDBProvider dbProvider;

	@Override
	public String getSourceVersion() {
		return "20200309";
	}

	@Override
	public String getTargetVersion() {
		return "20200605";
	}

	@Override
	public void migrate(Connection connection) throws SpecmateException {
		String objectName = "ModelImage";
		String packageName = "model/base";
		IObjectToSQLMapper oMapper = dbProvider.getObjectToSQLMapper(packageName, getSourceVersion(),
				getTargetVersion());

		oMapper.newObject(objectName);

		// Add attributes
		IAttributeToSQLMapper aMapper = dbProvider.getAttributeToSQLMapper(packageName, getSourceVersion(),
				getTargetVersion());
		aMapper.migrateNewStringAttribute(objectName, "id", "");
		aMapper.migrateNewStringAttribute(objectName, "name", "");
		aMapper.migrateNewStringAttribute(objectName, "description", "");
		aMapper.migrateNewBooleanAttribute(objectName, "recycled", false);
		aMapper.migrateNewBooleanAttribute(objectName, "hasRecycledChildren", false);
		aMapper.migrateNewStringAttribute(objectName, "imageData", "");
	}

	@Reference
	public void setDBProvider(IDBProvider dbProvider) {
		this.dbProvider = dbProvider;
	}

}