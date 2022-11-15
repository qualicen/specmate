package com.specmate.modelgeneration.internal.cira;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
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

	private RestClient client;

	public CiraClient(String ciraUrl, LoggerFactory loggerFactory) {
		client = new RestClient(ciraUrl, 10, loggerFactory.getLogger(RestClient.class));
	}

	public boolean isCausal(String sentence) throws SpecmateException {
		sentence = sentence.trim();
		JSONObject object = new JSONObject();
		object.put("sentence", sentence);
		object.put("language", "en");
		RestResult<JSONObject> result = client.post("classify", object);
		if (result.getResponse().getStatus() != Status.OK.getStatusCode()) {
			throw new SpecmateInternalException(ErrorCode.INTERNAL_PROBLEM, "Error when accessing Cira Api.");
		}
		Boolean causal = result.getPayload().getBoolean("causal");
		return causal;

	}

	public List<CiraLabel> getLabels(String sentence) throws SpecmateInternalException {
		sentence = sentence.trim();
		List<CiraLabel> labels = new ArrayList<>();
		JSONObject object = new JSONObject();
		object.put("sentence", sentence);
		object.put("language", "en");
		RestResult<JSONObject> result = client.post("label", object);
		if (result.getResponse().getStatus() != Status.OK.getStatusCode()) {
			throw new SpecmateInternalException(ErrorCode.INTERNAL_PROBLEM, "Error when accessing Cira Api.");
		}
		JSONObject resultObject = result.getPayload();
		JSONArray jsonlabels = resultObject.getJSONArray("labels");

		for (int i = 0; i < jsonlabels.length(); i++) {
			JSONObject jsonlabel = jsonlabels.getJSONObject(i);

			CiraLabel label = new CiraLabel(jsonlabel.getInt("begin"), jsonlabel.getInt("end"),
					jsonlabel.getString("label"), jsonlabel.getString("id"));
			labels.add(label);
		}

		return labels;

	}

}
