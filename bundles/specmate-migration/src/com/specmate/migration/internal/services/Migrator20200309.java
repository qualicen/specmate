package com.specmate.migration.internal.services;

import java.sql.Connection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.dbprovider.api.IDBProvider;
import com.specmate.dbprovider.api.migration.IAttributeToSQLMapper;
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
		createImageReference("model/requirements", "CEGModel");
		createImageReference("model/processes", "Process");
	}

	private void createImageReference(String packageName, String objectName) throws SpecmateException {
		IAttributeToSQLMapper aMapper = dbProvider.getAttributeToSQLMapper(packageName, getSourceVersion(),
				getTargetVersion());
		aMapper.migrateNewObjectReference(objectName, BasePackage.Literals.IMODEL__IMAGE.getName());
	}

	@Reference
	public void setDBProvider(IDBProvider dbProvider) {
		this.dbProvider = dbProvider;
	}

}