package com.specmate.connectors.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.osgi.service.log.Logger;

import com.specmate.common.exception.SpecmateException;
import com.specmate.connectors.api.IConnector;
import com.specmate.model.base.BaseFactory;
import com.specmate.model.base.Folder;
import com.specmate.model.base.IContainer;
import com.specmate.model.base.IContentElement;
import com.specmate.model.requirements.Requirement;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.persistency.IChange;
import com.specmate.persistency.ITransaction;

public class ConnectorUtil {

	/** Lock, thus only one sync runs at the same time */
	private final static Lock lock = new ReentrantLock();

	/** How many retrieved requirements to process in one transaction */
	public static final int BATCH_SIZE = 100;

	/** Max size of field */
	private static final int MAX_FIELD_LENGTH = 4000;

	/**
	 * Retrieves requirements from all sources and processes them in batches of
	 * BATCH_SIZE
	 */
	public static void syncConnectors(List<IConnector> connectors, ITransaction transaction, Logger logger) {
		lock.lock();
		try {

			logger.info("Synchronizing connectors");
			Resource resource = transaction.getResource();
			for (IConnector connector : connectors) {
				logger.info("Retrieving requirements from " + connector.getId());
				try {
					Collection<Requirement> requirements = connector.getRequirements();
					if (requirements == null) {
						continue;
					}

					IContainer localRootContainer = getOrCreateLocalRootContainer(resource, connector.getId());
					HashMap<String, EObject> localRequirementsMap = buildLocalRequirementsMap(localRootContainer);

					Requirement[] reqArray = requirements.toArray(new Requirement[0]);
					int greatestUnhandledIndex = 0;
					int maxIndex = requirements.size() - 1;
					while (greatestUnhandledIndex <= maxIndex) {
						int upperIndexExclusive = Math.min(greatestUnhandledIndex + BATCH_SIZE, maxIndex + 1);
						Requirement[] current = Arrays.copyOfRange(reqArray, greatestUnhandledIndex,
								upperIndexExclusive);
						greatestUnhandledIndex = upperIndexExclusive;
						List<Requirement> tosync = Arrays.asList(current);

						doAndCommitTransaction(transaction, localRootContainer, localRequirementsMap, tosync, connector,
								logger);
					}
				} catch (Exception e) {
					logger.error("An error occured synching requirements. Reason: " + e.getMessage(), e);
					transaction.rollback();
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Retrieves a single requirement with a given id
	 */
	public static void syncRequirementById(String id, IConnector source, ITransaction transaction, Logger logger) {
		lock.lock();
		try {
			logger.info("Synchronizing requirement by id");
			Resource resource = transaction.getResource();
			logger.info("Retrieving single requirement from " + source.getId());
			try {
				Requirement requirement = source.getRequirementById(id);
				if (requirement != null) {

					IContainer localRootContainer = getOrCreateLocalRootContainer(resource, source.getId());
					HashMap<String, EObject> localRequirementsMap = buildLocalRequirementsMap(localRootContainer);
					boolean alreadyImported = localRequirementsMap.containsKey(id);
					if (alreadyImported) {
						try {
							transaction.doAndCommit(new IChange<Object>() {
								@Override
								public Object doChange() throws SpecmateException {
									SpecmateEcoreUtil.copyAttributeValues(requirement, localRequirementsMap.get(id),
											false);
									return null;
								}
							});
						} catch (Exception e) {
							logger.error(
									"An error occured while committing synced requirement. Reason:" + e.getMessage(),
									e);
							transaction.rollback();
						}
					} else {
						List<Requirement> tosync = new LinkedList<Requirement>();
						tosync.add(requirement);

						doAndCommitTransaction(transaction, localRootContainer, localRequirementsMap, tosync, source,
								logger);
					}
				}
			} catch (Exception e) {
				logger.error("An error occured synching requirement. Reason: " + e.getMessage(), e);
				transaction.rollback();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Syncs remote requirements with locally available requirements.
	 */
	private static void syncContainers(IContainer localRootContainer, HashMap<String, EObject> localRequirementsMap,
			Collection<Requirement> requirements, IConnector source, Logger logger) {
		// Build hashset (extid -> requirement) for remote requirements
		HashMap<String, EObject> remoteRequirementsMap = new HashMap<>();
		buildExtIdMap(requirements.iterator(), remoteRequirementsMap);

		// add new requirements to local container and all folders on the way
		for (Entry<String, EObject> entry : remoteRequirementsMap.entrySet()) {

			IContainer reqContainer;
			try {
				reqContainer = source.getContainerForRequirement((Requirement) entry.getValue());

			} catch (SpecmateException e) {
				logger.error(e.getMessage());
				continue;
			}

			IContainer foundContainer = getOrCreateLocalSubContainer(localRootContainer, reqContainer, logger);
			if (foundContainer == null) {
				continue;
			}

			Requirement requirementToAdd = (Requirement) entry.getValue();
			boolean valid = ensureValid(requirementToAdd);
			if (!valid) {
				logger.warn("Found invalid requirement with id " + requirementToAdd.getId());
				continue;
			}

			Requirement localRequirement = (Requirement) localRequirementsMap.get(requirementToAdd.getExtId());

			if (localRequirement != null) {
				// Requirement exists
				// --> update attributes
				SpecmateEcoreUtil.copyAttributeValues(requirementToAdd, localRequirement, false);
				// --> if requirement has moved, move to new folder
				foundContainer.getContents().add(localRequirement);
				continue;
			}

			// new requirement: add to folder
			logger.debug("Adding requirement " + requirementToAdd.getId());
			foundContainer.getContents().add(requirementToAdd);
		}
	}

	/** Ensures a requirement is valid */
	private static boolean ensureValid(IContentElement element) {
		if (StringUtils.isEmpty(element.getId())) {
			return false;
		}
		if (StringUtils.isEmpty(element.getName())) {
			element.setName(element.getId());
		}
		if (element.getName().length() > MAX_FIELD_LENGTH) {
			element.setName(element.getName().substring(0, MAX_FIELD_LENGTH - 1));
		}
		element.setName(element.getName().replaceAll("[,\\|;]", " "));
		if (element.getDescription() != null && element.getDescription().length() > MAX_FIELD_LENGTH) {
			element.setDescription(element.getDescription().substring(0, MAX_FIELD_LENGTH - 1));
		}
		return true;
	}

	/**
	 * Retrieves a folder matching reqContainer or creates a new one.
	 *
	 * @param rootContainer
	 * @param reqContainer
	 * @return
	 */
	private static IContainer getOrCreateLocalSubContainer(IContainer rootContainer, IContainer reqContainer,
			Logger logger) {
		IContainer foundContainer = rootContainer;
		if (reqContainer != null) {
			foundContainer = (IContainer) SpecmateEcoreUtil.getEObjectWithId(reqContainer.getId(),
					rootContainer.eContents());
			if (foundContainer == null) {
				logger.debug("Creating new folder " + reqContainer.getName());
				foundContainer = BaseFactory.eINSTANCE.createFolder();
				SpecmateEcoreUtil.copyAttributeValues(reqContainer, foundContainer, true);
				boolean valid = ensureValid(foundContainer);
				if (!valid) {
					logger.warn("Found invalid folder with id " + foundContainer.getId());
					return null;
				}
				rootContainer.getContents().add(foundContainer);
			}
		}
		return foundContainer;
	}

	/**
	 * Retrieves a root folder with the given name or creates a new one
	 *
	 * @param resource
	 * @param name
	 * @return
	 */
	private static IContainer getOrCreateLocalRootContainer(Resource resource, String name) {
		EObject object = SpecmateEcoreUtil.getEObjectWithId(name, resource.getContents());
		if (object != null) {
			if (object instanceof IContainer) {
				return (IContainer) object;
			}
		}

		Folder folder = BaseFactory.eINSTANCE.createFolder();
		String validName = name.replaceAll("[,\\|;]", " ");
		folder.setName(validName);
		folder.setId(validName);
		resource.getContents().add(folder);
		return folder;
	}

	/**
	 * Builds a map from ext-ids to requirements
	 *
	 * @param iterator
	 * @param requirementsMap
	 */
	private static void buildExtIdMap(Iterator<? extends EObject> iterator, HashMap<String, EObject> requirementsMap) {
		while (iterator.hasNext()) {
			EObject content = iterator.next();
			if (content == null) {
				continue;
			}
			if (content.eClass().getName().equals("Requirement")) {
				Requirement requirement = (Requirement) content;
				if (!StringUtils.isEmpty(requirement.getExtId())) {
					requirementsMap.put(requirement.getExtId(), requirement);
				}
			}
		}
	}

	/**
	 * Build hashset (extid -> requirement) for local requirements
	 *
	 * @param localRootContainer
	 * @return
	 */
	private static HashMap<String, EObject> buildLocalRequirementsMap(IContainer localRootContainer) {
		HashMap<String, EObject> localRequirementsMap = new HashMap<>();
		TreeIterator<EObject> localIterator = localRootContainer.eAllContents();
		buildExtIdMap(localIterator, localRequirementsMap);
		return localRequirementsMap;
	}

	private static void doAndCommitTransaction(ITransaction transaction, IContainer localRootContainer,
			HashMap<String, EObject> localRequirementsMap, List<Requirement> tosync, IConnector source, Logger logger) {
		try {
			transaction.doAndCommit(new IChange<Object>() {
				@Override
				public Object doChange() throws SpecmateException {
					syncContainers(localRootContainer, localRequirementsMap, tosync, source, logger);
					return null;
				}
			});
		} catch (Exception e) {
			logger.error("An error occured while committing synced requirement(s). Reason:" + e.getMessage(), e);
			transaction.rollback();
		}
	}
}
