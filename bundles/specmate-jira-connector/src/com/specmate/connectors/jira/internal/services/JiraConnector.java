package com.specmate.connectors.jira.internal.services;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.specmate.common.cache.ICache;
import com.specmate.common.cache.ICacheLoader;
import com.specmate.common.cache.ICacheService;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.connectors.api.IConnector;
import com.specmate.connectors.api.IProject;
import com.specmate.connectors.api.IProjectConfigService;
import com.specmate.connectors.api.IProjectService;
import com.specmate.connectors.jira.config.JiraConfigConstants;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.crud.DetailsService;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.base.BaseFactory;
import com.specmate.model.base.Folder;
import com.specmate.model.base.IContainer;
import com.specmate.model.requirements.Requirement;
import com.specmate.model.requirements.RequirementsFactory;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.rest.RestResult;

/**
 * Connector to JIRA server and cloud
 *
 * @author junkerm
 *
 */
@Component(immediate = true, service = { IRestService.class,
		IConnector.class }, configurationPid = JiraConfigConstants.CONNECTOR_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class JiraConnector extends DetailsService implements IConnector, IRestService {

	/** Placeholder for the id of the parent element in a JQL query */
	private static final String PARENT_ID_PLACEHOLDER = "%parentId%";

	/** Placeholder for the project name in a JQL query */
	private static final String PROJECT_PLACEHOLDER = "%project%";

	/** Default value for pagination (i.e. number of items to query at once) */
	private static final String PAGINATION_SIZE_DEFAULT = "50";

	/** Name of the jira story cache */
	private static final String JIRA_STORY_CACHE_NAME = "jiraStoryCache";

	/** Source id of this connector */
	private static final String JIRA_SOURCE_ID = "jira";

	/** The associated project */
	private IProject project;

	/**
	 * Default jql query for fetching children (e.g. stories) of parent items (e.g.
	 * epics)
	 */
	private static final String DEFAULT_CHILDREN_JQL = "project=" + PROJECT_PLACEHOLDER
			+ " AND issueType=story AND \"Epic Link\"=\"" + PARENT_ID_PLACEHOLDER
			+ "\" ORDER BY assignee, resolutiondate";

	/**
	 * Default jql query for fetching items (e.g. stories) with no parent when
	 * option withFolders is true
	 */
	private static final String DEFAULT_DIRECT_JQL = "project=" + PROJECT_PLACEHOLDER
			+ " AND issueType=story AND \"Epic Link\" IS EMPTY ORDER BY assignee, resolutiondate";

	/** Default jql query for fetching items when option withFolders is false */
	private static final String DEFAULT_DIRECT_JQL_NO_FOLDERS = "project=" + PROJECT_PLACEHOLDER
			+ " AND issueType=story ORDER BY assignee, resolutiondate";

	/** Default jql query for fetching parent items */
	private static final String DEFAULT_PARENT_JQL = "project=" + PROJECT_PLACEHOLDER
			+ " AND issueType=epic ORDER BY id";

	/** Default evict time for the story cache */
	private static final String DEFAULT_CACHE_TIME = "120";

	/** The log service reference */
	private LogService logService;

	/** The caching service reference */
	private ICacheService cacheService;

	/** The configured id of this connector */
	private String id;

	/** The configured pagination size */
	private int paginationSizeInt;

	/** Configured jql query templates */
	private String parentJQL;
	private String directJQL;
	private String childrenJQL;

	/** Configured flag if connector should build a parent/child relationship */
	private boolean withFolders;

	/** Configured cache time */
	private int cacheTime;

	/** Map from issues to epics */
	private Map<Issue, Folder> epicFolders = new HashMap<>();

	/** Map from requirements to issues */
	private Map<Requirement, Issue> requirmentEpics = new HashMap<>();

	/** Default folder to put requirements that have no parent */
	private Folder defaultFolder;

	/** The story cache */
	private ICache<String, Issue> cache;

	/** The project service to access other projects **/
	private IProjectService projectService;

	/** The configuration of the jira server */
	private String serverUrl;
	private String serverProject;
	private String serverUsername;
	private String serverPassword;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		validateConfig(properties);

		id = (String) properties.get(IProjectConfigService.KEY_CONNECTOR_ID);
		serverUrl = (String) properties.get(JiraConfigConstants.KEY_JIRA_URL);
		serverProject = (String) properties.get(JiraConfigConstants.KEY_JIRA_PROJECT);
		serverUsername = (String) properties.get(JiraConfigConstants.KEY_JIRA_USERNAME);
		serverPassword = (String) properties.get(JiraConfigConstants.KEY_JIRA_PASSWORD);

		String paginationSizeStr = (String) properties.getOrDefault(JiraConfigConstants.KEY_JIRA_PAGINATION_SIZE,
				PAGINATION_SIZE_DEFAULT);
		try {
			paginationSizeInt = Integer.parseInt(paginationSizeStr);
		} catch (NumberFormatException nfe) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"Not valid value for pagination size: " + paginationSizeStr);
		}

		String cacheTimeStr = (String) properties.getOrDefault(JiraConfigConstants.KEY_JIRA_CACHE_TIME,
				DEFAULT_CACHE_TIME);
		try {
			cacheTime = Integer.parseInt(cacheTimeStr);
		} catch (NumberFormatException nfe) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"Not valid value for cache time: " + cacheTimeStr);
		}

		String withFoldersStr = (String) properties.getOrDefault(JiraConfigConstants.KEY_JIRA_WITH_FOLDERS, "true");
		withFolders = Boolean.parseBoolean(withFoldersStr.toLowerCase());

		if (withFolders) {
			directJQL = (String) properties.getOrDefault(JiraConfigConstants.KEY_JIRA_DIRECT_SQL, DEFAULT_DIRECT_JQL);
		} else {
			directJQL = (String) properties.getOrDefault(JiraConfigConstants.KEY_JIRA_DIRECT_SQL,
					DEFAULT_DIRECT_JQL_NO_FOLDERS);
		}
		parentJQL = (String) properties.getOrDefault(JiraConfigConstants.KEY_JIRA_PARENT_SQL, DEFAULT_PARENT_JQL);
		childrenJQL = (String) properties.getOrDefault(JiraConfigConstants.KEY_JIRA_CHILDREN_SQL, DEFAULT_CHILDREN_JQL);

		defaultFolder = createFolder(serverProject + "-Default", serverProject + "-Default");

		cache = cacheService.createCache(JIRA_STORY_CACHE_NAME, 500, cacheTime, new ICacheLoader<String, Issue>() {

			@Override
			public Issue load(String key) throws SpecmateException {
				Issue issue = getStory(key);
				return issue;
			}
		});

		logService.log(LogService.LOG_DEBUG, "Initialized Jira Connector with " + properties.toString() + ".");
	}

	@Deactivate
	public void deactivate() throws SpecmateInternalException {
		cacheService.removeCache(JIRA_STORY_CACHE_NAME);
	}

	private JiraRestClient getJiraClient() throws SpecmateInternalException {
		try {
			return JiraUtil.createJiraRESTClient(serverUrl, serverUsername, serverPassword);
		} catch (URISyntaxException e) {
			logService.log(LogService.LOG_ERROR, "Could not create Jira REST client. Reason is: " + e.getMessage());
			throw new SpecmateInternalException(ErrorCode.JIRA, "Could not create Jira REST client", e);
		}
	}

	private void validateConfig(Map<String, Object> properties) throws SpecmateException {
		String aProject = (String) properties.get(JiraConfigConstants.KEY_JIRA_PROJECT);
		String aUsername = (String) properties.get(JiraConfigConstants.KEY_JIRA_USERNAME);
		String aPassword = (String) properties.get(JiraConfigConstants.KEY_JIRA_PASSWORD);

		if (isEmpty(aProject) || isEmpty(aUsername) || isEmpty(aPassword)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION, String.format(
					"Jira Connector (%s) is not well configured. Username, password and project need to be provided.",
					id));
		}
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	@Reference
	public void setProjectService(IProjectService projectService) {
		this.projectService = projectService;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Collection<Requirement> getRequirements() throws SpecmateException {
		logService.log(LogService.LOG_DEBUG, String.format("Jira connector (%s): retrieving requirements.", id));
		List<Requirement> requirements = new ArrayList<>();

		List<Issue> storiesWithoutEpic = getStoriesWithoutEpic();
		for (Issue story : storiesWithoutEpic) {
			requirements.add(createRequirement(story));
		}

		if (withFolders) {
			List<Issue> epics = getEpics();

			for (Issue epic : epics) {
				createFolderIfNotExists(epic);
				List<Issue> stories = getStoriesForEpic(epic);
				for (Issue story : stories) {
					Requirement requirement = createRequirement(story);
					requirmentEpics.put(requirement, epic);
					requirements.add(requirement);
				}
			}
		}

		return requirements;
	}

	private List<Issue> getStoriesForEpic(Issue epic) throws SpecmateException {
		String jql = childrenJQL.replaceAll(PROJECT_PLACEHOLDER, serverProject).replace(PARENT_ID_PLACEHOLDER,
				epic.getKey());
		return getIssues(jql);
	}

	private List<Issue> getStoriesWithoutEpic() throws SpecmateException {
		logService.log(LogService.LOG_DEBUG,
				String.format("Jira connector (%s): retrieving default requirements. Query is %s", id, directJQL));
		String jql = directJQL.replaceAll(PROJECT_PLACEHOLDER, serverProject);
		return getIssues(jql);
	}

	private List<Issue> getEpics() throws SpecmateException {
		logService.log(LogService.LOG_DEBUG,
				String.format("Jira connector (%s): retrieving parent requirements. Query is %s", id, parentJQL));
		String jql = parentJQL.replaceAll(PROJECT_PLACEHOLDER, serverProject);
		return getIssues(jql);
	}

	private Issue getStory(String storyId) throws SpecmateException {
		logService.log(LogService.LOG_DEBUG,
				String.format("Jira connector (%s) retrieving item with id %s", id, storyId));
		List<Issue> issues = getIssues("project=" + serverProject + " AND id=" + storyId);
		if (issues == null || issues.size() == 0) {
			throw new SpecmateInternalException(ErrorCode.INTERNAL_PROBLEM, "JIRA Issue not found: " + storyId);
		}
		return issues.get(0);
	}

	private List<Issue> getIssues(String jql) throws SpecmateException {
		logService.log(LogService.LOG_DEBUG, String.format("Jira connector (%s): executing query: %s", id, jql));

		List<Issue> issues = new ArrayList<>();

		JiraRestClient jiraClient = null;
		try {

			jiraClient = this.getJiraClient();

			int maxResults = Integer.MAX_VALUE;
			while (issues.size() < maxResults) {
				try {

					SearchResult searchResult = jiraClient.getSearchClient()
							.searchJql(jql, paginationSizeInt, issues.size(), null).claim();
					maxResults = searchResult.getTotal();
					searchResult.getIssues().forEach(issue -> issues.add(issue));
					logService.log(LogService.LOG_DEBUG,
							"Jira Connector (" + id + "): Loaded ~" + searchResult.getMaxResults()
									+ " issues from Jira " + serverUrl + " project: " + serverProject);
				} catch (RestClientException e) {
					if (e.getStatusCode().get() == 400) {
						logService.log(LogService.LOG_WARNING,
								String.format("Jira Connector (%s): Received 400 status, JQL: %s, Details: %s", id, jql,
										e.getMessage()));
						return issues;
					} else {
						logService.log(LogService.LOG_ERROR, String.format(
								"Jira Connector (%s): Could not load issue from jira. Reason: %s", id, e.getMessage()));
						throw new SpecmateInternalException(ErrorCode.INTERNAL_PROBLEM,
								"Could not load issues from jira", e);
					}
				}

			}

			logService.log(LogService.LOG_INFO, "Jira Connector (" + id + "): Finished loading of " + issues.size()
					+ " issues from Jira " + serverUrl + " project: " + serverProject);

		} catch (Exception e) {
			throw new SpecmateInternalException(ErrorCode.INTERNAL_PROBLEM, e);
		} finally {

			try {
				jiraClient.close();
				cacheService.removeCache(JIRA_STORY_CACHE_NAME);
			} catch (IOException e) {
				throw new SpecmateInternalException(ErrorCode.INTERNAL_PROBLEM, "Could not close JIRA client.", e);
			}
		}

		return issues;
	}

	@Override
	public IContainer getContainerForRequirement(Requirement requirement) throws SpecmateException {
		if (!withFolders) {
			return null;
		}
		Issue epic = requirmentEpics.get(requirement);
		if (epic == null) {
			return defaultFolder;
		}
		return epicFolders.get(epic);
	}

	@Override
	public Set<IProject> authenticate(String username, String password) throws SpecmateException {

		List<String> accessibleJiraProjectNames = JiraUtil.getProjects(serverUrl, username, password);
		Set<IProject> accessibleSpecmateProjectNames = new HashSet<>();

		for (String specmateProjectName : projectService.getProjectNames()) {

			IProject specmateProject = projectService.getProject(specmateProjectName);
			IConnector connector = specmateProject.getConnector();

			if (connector instanceof JiraConnector) {
				JiraConnector jiraConnector = (JiraConnector) connector;

				if (accessibleJiraProjectNames.contains(jiraConnector.getProjectName())
						&& jiraConnector.getUrl().equals(getUrl())) {
					accessibleSpecmateProjectNames.add(specmateProject);
				}

			}
		}

		return accessibleSpecmateProjectNames;
	}

	private static Requirement createRequirement(Issue story) {
		Requirement requirement = RequirementsFactory.eINSTANCE.createRequirement();
		String id = story.getKey();
		String idShort = Long.toString(story.getId());
		requirement.setId(id + "-" + idShort);
		requirement.setExtId(id);
		requirement.setExtId2(id);
		requirement.setSource(JIRA_SOURCE_ID);
		requirement.setName(story.getSummary());
		requirement.setDescription(story.getDescription());
		requirement.setStatus(story.getStatus().getName());
		requirement.setLive(true);
		return requirement;
	}

	private void createFolderIfNotExists(Issue epic) {
		if (!epicFolders.containsKey(epic)) {
			String folderId = epic.getKey();
			String folderName = folderId + ": " + epic.getSummary();
			Folder folder = createFolder(folderId, folderName);
			epicFolders.put(epic, folder);
		}
	}

	private Folder createFolder(String folderId, String folderName) {
		Folder folder = BaseFactory.eINSTANCE.createFolder();
		folder.setId(folderId);
		folder.setName(folderName);
		return folder;
	}

	@Override
	public int getPriority() {
		return super.getPriority() + 1;
	}

	@Override
	public boolean canGet(Object target) {
		if (target instanceof Requirement) {
			Requirement req = (Requirement) target;
			return req.getSource() != null && req.getSource().equals(JIRA_SOURCE_ID)
					&& (SpecmateEcoreUtil.getProjectId(req).equals(id));
		}
		return false;
	}

	@Override
	public boolean canPut(Object target, Object object) {
		return false;
	}

	@Override
	public boolean canPost(Object object2, Object object) {
		return false;
	}

	@Override
	public boolean canDelete(Object object) {
		return false;
	}

	/**
	 * Behavior for GET requests. For requirements the current data is fetched from
	 * the HP server.
	 */
	@Override
	public RestResult<?> get(Object target, MultivaluedMap<String, String> queryParams, String token)
			throws SpecmateException {
		if (!(target instanceof Requirement)) {
			return super.get(target, queryParams, token);
		}
		Requirement localRequirement = (Requirement) target;

		if (localRequirement.getExtId() == null) {
			return super.get(target, queryParams, token);
		}

		Issue issue;
		try {
			issue = cache.get(localRequirement.getExtId());
		} catch (Exception e) {
			// Loading has failed
			return new RestResult<>(Status.NOT_FOUND);
		}

		Requirement retrievedRequirement = createRequirement(issue);
		SpecmateEcoreUtil.copyAttributeValues(retrievedRequirement, localRequirement, false);

		return new RestResult<>(Response.Status.OK, localRequirement);
	}

	@Reference
	public void setCacheService(ICacheService cacheService) {
		this.cacheService = cacheService;
	}

	public String getProjectName() {
		return serverProject;
	}

	public String getUrl() {
		return serverUrl;
	}

	@Override
	public Requirement getRequirementById(String id) throws SpecmateException {
		try {
			Issue issue = getStory(id);
			Requirement requirement = createRequirement(issue);
			return requirement;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}
}
