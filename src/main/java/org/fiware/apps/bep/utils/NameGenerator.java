package org.fiware.apps.bep.utils;

import java.text.Normalizer;

public class NameGenerator {
	
	public static String getURLName(String name) {
		
		// Normalize the String
		name = Normalizer
				.normalize(name, Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "");
		
		// Remove special characters ("-" and spaces are preserved)
		name = name.replaceAll("[^a-zA-Z0-9\\s\\-]+", "");

		// Replace " " by "-"
		name = name.replaceAll("[\\s]+", "-");
		
		// Lower case
		return name.toLowerCase();
	}

}
