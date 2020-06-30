package com.specmate.test.integration;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.model.administration.ErrorCode;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;
import com.specmate.nlp.matcher.AndMatcher;
import com.specmate.nlp.matcher.AnyMatcher;
import com.specmate.nlp.matcher.ChildrenSequenceMatcher;
import com.specmate.nlp.matcher.ConstituentTypeMatcher;
import com.specmate.nlp.matcher.CoveredTextMatcher;
import com.specmate.nlp.matcher.ExactlyOneConsumer;
import com.specmate.nlp.matcher.IConstituentTreeMatcher;
import com.specmate.nlp.matcher.SequenceMatcher;
import com.specmate.nlp.matcher.ZeroOrMoreConsumer;
import com.specmate.nlp.util.EnglishSentenceUnfolder;
import com.specmate.nlp.util.GermanSentenceUnfolder;
import com.specmate.nlp.util.NLPUtil;
import com.specmate.nlp.util.SentenceUnfolderBase;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class NLPServiceTest {

	@Test
	public void testNlpServiceEnglish() throws SpecmateException {
		INLPService nlpService = getNLPService();
		JCas result = nlpService.processText("If the tool detects an error, it shows a warning window.", ELanguage.EN);

		String posString = NLPUtil.printPOSTags(result);
		Assert.assertEquals(
				"If (IN) the (DT) tool (NN) detects (VBZ) an (DT) error (NN) , (,) it (PRP) shows (VBZ) a (DT) warning (NN) window (NN) . (.)",
				posString);

		String sentText = NLPUtil.printSentences(result);
		Assert.assertEquals("(If the tool detects an error, it shows a warning window.)", sentText);

		String parseString = NLPUtil.printParse(result);
		Assert.assertEquals(
				"( ROOT ( S ( SBAR If ( S ( NP the tool ) ( VP detects ( NP an error ) ) ) ) , ( NP it ) ( VP shows ( NP a warning window ) ) . ) )",
				parseString);

		String chunkText = NLPUtil.printChunks(result);
		Assert.assertEquals(
				"If (SBAR) the tool (NP) detects (VP) an error (NP) it (NP) shows (VP) a warning window (NP)",
				chunkText);

		String depText = NLPUtil.printDependencies(result);
		// spacy uses compound, malt uses nn
		depText = depText.replace("compound", "nn");

		Assert.assertEquals("detects <--mark-- If\n" + "tool <--det-- the\n" + "detects <--nsubj-- tool\n"
				+ "shows <--advcl-- detects\n" + "error <--det-- an\n" + "detects <--dobj-- error\n"
				+ "shows <--punct-- ,\n" + "shows <--nsubj-- it\n" + "shows <--ROOT-- shows\n" + "window <--det-- a\n"
				+ "window <--nn-- warning\n" + "shows <--dobj-- window\n" + "shows <--punct-- .", depText);
	}

	@Test
	public void testNLPServiceGerman() throws SpecmateException {
		INLPService nlpService = getNLPService();
		JCas result = nlpService.processText("Wenn das Werkzeug einen Fehler erkennt, zeigt es ein Warnfenster.",
				ELanguage.DE);

		Assert.assertEquals(
				"Wenn (KOUS) das (ART) Werkzeug (NN) einen (ART) Fehler (NN) erkennt (VVFIN) , ($,) zeigt (VVFIN) es (PPER) ein (ART) Warnfenster (NN) . ($.)",
				NLPUtil.printPOSTags(result));
		Assert.assertEquals("das Werkzeug (NP) einen Fehler (NP) erkennt (VP) zeigt (VP) ein Warnfenster (NP)",
				NLPUtil.printChunks(result));

		Assert.assertEquals(
				"erkennt <--KONJ-- Wenn\n" + "Werkzeug <--DET-- das\n" + "erkennt <--SUBJ-- Werkzeug\n"
						+ "Fehler <--DET-- einen\n" + "erkennt <--OBJA-- Fehler\n" + "zeigt <--NEB-- erkennt\n"
						+ "zeigt <--ROOT-- ,\n" + "zeigt <--ROOT-- zeigt\n" + "zeigt <--SUBJ-- es\n"
						+ "Warnfenster <--DET-- ein\n" + "zeigt <--OBJA-- Warnfenster\n" + ". <--ROOT-- .",
				NLPUtil.printDependencies(result));

		Assert.assertEquals(
				"( ROOT ( S ( S Wenn ( NP das Werkzeug ) ( NP einen Fehler ) ( S erkennt ) ) , zeigt es ( NP ein Warnfenster ) . ) )",
				NLPUtil.printParse(result));
	}

	@Test
	public void testSentenceUnfoldingEnglish() throws SpecmateException {
		INLPService nlpService = getNLPService();
		EnglishSentenceUnfolder unfolder = new EnglishSentenceUnfolder(nlpService);

		String text = "If the tool has an error or fails, the tool alerts the user and shows a window.";
		List<String> unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "").equals(
				"If the tool has an error or the tool fails the tool alerts the user and the tool shows a window.")));

		text = "The magazine contains the nicest hiking tours and trips.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "")
				.equals("The magazine contains the nicest hiking tours and The magazine contains trips.")));

		text = "If the field is empty or contains errors the button is greyed out.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "")
				.equals("If the field is empty or the field contains errors the button is greyed out.")));

		text = "The window and the list contain an entry.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(
				u -> u.replace(",", "").equals("The window contain an entry and the list contain an entry.")));

		text = "If the user clicks the button, the tool shows a window and saves the changes.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "")
				.equals("If the user clicks the button the tool shows a window and the tool saves the changes.")));

	}

	@Test
	public void testSentenceUnfoldingGerman() throws SpecmateException {
		INLPService nlpService = getNLPService();
		SentenceUnfolderBase unfolder = new GermanSentenceUnfolder(nlpService);

		// Ensure replace is correct
		String text = "Wenn das Werkzeug fehlschlägt oder einen Fehler oder ein Problem hat, dann zeigt das Werkzeug ein Warnfenster und einen Fehlermarker an und gibt eine Meldung aus.";
		List<String> unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "").equals(
				"Wenn das Werkzeug fehlschlägt oder wenn das Werkzeug einen Fehler hat oder wenn das Werkzeug ein Problem hat dann zeigt das Werkzeug ein Warnfenster und zeigt das Werkzeug einen Fehlermarker an und gibt das Werkzeug eine Meldung aus.")));

//		 Ensure nothing changes when applied on the already replace text
//		text = "Wenn das Werkzeug fehlschlägt, oder wenn das Werkzeug einen Fehler hat, oder wenn das Werkzeug ein Problem hat, dann zeigt das Werkzeug ein Warnfenster, und  zeigt das Werkzeug einen Fehlermarker an, und gibt das Werkzeug eine Meldung aus.";
//		unfolded = unfolder.unfold(nlpService, text, ELanguage.DE);
//		Assert.assertEquals(
//				"Wenn das Werkzeug fehlschlägt, oder wenn das Werkzeug einen Fehler hat, oder wenn das Werkzeug ein Problem hat, dann zeigt das Werkzeug ein Warnfenster, und dann zeigt das Werkzeug einen Fehlermarker an, und dann gibt das Werkzeug eine Meldung aus.",
//				unfolded);

		text = "Das Magazin hat die schönsten Wanderungen und Ausflugziele.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "")
				.equals("Das Magazin hat die schönsten Wanderungen und Das Magazin hat Ausflugziele.")));

		text = "Felix Lindner legte sein Abitur ab und nahm dann ein Studium der neueren Sprachen auf.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "").equals(
				"Felix Lindner legte sein Abitur ab und Felix nahm dann ein Studium der neueren Sprachen auf.")));

		text = "Der sowjetische Chirurg Fjodorow reist im Jahr 1982 nach Kabul, um dort Vorlesungen zu halten und um in einem Militärkrankenhaus zu arbeiten.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "").equals(
				"Der sowjetische Chirurg Fjodorow reist im Jahr 1982 nach Kabul um dort Vorlesungen zu halten und um in einem Militärkrankenhaus zu arbeiten.")));

		text = "Er versorgt Verwundete beider Seiten und befragt sie nach ihren Erfahrungen und persönlichen Lebensumständen.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "").equals(
				"Er versorgt Verwundete beider Seiten und Er befragt sie nach ihren Erfahrungen und persönlichen Lebensumständen.")));

		text = "Der häufige Blätterpilz wächst im Herbst und fruktifiziert gerne in Hexenringen.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "").equals(
				"Der häufige Blätterpilz wächst im Herbst und Der häufige Blätterpilz fruktifiziert gerne in Hexenringen.")));

		text = "Das Fenster und die Liste enthalten einen Eintrag.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "")
				.equals("Das Fenster enthalten einen Eintrag und die Liste enthalten einen Eintrag.")));

		text = "Wenn das Werkzeug einen Fehler oder ein Problem erkennt oder der Benutzer keine Anmeldung hat, zeigt das Werkzeug ein Warnfenster an und gibt einen Signalton aus.";
		unfolded = unfolder.unfold(text);
		Assert.assertTrue(unfolded.stream().anyMatch(u -> u.replace(",", "").equals(
				"Wenn das Werkzeug einen Fehler erkennt oder wenn das Werkzeug ein Problem erkennt oder der Benutzer keine Anmeldung hat zeigt das Werkzeug ein Warnfenster an und gibt das Werkzeug einen Signalton aus.")));

	}

	@Test
	public void testMatcher() throws SpecmateException {
		String text = "If the tool detects an error, then consequently it shows a warning window.";
		String expectedCause = "the tool detects an error";
		String expectedEffect = "it shows a warning window.";

		checkCauseEffect(text, ELanguage.EN, expectedCause, expectedEffect);

		text = "If Specmate detects an error or the user has no login, Specmate shows a warning window and makes a sound.";
		expectedCause = "Specmate detects an error or the user has no login";
		expectedEffect = "Specmate shows a warning window and makes a sound.";

		checkCauseEffect(text, ELanguage.EN, expectedCause, expectedEffect);

		text = "If Specmate detects an error or if the user has no login, Specmate shows a warning window and makes a sound.";
		expectedCause = "Specmate detects an error or if the user has no login";
		expectedEffect = "Specmate shows a warning window and makes a sound.";

		checkCauseEffect(text, ELanguage.EN, expectedCause, expectedEffect);

	}

	private void checkCauseEffect(String text, ELanguage language, String expectedCause, String expectedEffect)
			throws SpecmateException {
		INLPService nlpService = getNLPService();
		JCas result = nlpService.processText(text, language);
		System.out.println(NLPUtil.printParse(result));
		Constituent cons = NLPUtil.getSentenceConstituents(result).get(0);

		IConstituentTreeMatcher matcher = new ChildrenSequenceMatcher(new SequenceMatcher(Arrays.asList(
				new ExactlyOneConsumer(result,
						new AndMatcher(new ConstituentTypeMatcher("SBAR"),
								new CoveredTextMatcher("If\\s*(.*)", "cause"))),
				new ZeroOrMoreConsumer(result, new CoveredTextMatcher(",")),
				new ZeroOrMoreConsumer(result, new ConstituentTypeMatcher("ADVP")),
				new ZeroOrMoreConsumer(result, new AnyMatcher(), "effect"))));
		com.specmate.nlp.matcher.MatchResult matchResult = matcher.match(cons);
		Assert.assertTrue(matchResult.isMatch());
		String effect = matchResult.getMatchGroupAsText("effect");
		String cause = matchResult.getMatchGroupAsText("cause");

		Assert.assertEquals(expectedCause, cause);
		Assert.assertEquals(expectedEffect, effect);
	}

	public static INLPService getNLPService() throws SpecmateException {
		BundleContext context = FrameworkUtil.getBundle(NLPServiceTest.class).getBundleContext();
		ServiceTracker<INLPService, INLPService> nlpServiceTracker = new ServiceTracker<>(context,
				INLPService.class.getName(), null);
		nlpServiceTracker.open();
		INLPService nlpService;
		try {
			nlpService = nlpServiceTracker.waitForService(20000);
		} catch (InterruptedException e) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION, e);
		}
		Assert.assertNotNull(nlpService);
		return nlpService;
	}

}
