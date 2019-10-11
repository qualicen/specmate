package com.specmate.common;

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
		String textALower = textA.toLowerCase();
		String textBLower = textB.toLowerCase();
		String[] arrayA = textALower.split("\\s+");
		String[] arrayB = textBLower.split("\\s+");
		Set<String> setA = Set.of(arrayA);
		Set<String> setB = Set.of(arrayB);
		return setA.equals(setB);
	}
}
