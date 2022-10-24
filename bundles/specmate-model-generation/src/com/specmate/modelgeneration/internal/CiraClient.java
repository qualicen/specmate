package com.specmate.modelgeneration.internal;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.model.administration.ErrorCode;
import com.specmate.rest.RestClient;
import com.specmate.rest.RestResult;

/**
 * Class to facilitate interacting with a Cira backend for classification and
 * labeling (see https://github.com/JulianFrattini/cira-app)
 */
public class CiraClient {

	private Logger logger;
	private RestClient client;

	public CiraClient(String ciraUrl, LoggerFactory loggerFactory) {
		client = new RestClient(ciraUrl, 10, loggerFactory.getLogger(RestClient.class));
		logger = loggerFactory.getLogger(CiraClient.class);
	}

	public boolean isCausal(String sentence) throws SpecmateException {
		JSONObject object = new JSONObject();
		object.put("sentence", sentence);
		object.put("language", "en");
		RestResult<JSONObject> result = client.post("/api/classify", object);
		if (result.getResponse().getStatus() != Status.OK.getStatusCode()) {
			throw new SpecmateInternalException(ErrorCode.INTERNAL_PROBLEM, "Error when accessing Cira Api.");
		}
		Boolean causal = result.getPayload().getBoolean("causal");
		return causal;

	}

	public List<Label> getLabels(String sentence) throws SpecmateInternalException {
		List<Label> labels = new ArrayList<>();
		JSONObject object = new JSONObject();
		object.put("sentence", sentence);
		object.put("language", "en");
		RestResult<JSONObject> result = client.post("/api/label", object);
		if (result.getResponse().getStatus() != Status.OK.getStatusCode()) {
			throw new SpecmateInternalException(ErrorCode.INTERNAL_PROBLEM, "Error when accessing Cira Api.");
		}
		JSONObject resultObject = result.getPayload();
		JSONArray jsonlabels = resultObject.getJSONArray("labels");

		for (int i = 0; i < jsonlabels.length(); i++) {
			JSONObject jsonlabel = jsonlabels.getJSONObject(i);

			Label label = new Label(jsonlabel.getInt("begin"), jsonlabel.getInt("end"), jsonlabel.getString("label"),
					jsonlabel.getString("id"));
			labels.add(label);
		}

		return labels;

	}

	class Label {
		int begin;
		int end;
		String label;
		String id;

		public Label(int begin, int end, String label, String id) {
			super();
			this.begin = begin;
			this.end = end;
			this.label = label;
			this.id = id;
		}

	}

}
