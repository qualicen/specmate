package com.specmate.export.internal.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateValidationException;
import com.specmate.export.api.IExporter;
import com.specmate.model.export.Export;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.usermodel.UserSession;

/**
 * Service that manages export services and allows accessing them via a single
 * interface.
 */
@Component(immediate = true, service = ExportManagerService.class)
public class ExportManagerService {

	/** Collects all exporters that can export test specifications */
	private List<IExporter> testSpecificationExporters = new ArrayList<IExporter>();

	/** Collects all exporters that can export test specifications */
	private List<IExporter> testProcedureExporters = new ArrayList<IExporter>();

	/** Reference to the logging service */
	private LogService logService;

	/** Reference to the session service */
	private ISessionService sessionService;

	/** Map to store allowed exporters for users */
	private Map<String, Set<IExporter>> allowedExportersMap = new HashMap<>();

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
				fillAllowedExporters(session, userName, password);
			}

		});
	}

	private void fillAllowedExporters(UserSession session, String userName, String password) {
		Set<IExporter> allowedExporters;
		try {
			allowedExporters = determineAllowedExporters(session, userName, password);
			allowedExportersMap.put(session.getId(), allowedExporters);
		} catch (SpecmateException e) {
			logService.log(LogService.LOG_WARNING, "Could not determine the list of allowed exporters.");
		}
	}

	private Set<IExporter> determineAllowedExporters(UserSession session, String userName, String password)
			throws SpecmateException {
		Set<IExporter> allowedExporters = new HashSet<IExporter>();
		IteratorChain<IExporter> allExporters = new IteratorChain<IExporter>(testProcedureExporters.iterator(),
				testSpecificationExporters.iterator());
		while (allExporters.hasNext()) {
			IExporter exporter = allExporters.next();
			if (exporter.getProjectName() == null
					|| sessionService.isAuthorizedProject(session, exporter.getProjectName())) {
				if (exporter.isAuthorizedToExport(userName, password)) {
					allowedExporters.add(exporter);
				}
			}
		}
		return allowedExporters;
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
	public Optional<Export> export(Object object, String language, String userToken) throws SpecmateException {
		Set<IExporter> allowedExporters = allowedExportersMap.get(userToken);
		if (allowedExporters == null) {
			return Optional.empty();
		}
		String languageKey = language.toLowerCase();
		Optional<IExporter> exporter = null;
		if (object instanceof TestSpecification) {
			exporter = selectExporter(testSpecificationExporters, allowedExporters, languageKey);
		} else if (object instanceof TestProcedure) {
			exporter = selectExporter(testProcedureExporters, allowedExporters, languageKey);
		}
		if (exporter == null || exporter.isEmpty()) {
			throw new SpecmateValidationException("Exporter for language " + language + " does not exist.");
		}
		return exporter.get().export(object);
	}

	private Optional<IExporter> selectExporter(List<IExporter> exporterList, Set<IExporter> allowedExporters,
			String languageKey) {
		return exporterList.stream()
				.filter(e -> e.getType().toLowerCase().equals(languageKey) && allowedExporters.contains(e)).findFirst();
	}

	/** Returns a list of exporters for which a user is authorized */
	public List<IExporter> getExporters(Object object, String userToken) {

		Set<IExporter> allowedExporters = allowedExportersMap.get(userToken);
		if (allowedExporters == null) {
			return Collections.emptyList();
		}

		final Set<IExporter> _allowedExporters = allowedExporters;
		if (object instanceof TestSpecification) {
			return testSpecificationExporters.stream().filter(e -> _allowedExporters.contains(e))
					.collect(Collectors.toList());
		}
		if (object instanceof TestProcedure) {
			return testProcedureExporters.stream().filter(e -> _allowedExporters.contains(e))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addTestSpecificationExporter(IExporter exporter) {
		if (exporter.canExportTestProcedure()) {
			testProcedureExporters.add(exporter);
		}
		if (exporter.canExportTestSpecification()) {
			testSpecificationExporters.add(exporter);
		}
	}

	public void removeTestSpecificationExporter(IExporter exporter) {
		testSpecificationExporters.remove(exporter);
		testProcedureExporters.remove(exporter);
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
