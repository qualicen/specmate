package com.specmate.migration.internal.services;

import java.sql.Connection;

import org.osgi.service.component.annotations.Component;

import com.specmate.common.exception.SpecmateException;
import com.specmate.migration.api.IMigrator;

@Component(property = "sourceVersion=20200624", service = IMigrator.class)
public class Migrator20200624 implements IMigrator {

	@Override
	public String getSourceVersion() {
		return "20200309";
	}

	@Override
	public String getTargetVersion() {
		return "20200624";
	}

	@Override
	public void migrate(Connection connection) throws SpecmateException {

	}

}