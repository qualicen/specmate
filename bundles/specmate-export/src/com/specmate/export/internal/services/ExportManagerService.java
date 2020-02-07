package com.specmate.export.internal.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import com.specmate.auth.api.ISessionListener;
import com.specmate.auth.api.ISessionService;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateValidationException;
import com.specmate.export.api.ITestExporter;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.testspecification.TestSpecificationSkeleton;
import com.specmate.usermodel.UserSession;

@Component(immediate = true, service = ExportManagerService.class)
public class ExportManagerService {

	private Map<String, ITestExporter> testSpecificationExporters = new HashMap<String, ITestExporter>();
	private Map<String, ITestExporter> testProcedureExporters = new HashMap<String, ITestExporter>();
	private LogService logService;
	private ISessionService sessionService;

	@Activate
	public void activate() {
		sessionService.registerSessionListener(new ISessionListener() {

			@Override
			public void sessionDeleted(UserSession session) {
				// nothing to do
			}

			@Override
			public void sessionCreated(UserSession session, String userName, String password) {
				List<String> allowedExporters = new ArrayList<String>();
				for (ITestExporter exporter : testSpecificationExporters.values()) {
					if (exporter.getProject() == null
							|| Pattern.matches(session.getAllowedPathPattern(), exporter.getProject().getID())) {
						if (exporter.isAuthorizedToExport(userName, password)) {
							allowedExporters.add(exporter.getLanguage());
						}
					}
				}
				session.getExporters().addAll(allowedExporters);
			}
		});
	}

	public Optional<TestSpecificationSkeleton> export(Object object, String language) throws SpecmateException {

		ITestExporter exporter = null;
		if (object instanceof TestSpecification) {
			exporter = testSpecificationExporters.get(language);
		} else if (object instanceof TestProcedure) {
			exporter = testProcedureExporters.get(language);
		}
		if (exporter == null) {
			throw new SpecmateValidationException("Generator for langauge " + language + " does not exist.");
		}
		return exporter.export(object);
	}

	public List<String> getExporters(Object object, String userToken) {
		List<String> allowedExporters;
		try {
			allowedExporters = sessionService.getExporters(userToken);
		} catch (SpecmateException e) {
			logService.log(LogService.LOG_ERROR, "Exception occured when retrieving allowed exporters.", e);
			allowedExporters = new ArrayList<>();
		}
		final List<String> _allowedExporters = allowedExporters;
		if (object instanceof TestSpecification) {
			return testSpecificationExporters.keySet().stream().filter(e -> _allowedExporters.contains(e))
					.collect(Collectors.toList());
		}
		if (object instanceof TestProcedure) {
			return testProcedureExporters.keySet().stream().filter(e -> _allowedExporters.contains(e))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addTestSpecificationExporter(ITestExporter exporter) {
		if (exporter.canExportTestProcedure()) {
			addToExporterCollection(exporter, testProcedureExporters);
		}
		if (exporter.canExportTestSpecification()) {
			addToExporterCollection(exporter, testSpecificationExporters);
		}
	}

	public void removeTestSpecificationExporter(ITestExporter exporter) {
		ITestExporter existing = testSpecificationExporters.get(exporter.getLanguage());
		if (existing == exporter) {
			testSpecificationExporters.remove(exporter.getLanguage());
		}
	}

	private void addToExporterCollection(ITestExporter exporter, Map<String, ITestExporter> exporterMap) {
		if (exporterMap.containsKey(exporter.getLanguage())) {
			logService.log(LogService.LOG_WARNING, "Test exporter for langugae " + exporter.getLanguage()
					+ " already exists. Ignoring: " + exporter.getClass().getName());
		}
		exporterMap.put(exporter.getLanguage(), exporter);
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	@Reference
	public void setSessionService(ISessionService sessionService) {
		this.sessionService = sessionService;
	}

}
