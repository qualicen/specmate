package com.specmate.nlp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.specmate.common.exception.SpecmateException;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * Base class to unfold sentences by adding implicit subjects, predicates and
 * objects
 */
public abstract class SentenceUnfolderBase {

	/** Order of subject, verb, object */
	protected enum EWordOrder {
		SOV, VS, SVO, SV
	}

	/** Role of a noun, either subject or object */
	protected enum ENounRole {
		SUBJ, OBJ
	}

	/** Service for NLP processing */
	private INLPService nlpService;

	/** Language used for processing */
	private ELanguage language;

	public SentenceUnfolderBase(INLPService nlpService, ELanguage language) {
		this.nlpService = nlpService;
		this.language = language;
	}

	protected JCas processText(String text) throws SpecmateException {
		return nlpService.processText(text, language);
	}

	/**
	 * Unfolds a sentence by first adding implicit verbs and then adding implicit
	 * subjects
	 */
	public List<String> unfold(String text) throws SpecmateException {
		String original = text;
		String noComma = text.replace(",", "");
		String allComma = insertCommasBeforeConjunctions(text);
		Set<String> result = new HashSet<>();
		for (String variant : Arrays.asList(original, noComma, allComma)) {
			List<String> unfolded = doUnfold(variant);
			result.addAll(unfolded);
		}
		return new ArrayList<String>(result);
	}

	private List<String> doUnfold(String text) throws SpecmateException {
		JCas jCas = nlpService.processText(text, language);

		String unfoldedText1 = "";
		String unfoldedText2 = "";
		String unfoldedText3 = "";

		for (Sentence origSentence : NLPUtil.getSentences(jCas)) {
			String unfoldedCC = insertImplicitConjunctions(origSentence.getCoveredText());
			String unfoldedImplicitNouns = insertImplicitNouns(unfoldedCC);
			String unfoldedImplicitVerbs = insertsImplicitVerbs(unfoldedImplicitNouns);
			String unfoldedImplicitSubjects = insertImplicitSubjects(unfoldedImplicitVerbs);

			String result1 = insertCommasBeforeConjunctions(unfoldedImplicitSubjects);
			String result2 = unfoldedImplicitSubjects;
			String result3 = unfoldedImplicitSubjects.replace(",", "");

			unfoldedText1 += " " + result1;
			unfoldedText2 += " " + result2;
			unfoldedText3 += " " + result3;
		}
		return Arrays.asList(unfoldedText1.trim(), unfoldedText2.trim(), unfoldedText3.trim());
	}

	private String insertImplicitConjunctions(String text) throws SpecmateException {
		JCas jCas = processText(text);
		List<Pair<Integer, String>> insertions = new ArrayList<>();
		List<Annotation> conjunctionsWithoutConnectives = identifyConjunctionsWithoutConnectives(jCas);
		for (Annotation annotation : conjunctionsWithoutConnectives) {
			Optional<Annotation> optConnective = getNearestForwardConnective(jCas, annotation);
			if (optConnective.isPresent()) {
				Annotation connective = optConnective.get();
				String connectiveText = connective.getCoveredText();
				int insertionPoint = annotation.getBegin();
				insertions.add(Pair.of(insertionPoint, connectiveText));
			}
		}
		return insert(text, insertions);
	}

	protected abstract Optional<Annotation> getNearestForwardConnective(JCas jcas, Annotation annotation);

	protected abstract List<Annotation> identifyConjunctionsWithoutConnectives(JCas jCas);

	protected abstract String insertCommasBeforeConjunctions(String unfoldedStageC);

	/**
	 * Insert Nouns into adjective conjunctions
	 *
	 * @throws SpecmateException
	 */
	private String insertImplicitNouns(String text) throws SpecmateException {
		JCas jCas = processText(text);
		Sentence sentence = NLPUtil.getSentences(jCas).iterator().next();

		List<Chunk> nounPhrases = NLPUtil.getNounPhraseChunks(jCas, sentence);
		List<Pair<Integer, String>> insertions = new ArrayList<Pair<Integer, String>>();

		for (Annotation np : nounPhrases) {
			List<Dependency> modifiers = getConjunctiveAdjectiveModifyers(jCas, np);
			insertions.addAll(completeConjunctiveAdjectiveNounPhrase(jCas, np, modifiers));
		}
		return insert(sentence.getCoveredText(), insertions);
	}

	/**
	 * Inserts implicit subjects into a sentence
	 *
	 * @throws SpecmateException
	 */
	private String insertImplicitSubjects(String text) throws SpecmateException {

		JCas jCas = processText(text);
		Sentence sentence = NLPUtil.getSentences(jCas).iterator().next();

		List<Pair<Integer, String>> insertions = new ArrayList<Pair<Integer, String>>();
		List<Chunk> vpws = findVerbalPhraseWithoutSubject(jCas, sentence);
		for (Chunk vp : vpws) {
			Optional<Pair<Annotation, EWordOrder>> optImplicitSubjectAndOrder = findImplicitVerbSubjectByConjunction(
					jCas, vp);
			if (optImplicitSubjectAndOrder.isPresent()) {
				Annotation implicitSubject = optImplicitSubjectAndOrder.get().getLeft();
				EWordOrder wordOrder = optImplicitSubjectAndOrder.get().getRight();
				int ip = determineSubjectInsertionPoint(jCas, vp, wordOrder);

				List<Pair<Annotation, Annotation>> allConjunctedImplicitSubjects = completeSubjectsByConjunction(jCas,
						implicitSubject);

				Optional<Annotation> optAssociatedConditional = getAssociatedSubjectConditional(jCas, implicitSubject);
				if (optAssociatedConditional.isPresent()) {
					String assConditionalWordText = optAssociatedConditional.get().getCoveredText().toLowerCase();
					insertions.add(Pair.of(ip, assConditionalWordText));
				}

				for (Pair<Annotation, Annotation> toInsert : allConjunctedImplicitSubjects) {
					Annotation conjunctionWord = toInsert.getLeft();
					Annotation subjectWord = toInsert.getRight();

					String conjunctionWordText = conjunctionWord != null ? conjunctionWord.getCoveredText() + " " : "";

					insertions.add(Pair.of(ip, conjunctionWordText + subjectWord.getCoveredText()));
				}
			}
		}
		return insert(sentence.getCoveredText(), insertions);
	}

	/**
	 * Inserts implicit verbs into a sentence
	 *
	 * @throws SpecmateException
	 */
	private String insertsImplicitVerbs(String text) throws SpecmateException {

		JCas jCas = processText(text);
		Sentence sentence = NLPUtil.getSentences(jCas).iterator().next();

		List<Pair<Integer, String>> insertions = new ArrayList<Pair<Integer, String>>();
		List<Annotation> npwv = findNounPhraseWithoutVerb(jCas, sentence);
		for (Annotation np : npwv) {
			Optional<Triple<Annotation, EWordOrder, ENounRole>> optImplicitVerbs = findImplicitVerbByConjunction(jCas,
					np);
			// TODO: collect also connected verbs (conjunction)
			if (optImplicitVerbs.isPresent()) {
				Triple<Annotation, EWordOrder, ENounRole> toInsert = optImplicitVerbs.get();

				Annotation implicitVerb = toInsert.getLeft();
				EWordOrder wordOrder = toInsert.getMiddle();
				ENounRole nounRole = toInsert.getRight();

				int ip = determineVerbInsertionPoint(jCas, np, implicitVerb, wordOrder, nounRole);
				String addObject = "";
				if (nounRole == ENounRole.SUBJ) {
					Optional<Dependency> optObjDep = findObjectDependency(jCas, implicitVerb, true);
					if (optObjDep.isPresent()) {
						Annotation objNounPhrase = NLPUtil.getCoveringNounPhraseOrToken(jCas,
								optObjDep.get().getDependent());
						addObject = " " + objNounPhrase.getCoveredText();
					}
				}

				insertions.add(Pair.of(ip, implicitVerb.getCoveredText() + addObject));
			}
		}
		return insert(sentence.getCoveredText(), insertions);
	}

	/** Finds noun phrases without a dependency (object or subject) to a verb */
	private List<Annotation> findNounPhraseWithoutVerb(JCas jCas, Sentence sentence) {
		List<Annotation> result = new ArrayList<>();
		List<Chunk> nounPhrases = NLPUtil.getNounPhraseChunks(jCas, sentence);
		for (Annotation np : nounPhrases) {
			Optional<Dependency> optVerbDependency = findVerbForNounPhrase(jCas, np);
			if (!optVerbDependency.isPresent()) {
				result.add(np);
			}
		}
		return result;
	}

	/** Finds verb phrases without a subject dependency to a noun */
	private List<Chunk> findVerbalPhraseWithoutSubject(JCas jCas, Sentence sentence) {
		List<Chunk> result = new ArrayList<>();
		List<Chunk> verbPhrases = NLPUtil.getVerbPhraseChunks(jCas, sentence);
		for (Chunk vp : verbPhrases) {
			Optional<Dependency> subject = findSubjectDependency(jCas, vp, true);
			if (!subject.isPresent()) {
				result.add(vp);
			}
		}
		return result;
	}

	/** Performs the given set of insertion commands */
	private static String insert(String text, List<Pair<Integer, String>> insertions) {
		StringBuffer buffer = new StringBuffer(text);
		int indexCorrection = 0;
		insertions.sort((p1, p2) -> p1.getLeft().compareTo(p2.getLeft()));
		for (Pair<Integer, String> insertion : insertions) {
			int index = insertion.getLeft() + indexCorrection;
			String toInsert = insertion.getRight() + " ";
			buffer.insert(index, toInsert);
			indexCorrection += toInsert.length();
		}
		return buffer.toString();
	}

	/** Find all adjective conjunctions of the given nounphrase */
	protected abstract List<Dependency> getConjunctiveAdjectiveModifyers(JCas jCas, Annotation np);

	/**
	 * Distribute the nounphrase over all adjective conjuncitons.
	 */
	protected abstract List<Pair<Integer, String>> completeConjunctiveAdjectiveNounPhrase(JCas jCas, Annotation np,
			List<Dependency> modifiers);

	/**
	 * Determines an implicit subject for a verb phrase by finding conjuncted verbs
	 * and there subjects
	 */
	protected abstract Optional<Pair<Annotation, EWordOrder>> findImplicitVerbSubjectByConjunction(JCas jCas, Chunk vp);

	/** Finds all conjuncted noun phrases for a given subject */
	protected abstract List<Pair<Annotation, Annotation>> completeSubjectsByConjunction(JCas jCas, Annotation subj);

	/** Finds a subject dependendency for the given token or chunk */
	protected abstract Optional<Dependency> findSubjectDependency(JCas jCas, Annotation vp, boolean isGovernor);

	/**
	 * Determins where to insert a subject for the given verb phrase, given a
	 * certain word order
	 */
	protected abstract int determineSubjectInsertionPoint(JCas jcas, Chunk vp, EWordOrder order);

	/**
	 * Determines an implicit verb for a given noun phrase by finding conjuncted
	 * nouns and there verbs
	 */
	protected abstract Optional<Triple<Annotation, EWordOrder, ENounRole>> findImplicitVerbByConjunction(JCas jCas,
			Annotation np);

	/** Finds an object or subject dependency for a noun phrase */
	protected abstract Optional<Dependency> findVerbForNounPhrase(JCas jCas, Annotation np);

	/**
	 * Determines where to insert a verb for the given noun phrase, the original
	 * verb, a word order and the role of the noun
	 */
	protected abstract int determineVerbInsertionPoint(JCas jcas, Annotation np, Annotation verb, EWordOrder order,
			ENounRole role);

	/**
	 * Determines an object dependency associated with the given annoation.
	 */
	protected abstract Optional<Dependency> findObjectDependency(JCas jCas, Annotation anno, boolean isGovernor);

	/**
	 * Determines an associated conditional word associated with a subject
	 */
	protected abstract Optional<Annotation> getAssociatedSubjectConditional(JCas jCas, Annotation implicitSubject);

}
