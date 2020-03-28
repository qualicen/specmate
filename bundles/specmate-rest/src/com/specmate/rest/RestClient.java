package com.specmate.rest;

import java.util.Map;

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

	private Response rawGet(String url, Map<String, String> params, Map<String, String> headers) {
		Invocation.Builder invocationBuilder = getInvocationBuilder(url, params, headers);
		Response response = invocationBuilder.get();
		return response;
	}

	private Invocation.Builder getInvocationBuilder(String url, Map<String, String> params,
			Map<String, String> headers) {
		UriBuilder uriBuilder = UriBuilder.fromUri(restUrl);
		uriBuilder.path(url);
		if (params != null) {
			params.forEach((key, val) -> uriBuilder.queryParam(key, val));
		}

		if (logService != null) {
			logService.log(LogService.LOG_DEBUG, "Building Invocation for " + uriBuilder);
		}
		WebTarget getTarget = restClient.target(uriBuilder);
		Invocation.Builder invocationBuilder = getTarget.request();
		if (authenticationToken != null && !authenticationToken.isEmpty()) {
			invocationBuilder.header(HttpHeaders.AUTHORIZATION, "Token " + authenticationToken);
		}
		if (headers != null) {
			headers.forEach((key, val) -> invocationBuilder.header(key, val));
		}
		return invocationBuilder;
	}

	public RestResult<JSONObject> get(String url, Map<String, String> params, Map<String, String> headers) {
		Response response = rawGet(url, params, headers);
		return createResult(url, response);
	}

	public RestResult<JSONObject> get(String url, Map<String, String> params) {
		return get(url, params, null);
	}

	public RestResult<JSONObject> get(String url) {
		return get(url, null, null);
	}

	public RestResult<JSONArray> getList(String url, Map<String, String> params, Map<String, String> headers) {
		Response response = rawGet(url, params, headers);
		if (response.hasEntity()) {
			String result = response.readEntity(String.class);
			return new RestResult<>(response, url, new JSONArray(new JSONTokener(result)));
		} else {
			return new RestResult<>(response, url, null);
		}
	}

	public RestResult<JSONArray> getList(String url, Map<String, String> params) {
		return getList(url, params, null);
	}

	public RestResult<JSONArray> getList(String url) {
		return getList(url, null, null);
	}

	public RestResult<JSONObject> post(String url, Object object, Map<String, String> params,
			Map<String, String> headers) {
		Response response = rawPost(url, object, params, headers);
		return createResult(url, response);
	}

	public RestResult<JSONObject> post(String url, Object object, Map<String, String> params) {
		return post(url, object, params, null);
	}

	public RestResult<JSONObject> post(String url, Object object) {
		return post(url, object, null, null);
	}

	public Response rawPost(String url, Object object, Map<String, String> params, Map<String, String> headers) {
		Invocation.Builder invocationBuilder = getInvocationBuilder(url, params, headers);
		Entity<String> entity;
		if (object == null) {
			entity = null;
		} else {
			entity = Entity.entity(object.toString(), "application/json;charset=utf-8");
		}
		Response response = invocationBuilder.post(entity);
		return response;
	}

	public RestResult<JSONObject> put(String url, JSONObject objectJson, Map<String, String> params,
			Map<String, String> headers) {
		Invocation.Builder invocationBuilder = getInvocationBuilder(url, params, headers);
		Response response = invocationBuilder.put(Entity.json(objectJson.toString()));
		return createResult(url, response);
	}

	public RestResult<JSONObject> put(String url, JSONObject objectJson, Map<String, String> params) {
		return put(url, objectJson, params, null);
	}

	public RestResult<JSONObject> put(String url, JSONObject objectJson) {
		return put(url, objectJson, null, null);
	}

	public RestResult<JSONObject> delete(String url, Map<String, String> params, Map<String, String> headers) {
		Invocation.Builder invocationBuilder = getInvocationBuilder(url, params, headers);
		Response response = invocationBuilder.delete();
		return createResult(url, response);
	}

	public RestResult<JSONObject> delete(String url, Map<String, String> params) {
		return delete(url, params, null);
	}

	public RestResult<JSONObject> delete(String url) {
		return delete(url, null, null);
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
