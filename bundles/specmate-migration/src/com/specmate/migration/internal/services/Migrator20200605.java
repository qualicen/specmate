package com.specmate.migration.internal.services;

import java.sql.Connection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.dbprovider.api.IDBProvider;
import com.specmate.dbprovider.api.migration.IAttributeToSQLMapper;
import com.specmate.dbprovider.api.migration.IObjectToSQLMapper;
import com.specmate.migration.api.IMigrator;

@Component(property = "sourceVersion=20200605", service = IMigrator.class)
public class Migrator20200605 implements IMigrator {
	private IDBProvider dbProvider;

	@Override
	public String getSourceVersion() {
		return "20200605";
	}

	@Override
	public String getTargetVersion() {
		return "20200921";
	}

	@Override
	public void migrate(Connection connection) throws SpecmateException {
		addCEGLinkedNode();
		addCEGNodeAttributes();
	}

	public void addCEGLinkedNode() throws SpecmateException {
		String objectName = "CEGLinkedNode";
		String packageName = "model/requirements";
		IObjectToSQLMapper oMapper = dbProvider.getObjectToSQLMapper(packageName, getSourceVersion(),
				getTargetVersion());

		oMapper.newObject(objectName);

		// Add attributes
		IAttributeToSQLMapper aMapper = dbProvider.getAttributeToSQLMapper(packageName, getSourceVersion(),
				getTargetVersion());
		aMapper.migrateNewStringAttribute(objectName, "id", "", true);
		aMapper.migrateNewStringAttribute(objectName, "name", "", true);
		aMapper.migrateNewStringAttribute(objectName, "description", "", true);
		aMapper.migrateNewBooleanAttribute(objectName, "recycled", false, true);
		aMapper.migrateNewBooleanAttribute(objectName, "hasRecycledChildren", false, true);
		aMapper.migrateNewObjectReferenceNtoM(objectName, "contents", true, "model/base/IContainer");
		aMapper.migrateNewObjectReferenceNtoM(objectName, "tracesTo", true, "model/base/ITracingElement");
		aMapper.migrateNewObjectReferenceNtoM(objectName, "tracesFrom", true, "model/base/ITracingElement");
		aMapper.migrateNewDoubleAttribute(objectName, "x", 0.0, true);
		aMapper.migrateNewDoubleAttribute(objectName, "y", 0.0, true);
		aMapper.migrateNewDoubleAttribute(objectName, "width", 0.0, true);
		aMapper.migrateNewDoubleAttribute(objectName, "height", 0.0, true);
		aMapper.migrateNewObjectReferenceNtoM(objectName, "outgoingConnections", true, "model/base/IModelNode");
		aMapper.migrateNewObjectReferenceNtoM(objectName, "incomingConnections", true, "model/base/IModelNode");
		aMapper.migrateNewIntegerAttribute(objectName, "type", 0, true);
		aMapper.migrateNewStringAttribute(objectName, "variable", "", true);
		aMapper.migrateNewStringAttribute(objectName, "condition", "", true);
		aMapper.migrateNewObjectReferenceOneToN(objectName, "linkTo", false);
	}

	public void addCEGNodeAttributes() throws SpecmateException {
		String objectName = "CEGNode";
		String packageName = "model/requirements";

		// Add attributes
		IAttributeToSQLMapper aMapper = dbProvider.getAttributeToSQLMapper(packageName, getSourceVersion(),
				getTargetVersion());
		aMapper.migrateNewObjectReferenceNtoM(objectName, "linksFrom");
	}

	@Reference
	public void setDBProvider(IDBProvider dbProvider) {
		this.dbProvider = dbProvider;
	}

}