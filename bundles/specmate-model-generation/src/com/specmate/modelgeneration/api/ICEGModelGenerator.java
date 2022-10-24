package com.specmate.modelgeneration.api;

import com.specmate.common.exception.SpecmateException;
import com.specmate.model.requirements.CEGModel;

public interface ICEGModelGenerator {

	void createModel(CEGModel model) throws SpecmateException;

}
