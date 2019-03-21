package org.weymouth.book.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class TrigramAndPunctuationCount {
	
	public static final String PUNCTUATION = "\\\"',.?;()[]{}:!-" + System.getProperty("line.separator");

	private Map<String,Integer> map = new HashMap<String,Integer>();
	
	public static void main(String[] args) throws Exception {
		(new TrigramAndPunctuationCount()).exec();
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
				
		int len = inputData.length();
		inputData = inputData.replace("  ", " ");
		while (len > inputData.length()) {
			len = inputData.length();
			inputData = inputData.replace("  ", " ");
		}		
		
		for (int i = 0; i < inputData.length()-2; i++) {
			String gram = inputData.substring(i, i+3);
			if (StringUtils.containsAny(gram, PUNCTUATION)) {
				for (int j = 0; j < gram.length(); j++) {
					String letter = gram.substring(j, j+1);
					if (StringUtils.containsAny(PUNCTUATION,letter)) {
						accountFor(letter);
					}
				}
				continue;
			}
			accountFor(gram);
			Integer count = map.get(gram);
			if (count == null) {
				count = new Integer(1);
			} else {
				count = new Integer(1 + count.intValue());
			}
			map.put(gram, count);
		}
		for (String key: map.keySet()) {
			System.out.println(String.format("%3s: %d", key, map.get(key).intValue()));
		}
		
		System.out.println(map.size());
		
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
