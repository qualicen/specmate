package com.specmate.modelgeneration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.osgi.service.log.LogService;

import com.specmate.cause_effect_patterns.parse.matcher.MatchResult;
import com.specmate.cause_effect_patterns.parse.wrapper.BinaryMatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchTreeBuilder;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.config.api.IConfigService;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.requirements.CEGModel;
import com.specmate.modelgeneration.stages.GraphBuilder;
import com.specmate.modelgeneration.stages.GraphLayouter;
import com.specmate.modelgeneration.stages.MatcherPostProcesser;
import com.specmate.modelgeneration.stages.RuleMatcher;
import com.specmate.modelgeneration.stages.TextPreProcessor;
import com.specmate.modelgeneration.stages.graph.Graph;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;

public class PatternbasedCEGGenerator implements ICEGFromRequirementGenerator {
	private final INLPService tagger;
	private final CEGCreation creation;
	private final ELanguage lang;
	private final RuleMatcher matcher;
	private final LogService log;
	private final TextPreProcessor preProcessor;

	public PatternbasedCEGGenerator(ELanguage lang, INLPService tagger, IConfigService configService,
			LogService logService) throws SpecmateException {
		this.tagger = tagger;
		creation = new CEGCreation();
		this.lang = lang;
		matcher = new RuleMatcher(this.tagger, configService, lang);
		log = logService;
		preProcessor = new TextPreProcessor(lang, tagger);
	}

	@Override
	public CEGModel createModel(CEGModel originalModel, String input) throws SpecmateException {
		log.log(LogService.LOG_INFO, "Textinput: " + input);
		List<String> texts = preProcessor.preProcess(input);
		List<Pair<String, CEGModel>> candidates = new ArrayList<>();

		for (String text : texts) {
			log.log(LogService.LOG_INFO, "Text Pre Processing: " + text);
			final List<MatchResult> results = matcher.matchText(text);

			final MatchTreeBuilder builder = new MatchTreeBuilder();

			// Convert all successful match results into an intermediate representation
			final List<MatchResultTreeNode> trees = results.stream().filter(MatchResult::isSuccessfulMatch)
					.map(builder::buildTree).filter(Optional::isPresent).map(Optional::get)
					.collect(Collectors.toList());

			final MatcherPostProcesser matchPostProcesser = new MatcherPostProcesser(lang);
			GraphBuilder graphBuilder = new GraphBuilder();
			GraphLayouter graphLayouter = new GraphLayouter(lang, creation);

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
					log.log(LogService.LOG_DEBUG,
							"Error occured processing the dependency parse tree: " + t.getMessage(), t);
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
		return originalModel;
	}
}
