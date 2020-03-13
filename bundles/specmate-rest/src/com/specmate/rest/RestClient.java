package com.specmate.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.sse.SseFeature;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.service.log.LogService;

public class RestClient {

	private Client restClient;
	private String restUrl;
	private int timeout;
	private LogService logService;
	private String authenticationToken;

	public RestClient(String restUrl, String authenticationToken, int timeout, LogService logService) {
		restClient = initializeClient();
		this.restUrl = restUrl;
		this.timeout = timeout;
		this.logService = logService;
		this.authenticationToken = authenticationToken != null ? authenticationToken : "";
	}

	public void close() {
		restClient.close();
	}

	public RestClient(String restUrl, String authenticationToken, LogService logService) {
		this(restUrl, authenticationToken, 5000, logService);
	}

	public RestClient(String restUrl, int timeout, LogService logService) {
		this(restUrl, null, timeout, logService);
	}

	private Client initializeClient() {
		ClientConfig config = new ClientConfig();
		config.property(ClientProperties.CONNECT_TIMEOUT, timeout);
		config.property(ClientProperties.READ_TIMEOUT, timeout);
		Client client = ClientBuilder.newBuilder().withConfig(config).register(SseFeature.class).build();
		return client;
	}

	private Response rawGet(String url, String... params) {
		Invocation.Builder invocationBuilder = getInvocationBuilder(url, params);
		Response response = invocationBuilder.get();
		return response;
	}

	private Invocation.Builder getInvocationBuilder(String url, String... params) {
		UriBuilder uriBuilder = UriBuilder.fromUri(restUrl);
		uriBuilder.path(url);
		for (int i = 0; i < params.length; i += 2) {
			if (i < params.length - 1) {
				uriBuilder.queryParam(params[i], params[i + 1]);
			}
		}
		if (logService != null) {
			logService.log(LogService.LOG_DEBUG, "Building Invocation for " + uriBuilder);
		}
		WebTarget getTarget = restClient.target(uriBuilder);
		Invocation.Builder invocationBuilder = getTarget.request().header(HttpHeaders.AUTHORIZATION,
				"Token " + authenticationToken);
		return invocationBuilder;
	}

	public RestResult<JSONObject> get(String url, String... params) {
		Response response = rawGet(url, params);
		return createResult(url, response);
	}

	public RestResult<JSONArray> getList(String url, String... params) {
		Response response = rawGet(url, params);
		if (response.hasEntity()) {
			String result = response.readEntity(String.class);
			return new RestResult<>(response, url, new JSONArray(new JSONTokener(result)));
		} else {
			return new RestResult<>(response, url, null);
		}
	}

	public RestResult<JSONObject> post(String url, JSONObject jsonObject) {
		Response response = rawPost(url, jsonObject);
		return createResult(url, response);
	}

	public Response rawPost(String url, JSONObject jsonObject) {
		Invocation.Builder invocationBuilder = getInvocationBuilder(url);
		Entity<String> entity;
		if (jsonObject == null) {
			entity = null;
		} else {
			entity = Entity.entity(jsonObject.toString(), "application/json;charset=utf-8");
		}
		Response response = invocationBuilder.post(entity);
		return response;
	}

	public RestResult<JSONObject> put(String url, JSONObject objectJson) {
		Invocation.Builder invocationBuilder = getInvocationBuilder(url);
		Response response = invocationBuilder.put(Entity.json(objectJson.toString()));
		return createResult(url, response);
	}

	public RestResult<JSONObject> delete(String url) {
		Invocation.Builder invocationBuilder = getInvocationBuilder(url);
		Response response = invocationBuilder.delete();
		return createResult(url, response);
	}

	private RestResult<JSONObject> createResult(String url, Response response) {
		if (response.hasEntity()) {
			String result = response.readEntity(String.class);
			try {
				return new RestResult<>(response, url, new JSONObject(new JSONTokener(result)));
			} catch (JSONException e) {
				logService.log(LogService.LOG_WARNING, e.getMessage());
			}
		}

		return new RestResult<>(response, url, null);
	}
}
