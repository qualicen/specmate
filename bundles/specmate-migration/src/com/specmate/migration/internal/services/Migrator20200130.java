package com.specmate.migration.internal.services;

import java.sql.Connection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.dbprovider.api.IDBProvider;
import com.specmate.dbprovider.api.migration.IAttributeToSQLMapper;
import com.specmate.dbprovider.api.migration.IObjectToSQLMapper;
import com.specmate.migration.api.IMigrator;

@Component(property = "sourceVersion=20200130", service = IMigrator.class)
public class Migrator20200130 implements IMigrator {

	private IDBProvider dbProvider;

	@Override
	public String getSourceVersion() {
		return "20200130";
	}

	@Override
	public String getTargetVersion() {
		return "20200228";
	}

	@Override
	public void migrate(Connection connection) throws SpecmateException {
		migrateExportObject();
		migrateUserSessionDeletedFlag();
	}

	private void migrateUserSessionDeletedFlag() throws SpecmateException {
		IAttributeToSQLMapper aMapper = dbProvider.getAttributeToSQLMapper("model/user", getSourceVersion(),
				getTargetVersion());
		aMapper.migrateNewBooleanAttribute("usersession", "isdeleted", true);
	}

	private void migrateExportObject() throws SpecmateException {
		IObjectToSQLMapper oMapper = dbProvider.getObjectToSQLMapper("model/export", getSourceVersion(),
				getTargetVersion());
		oMapper.newObject("Export");

		IAttributeToSQLMapper aMapper = dbProvider.getAttributeToSQLMapper("model/export", getSourceVersion(),
				getTargetVersion());
		aMapper.migrateNewStringAttribute("export", "type", "");
		aMapper.migrateNewStringAttribute("export", "content", "");
		aMapper.migrateNewStringAttribute("export", "name", "");
	}

	@Reference
	public void setDBProvider(IDBProvider dbProvider) {
		this.dbProvider = dbProvider;
	}

}
