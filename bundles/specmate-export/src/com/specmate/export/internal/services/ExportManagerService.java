package com.specmate.export.internal.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.iterators.IteratorChain;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import com.specmate.auth.api.ISessionListener;
import com.specmate.auth.api.ISessionService;
import com.specmate.common.exception.SpecmateAuthorizationException;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateValidationException;
import com.specmate.export.api.IExporter;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.export.Export;
import com.specmate.usermodel.UserSession;

/**
 * Service that manages export services and allows accessing them via a single
 * interface.
 */
@Component(immediate = true, service = ExportManagerService.class)
public class ExportManagerService {

	/** Collects all exporters that can export test specifications */
	private Map<String, IExporter> testSpecificationExporters = new HashMap<String, IExporter>();

	/** Collects all exporters that can export test specifications */
	private Map<String, IExporter> testProcedureExporters = new HashMap<String, IExporter>();

	/** Reference to the logging service */
	private LogService logService;

	/** Reference to the session service */
	private ISessionService sessionService;

	@Activate
	public void activate() {
		sessionService.registerSessionListener(new ISessionListener() {

			@Override
			public void sessionDeleted(UserSession session) {
				// nothing to do
			}

			/**
			 * When a new session is created, checks for which exporters the user is
			 * authorized and saves this in the session.
			 */
			@Override
			public void sessionCreated(UserSession session, String userName, String password) {
				Set<String> allowedExporters = new HashSet<String>();
				IteratorChain<IExporter> allExporters = new IteratorChain<IExporter>(
						testProcedureExporters.values().iterator(), testSpecificationExporters.values().iterator());
				while (allExporters.hasNext()) {
					IExporter exporter = allExporters.next();
					if (exporter.getProjectName() == null
							|| Pattern.matches(session.getAllowedPathPattern(), exporter.getProjectName())) {
						if (exporter.isAuthorizedToExport(userName, password)) {
							allowedExporters.add(exporter.getType().toLowerCase());
						}
					}
				}
				session.getExporters().addAll(allowedExporters);
			}
		});
	}

	/**
	 * Exports the given object with an exporter that fits the given language.
	 *
	 * @param object    The object to export
	 * @param language  Specifices the exporter to use
	 * @param userToken The user token of the user who is requesting the export
	 * @return
	 * @throws SpecmateException
	 */
	public Optional<Export> export(Object object, String language, String userToken)
			throws SpecmateException {
		List<String> allowedExporters = sessionService.getExporters(userToken);
		if (!allowedExporters.contains(language.toLowerCase())) {
			throw new SpecmateAuthorizationException("Export to " + language + " is not allowed.");
		}
		String languageKey = language.toLowerCase();
		IExporter exporter = null;
		if (object instanceof TestSpecification) {
			exporter = testSpecificationExporters.get(languageKey);
		} else if (object instanceof TestProcedure) {
			exporter = testProcedureExporters.get(languageKey);
		}
		if (exporter == null) {
			throw new SpecmateValidationException("Exporter for language " + language + " does not exist.");
		}
		return exporter.export(object);
	}

	/** Returns a list of exporters for which a user is authorized */
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
			return testSpecificationExporters.entrySet().stream().filter(e -> _allowedExporters.contains(e.getKey()))
					.map(e -> e.getValue().getType()).collect(Collectors.toList());
		}
		if (object instanceof TestProcedure) {
			return testProcedureExporters.entrySet().stream().filter(e -> _allowedExporters.contains(e.getKey()))
					.map(e -> e.getValue().getType()).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addTestSpecificationExporter(IExporter exporter) {
		if (exporter.canExportTestProcedure()) {
			addToExporterCollection(exporter, testProcedureExporters);
		}
		if (exporter.canExportTestSpecification()) {
			addToExporterCollection(exporter, testSpecificationExporters);
		}
	}

	public void removeTestSpecificationExporter(IExporter exporter) {
		String languageKey = exporter.getType().toLowerCase();
		IExporter existing = testSpecificationExporters.get(languageKey);
		if (existing == exporter) {
			testSpecificationExporters.remove(languageKey);
		}
	}

	private void addToExporterCollection(IExporter exporter, Map<String, IExporter> exporterMap) {
		String languageKey = exporter.getType().toLowerCase();
		if (exporterMap.containsKey(languageKey)) {
			logService.log(LogService.LOG_WARNING, "Test exporter for langugae " + exporter.getType()
					+ " already exists. Ignoring: " + exporter.getClass().getName());
		}
		exporterMap.put(languageKey, exporter);
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
