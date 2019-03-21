package org.weymouth.playing.words;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Main {

	public static void main(String[] args) throws Exception {
		(new Main()).exec();
	}

	private void exec() throws IOException {
		String hyphonatedWord = null;
		HashMap<String, Integer> words = new HashMap<String, Integer>();
		
		File file = new File("Shakespeare.txt");
		if ( ! file.exists() && ! file.canRead()) {
			System.out.println("Can't read file.");
		}
		List<String> lines = Files.readAllLines(file.toPath());
		for (String line: lines) {
			if (hyphonatedWord != null) {
				line = hyphonatedWord + line;
				hyphonatedWord = null;
			}
			String[] lineWords = line.split(" ");
			String lastWord = lineWords[lineWords.length-1];
			if (lastWord.endsWith("-") && (! lastWord.endsWith("--"))) {
				hyphonatedWord = lastWord.substring(0, lastWord.length()-1);
				lineWords[lineWords.length-1] = null;
			}
			for(String s: lineWords) {
				if (s != null && s.length() != 0) {
					s = s.replaceAll("[^\\-\\p{L}\\p{Z}]","");
					if (s.endsWith("--")) s = s.substring(0,s.length()-2);
					if (s.endsWith("-")) s = s.substring(0,s.length()-1);
					if (s.length() != 0) {
						s = s.toLowerCase();
						Integer count = words.get(s);
						if (count == null) {
							count = new Integer(1);
						} else {
							count = new Integer(1 + count.intValue());
						}
						words.put(s, count);
					}
				}
			}
		}
		List<String> wordList = new ArrayList<String>(words.keySet());
		Collections.sort(wordList);
		for (String word: wordList) {
			if (words.get(word).intValue() > 20)
				System.out.println(String.format("%s,  %d", word, words.get(word).intValue()));
		}
		System.out.println(wordList.size());
	}

}
