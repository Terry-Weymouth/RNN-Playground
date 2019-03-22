package org.weymouth.book.exploration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordCount {
	
	public static final String PUNCTUATION = "\\\"',.?;()[]{}:!-" + System.getProperty("line.separator");

	private Map<String,Integer> map = new HashMap<String,Integer>();
	
	public static void main(String[] args) throws Exception {
		(new WordCount()).exec();
	}

	private void exec() throws FileNotFoundException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("HayslopeGrange-EmmaLeslie.txt").getFile());
		List<String> lines = Files.readAllLines(file.toPath());
		StringBuffer collect = new StringBuffer();
		for (String line: lines) {
			collect.append(line + " ");
		}
		String inputData = collect.substring(0, collect.length()-1);
		
		for (int i = 0; i < PUNCTUATION.length(); i++) {
			String one = PUNCTUATION.substring(i, i+1);
			inputData = inputData.replace(one, " ");
		}
		
		int len = inputData.length();
		inputData = inputData.replace("  ", " ");
		while (len > inputData.length()) {
			len = inputData.length();
			inputData = inputData.replace("  ", " ");
		}		

		// inputData = inputData.toLowerCase();
		
		String[] words = inputData.split(" ");
		
		for (int i = 0; i < words.length; i++) {
			accountFor(words[i]);
		}
		
		for (String key: map.keySet()) {
			System.out.println(String.format("%3s: %d", key, map.get(key).intValue()));
		}
		
		System.out.println(map.size());
		System.out.println(PUNCTUATION.length());
		System.out.println(map.size() + PUNCTUATION.length());
	}
	
	private void accountFor(String gram) {
		Integer count = map.get(gram);
		if (count == null) {
			count = new Integer(1);
		} else {
			count = new Integer(1 + count.intValue());
		}
		map.put(gram, count);
	}

}
