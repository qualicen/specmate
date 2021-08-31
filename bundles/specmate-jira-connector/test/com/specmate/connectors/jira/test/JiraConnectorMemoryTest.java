package com.specmate.connectors.jira.test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerConsumer;

import com.atlassian.jira.rest.client.api.AuditRestClient;
import com.atlassian.jira.rest.client.api.ComponentRestClient;
import com.atlassian.jira.rest.client.api.GroupRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.MyPermissionsRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.ProjectRolesRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.specmate.common.cache.ICache;
import com.specmate.common.cache.ICacheLoader;
import com.specmate.common.cache.ICacheService;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.connectors.api.IProjectConfigService;
import com.specmate.connectors.jira.config.JiraConfigConstants;
import com.specmate.connectors.jira.internal.services.IJiraClientFactory;
import com.specmate.connectors.jira.internal.services.JiraConnector;

import io.atlassian.util.concurrent.Promise;

public class JiraConnectorMemoryTest {

	boolean stopMemoryThread = false;
	long maxMemory = 0;
	private int idCounter = 0;

	@Test
	public void testNoMemoryLeak() throws SpecmateException, InterruptedException {

		Thread memoryThread = new Thread() {
			@Override
			public void run() {
				while (!stopMemoryThread) {
					maxMemory = Math.max(maxMemory, Runtime.getRuntime().totalMemory());
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			};
		};
		memoryThread.start();
		JiraConnector connector = new JiraConnector();
		Map<String, Object> config = getConnectorConfig();
		connector.setJiraRestClientFactory(new TestJiraClientFactory());
		connector.setCacheService(new TestCacheService());
		connector.setLogger(new TestLogger());
		connector.activate(config);
		for (int i = 1; i <= 2; i++) {
			connector.getRequirements();
			idCounter = 0;
			Runtime.getRuntime().gc();
		}
		stopMemoryThread = true;
		memoryThread.join();
		// Should acquire no more than 2gb of heap memory
		Assert.assertTrue(maxMemory / 1e9 < 2);

	}

	private Map<String, Object> getConnectorConfig() {
		Map<String, Object> config = new HashMap<>();
		config.put(IProjectConfigService.KEY_CONNECTOR_ID, "testid");
		config.put(JiraConfigConstants.KEY_JIRA_URL, "https://testurl");
		config.put(JiraConfigConstants.KEY_JIRA_PROJECT, "testproject");
		config.put(JiraConfigConstants.KEY_JIRA_USERNAME, "testuser");
		config.put(JiraConfigConstants.KEY_JIRA_PASSWORD, "testpassword");

		return config;
	}

	private class TestJiraClientFactory implements IJiraClientFactory {

		@Override
		public JiraRestClient createJiraClient(String serverUrl, String serverUsername, String serverPassword)
				throws SpecmateInternalException {
			return new TestJiraClient();
		}

	}

	private class TestJiraClient implements JiraRestClient {

		@Override
		public void close() throws IOException {
		}

		@Override
		public AuditRestClient getAuditRestClient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ComponentRestClient getComponentClient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public GroupRestClient getGroupClient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IssueRestClient getIssueClient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public MetadataRestClient getMetadataClient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public MyPermissionsRestClient getMyPermissionsRestClient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ProjectRestClient getProjectClient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ProjectRolesRestClient getProjectRolesRestClient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public SearchRestClient getSearchClient() {
			return new TestSearchClient();
		}

		@Override
		public SessionRestClient getSessionClient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public UserRestClient getUserClient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public VersionRestClient getVersionRestClient() {
			throw new UnsupportedOperationException();
		}

	}

	private class TestSearchClient implements SearchRestClient {

		private static final int MAX_EPICS = 20000;
		private static final int STORIES_PER_EPIC = 50;

		IssueType epicType = new IssueType(null, (long) idCounter++, "epic", false, "", null);
		IssueType storyType = new IssueType(null, (long) idCounter++, "story", false, "", null);
		Status status = new Status(null, (long) idCounter++, "open", null, null, null);

		@Override
		public Promise<Iterable<Filter>> getFavouriteFilters() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<Filter> getFilter(URI arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<Filter> getFilter(long arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<SearchResult> searchJql(String arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<SearchResult> searchJql(String jql, Integer paginationSize, Integer from, Set<String> fields) {
			if (jql.contains("issueType=epic")) {
				return getEpicsSearchResult(paginationSize, from);
			} else if (jql.contains("issueType=story AND \"Epic Link\" IS EMPTY")) {
				return getStoriesWithoutEpicsSearchResult();
			} else if (jql.contains("issueType=story AND \"Epic Link\"=")) {
				return getStoriesForEpicSearchResult();
			}
			return null;
		}

		private Promise<SearchResult> getStoriesForEpicSearchResult() {
			List<Issue> stories = new ArrayList<>();
			for (int i = 0; i < STORIES_PER_EPIC; i++) {
				stories.add(createStory());
			}
			SearchResult result = new SearchResult(0, STORIES_PER_EPIC, STORIES_PER_EPIC, stories);
			return new TestPromise<SearchResult>(result);
		}

		private Promise<SearchResult> getStoriesWithoutEpicsSearchResult() {
			SearchResult result = new SearchResult(0, 0, 0, Collections.emptyList());
			return new TestPromise<SearchResult>(result);
		}

		private Promise<SearchResult> getEpicsSearchResult(Integer paginationSize, Integer from) {
			List<Issue> epics = new ArrayList<>();
			for (int i = from; i < from + paginationSize; i++) {
				epics.add(createEpic());
			}
			SearchResult result = new SearchResult(from, paginationSize, MAX_EPICS, epics);
			return new TestPromise<SearchResult>(result);
		}

		private Issue createEpic() {

			int id = idCounter++;
			Issue issue = new Issue("Summary for epic " + id, null, "TP-" + id, (long) id, null, epicType, status, null,
					null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
					null, null, null, null, null, null, null, null);
			return issue;
		}

		private Issue createStory() {

			int id = idCounter++;
			Issue issue = new Issue("Summary for story " + id, null, "TP-" + id, (long) id, null, storyType, status,
					null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
					null, null, null, null, null, null, null, null, null);
			return issue;
		}

	}

	private class TestPromise<T> implements Promise<T> {

		private T value;

		public TestPromise(T value) {
			this.value = value;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isCancelled() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isDone() {
			throw new UnsupportedOperationException();
		}

		@Override
		public T get() throws InterruptedException, ExecutionException {
			throw new UnsupportedOperationException();
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			throw new UnsupportedOperationException();
		}

		@Override
		public T claim() {
			return this.value;
		}

		@Override
		public Promise<T> done(Consumer<? super T> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<T> fail(Consumer<Throwable> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <B> Promise<B> flatMap(Function<? super T, ? extends Promise<? extends B>> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <B> Promise<B> fold(Function<Throwable, ? extends B> arg0, Function<? super T, ? extends B> arg1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <B> Promise<B> map(Function<? super T, ? extends B> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<T> recover(Function<Throwable, ? extends T> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<T> then(TryConsumer<? super T> arg0) {
			throw new UnsupportedOperationException();
		}

	}

	private class TestCacheService implements ICacheService {

		@Override
		public <K, V> ICache<K, V> createCache(String name, int capacity, int evictTime, ICacheLoader<K, V> loader) {
			return new ICache<K, V>() {

				@Override
				public V get(K key) throws SpecmateException {
					return loader.load(key);
				}

				@Override
				public void clean() {
				}
			};
		}

		@Override
		public void removeCache(String name) {
		}

	}

	private class TestLogger implements Logger {

		@Override
		public void audit(String arg0) {
		}

		@Override
		public void audit(String arg0, Object arg1) {
		}

		@Override
		public void audit(String arg0, Object... arg1) {
		}

		@Override
		public void audit(String arg0, Object arg1, Object arg2) {
		}

		@Override
		public void debug(String arg0) {
		}

		@Override
		public <E extends Exception> void debug(LoggerConsumer<E> arg0) throws E {
		}

		@Override
		public void debug(String arg0, Object arg1) {
		}

		@Override
		public void debug(String arg0, Object... arg1) {
		}

		@Override
		public void debug(String arg0, Object arg1, Object arg2) {
		}

		@Override
		public void error(String arg0) {
		}

		@Override
		public <E extends Exception> void error(LoggerConsumer<E> arg0) throws E {
		}

		@Override
		public void error(String arg0, Object arg1) {
		}

		@Override
		public void error(String arg0, Object... arg1) {
		}

		@Override
		public void error(String arg0, Object arg1, Object arg2) {
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void info(String arg0) {
		}

		@Override
		public <E extends Exception> void info(LoggerConsumer<E> arg0) throws E {
		}

		@Override
		public void info(String arg0, Object arg1) {
		}

		@Override
		public void info(String arg0, Object... arg1) {
		}

		@Override
		public void info(String arg0, Object arg1, Object arg2) {
		}

		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public boolean isErrorEnabled() {
			return false;
		}

		@Override
		public boolean isInfoEnabled() {
			return false;
		}

		@Override
		public boolean isTraceEnabled() {
			return false;
		}

		@Override
		public boolean isWarnEnabled() {
			return false;
		}

		@Override
		public void trace(String arg0) {
		}

		@Override
		public <E extends Exception> void trace(LoggerConsumer<E> arg0) throws E {
		}

		@Override
		public void trace(String arg0, Object arg1) {
		}

		@Override
		public void trace(String arg0, Object... arg1) {
		}

		@Override
		public void trace(String arg0, Object arg1, Object arg2) {
		}

		@Override
		public void warn(String arg0) {
		}

		@Override
		public <E extends Exception> void warn(LoggerConsumer<E> arg0) throws E {
		}

		@Override
		public void warn(String arg0, Object arg1) {
		}

		@Override
		public void warn(String arg0, Object... arg1) {
		}

		@Override
		public void warn(String arg0, Object arg1, Object arg2) {
		}

	}
}
