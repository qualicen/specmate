package com.specmate.modelgeneration.internal.legacy.patternbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.cause_effect_patterns.parse.matcher.MatchResult;
import com.specmate.cause_effect_patterns.parse.wrapper.BinaryMatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchTreeBuilder;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.config.api.IConfigService;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.requirements.CEGModel;
import com.specmate.modelgeneration.api.ICEGModelGenerator;
import com.specmate.modelgeneration.internal.legacy.patternbased.stages.GraphBuilder;
import com.specmate.modelgeneration.internal.legacy.patternbased.stages.GraphLayouter;
import com.specmate.modelgeneration.internal.legacy.patternbased.stages.MatcherPostProcesser;
import com.specmate.modelgeneration.internal.legacy.patternbased.stages.RuleMatcher;
import com.specmate.modelgeneration.internal.legacy.patternbased.stages.TextPreProcessor;
import com.specmate.modelgeneration.internal.legacy.patternbased.stages.graph.Graph;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;
import com.specmate.nlp.util.NLPUtil;

@Component(immediate = true, service = ICEGModelGenerator.class, property = { "service.ranking:Integer=75",
		"languages=de", "languages=en" })
public class PatternbasedCEGGenerator implements ICEGModelGenerator {
	private final Map<ELanguage, RuleMatcher> matchers = new HashMap<>();
	private final Map<ELanguage, TextPreProcessor> preProcessors = new HashMap<>();

	@Reference
	private IConfigService configService;

	@Reference(service = LoggerFactory.class)
	private Logger logger;

	@Reference
	private INLPService tagger;

	@Activate
	public void activate() throws SpecmateException {
		matchers.put(ELanguage.EN, new RuleMatcher(tagger, configService, ELanguage.EN));
		matchers.put(ELanguage.DE, new RuleMatcher(tagger, configService, ELanguage.DE));
		preProcessors.put(ELanguage.EN, new TextPreProcessor(ELanguage.EN, tagger));
		preProcessors.put(ELanguage.DE, new TextPreProcessor(ELanguage.DE, tagger));
	}

	@Override
	public boolean createModel(CEGModel originalModel) throws SpecmateException {
		String input = originalModel.getModelRequirements();
		logger.info("Textinput: " + input);
		ELanguage language = NLPUtil.detectLanguage(input);
		TextPreProcessor preProcessor = preProcessors.get(language);
		RuleMatcher matcher = matchers.get(language);
		List<String> texts = preProcessor.preProcess(input);
		List<Pair<String, CEGModel>> candidates = new ArrayList<>();

		for (String text : texts) {
			logger.info("Text Pre Processing: " + text);
			final List<MatchResult> results = matcher.matchText(text);

			final MatchTreeBuilder builder = new MatchTreeBuilder();

			// Convert all successful match results into an intermediate representation
			final List<MatchResultTreeNode> trees = results.stream().filter(MatchResult::isSuccessfulMatch)
					.map(builder::buildTree).filter(Optional::isPresent).map(Optional::get)
					.collect(Collectors.toList());

			final MatcherPostProcesser matchPostProcesser = new MatcherPostProcesser(language);
			GraphBuilder graphBuilder = new GraphBuilder();
			GraphLayouter graphLayouter = new GraphLayouter(language);

			for (MatchResultTreeNode tree : trees) {
				try {
					matchPostProcesser.process(tree);
					while (tree.getType().isLimitedCondition()) {
						tree = ((BinaryMatchResultTreeNode) tree).getSecondArgument();

					}

					if (!tree.getType().isCondition()) {
						continue;
					}

					Graph graph = graphBuilder.buildGraph((BinaryMatchResultTreeNode) tree);
					CEGModel model = graphLayouter.createModel(graph);
					candidates.add(Pair.of(text, model));
				} catch (Throwable t) {
					logger.debug("Error occured processing the dependency parse tree: " + t.getMessage(), t);
					return false;
				}
			}

		}
		if (candidates.isEmpty()) {
			throw new SpecmateInternalException(ErrorCode.NLP, "No Cause-Effect Pair Found.");
		}

		// Sort by model size (bigger models are better) and number of commas (more is
		// better) and length of the input texts
		// (shorter texts are better)
		candidates.sort((p1, p2) -> {
			CEGModel m1 = p1.getRight();
			CEGModel m2 = p2.getRight();
			int c = Integer.compare(m2.getContents().size(), m1.getContents().size());
			if (c != 0) {
				return c;
			}
			String t1 = p1.getLeft();
			String t2 = p2.getLeft();
			c = Integer.compare(StringUtils.countMatches(t2, ","), StringUtils.countMatches(t1, ","));
			if (c != 0) {
				return c;
			}
			return Integer.compare(t1.length(), t2.length());

		});
		originalModel.getContents().addAll(candidates.get(0).getRight().getContents());
		return true;
	}
}
