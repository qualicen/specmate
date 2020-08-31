package com.specmate.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TextUtil {

	/**
	 * Compares, if two strings contains the same words. Case-sensitivity is
	 * irrelevant.
	 *
	 * @param textA
	 * @param textB
	 * @return
	 */
	public static boolean textsContainSameWords(String textA, String textB) {
		if (textA.equalsIgnoreCase(textB)) {
			return true;
		}
		if (textA.equals("*") || textB.equals("*")) {
			return true;
		}
		String textALower = textA.toLowerCase();
		String textBLower = textB.toLowerCase();
		String[] arrayA = textALower.split("\\s+");
		String[] arrayB = textBLower.split("\\s+");
		Set<String> setA = new HashSet<>(Arrays.asList(arrayA));
		Set<String> setB = new HashSet<>(Arrays.asList(arrayB));

		return setA.equals(setB);
	}
}
