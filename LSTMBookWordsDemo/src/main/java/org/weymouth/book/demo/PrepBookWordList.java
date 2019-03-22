package org.weymouth.book.demo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class PrepBookWordList {
	
	public static final String PUNCTUATION = "\\\"',.?;()[]{}:!-" + System.getProperty("line.separator");

	List<String> inputWordList;
	Map<String, Integer> wordCountMap;
	List<String> wordDictionaryList;
	Map<String, Integer> wordIndexMap;
	List<Integer> inputWordIndexList;

	public PrepBookWordList() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("HayslopeGrange-EmmaLeslie.txt").getFile());
		List<String> lines = Files.readAllLines(file.toPath());
		StringBuffer collect = new StringBuffer();
		for (int i = 87; i < 3075; i++) {
			// Note: in this doc, there are no lines ending in a hyphenated (continued) word
			// checked with this code...
			// String line = lines.get(i);
			// if (line.endsWith("-") && ! line.endsWith("--"))
			// 	System.out.println(line);
			collect.append(lines.get(i) + " ");
		}
		String inputData = collect.substring(0, collect.length()-1);
		inputData = whiteSpaceSeperatePunctuationAndCleanData(inputData);
		inputWordList = makeInputWordList(inputData);
		wordCountMap = makePunctuationAndWordCountMap(inputWordList);
		wordDictionaryList = new ArrayList<String>(wordCountMap.keySet());
		wordIndexMap = makeWordKeyMap(wordDictionaryList);
		inputWordIndexList = makeInputIndexList(wordIndexMap, inputWordList);
	}
	
	private String whiteSpaceSeperatePunctuationAndCleanData(String inputData) {
		// Somewhat imperfect. For example "son-in-law" becomes 5 words: son - in - law.
		StringBuffer output = new StringBuffer();
		for (int i = 0; i < inputData.length(); i++) {
			String probe = inputData.substring(i, i + 1);
			if (StringUtils.containsAny(PUNCTUATION, probe))
				output.append(" " + probe + " ");
			else
				output.append(probe);
		}
		String ret = output.toString();
		int len = ret.length();
		ret = ret.replaceAll("  ", " ");
		while (len < ret.length()) {
			len = ret.length();
			ret = ret.replaceAll("  ", " ");
		}
		return ret;
	}

	private List<String> makeInputWordList(String input) {
		String[] candidates = input.split(" ");
		List<String> ret = new ArrayList<String>();
		for (String c : candidates) {
			if (c.isEmpty())
				continue;
			ret.add(c);
		}
		return ret;
	}

	private Map<String, Integer> makePunctuationAndWordCountMap(List<String> inputList) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (String word : inputList) {
			Integer count = map.get(word);
			if (count == null) {
				count = new Integer(1);
			} else {
				count = new Integer(1 + count.intValue());
			}
			map.put(word, count);
		}
		return map;
	}

	private Map<String, Integer> makeWordKeyMap(List<String> keys) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			map.put(key, i);
		}
		return map;
	}

	private List<Integer> makeInputIndexList(Map<String, Integer> map, List<String> docWords) {
		List<Integer> list = new ArrayList<Integer>();
		for (String word : docWords) {
			list.add(map.get(word));
		}
		return list;
	}

}
