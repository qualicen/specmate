package com.specmate.modelgeneration.internal.legacy.pseudocode;

import java.util.List;

import org.osgi.service.component.annotations.Component;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.requirements.CEGModel;
import com.specmate.modelgeneration.api.ICEGModelGenerator;
import com.specmate.objectif.resolve.BusinessRuleUtil;
import com.specmate.objectif.resolve.rule.BusinessRuleNode;
import com.specmate.xtext.XTextException;

/**
 * This class transforms pseudo code into a CEG. The pseudo code must conform
 * with the implemented XText grammar.
 */
@Component(immediate = true, service = ICEGModelGenerator.class, property = { "service.ranking:Integer=75",
		"languages=pseudo" })
public class CEGFromPseudoCodeGenerator implements ICEGModelGenerator {

	@Override
	public boolean createModel(CEGModel model) throws SpecmateException {
		String text = model.getModelRequirements();
		List<BusinessRuleNode> rules;
		/** First, we need to load all pseudo code lines from a document / input. */
		try {
			rules = new BusinessRuleUtil().parseXTextResource(text);
		} catch (XTextException e) {
			throw new SpecmateInternalException(ErrorCode.INTERNAL_PROBLEM,
					"Unfortunately, the XText parsing of your inserted pseudo code went wrong." + e.getMessage());
		}
		BusinessRuleNode rule = rules.get(0);
		new CEGFromPseudoCodeGeneratorHelper().createCEGModelForEachSection(rule, model, null, false);
		return true;
	}

}
