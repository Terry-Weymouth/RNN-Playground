package org.weymouth.book.demo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TrimBookLines {

	public static void main(String[] args) throws IOException {
		(new TrimBookLines()).exec();
	}

	private void exec() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("HayslopeGrange-EmmaLeslie.txt").getFile());
		List<String> lines = Files.readAllLines(file.toPath());

		int start = 87;
		int end = 3075;
		for (int i = start; i < start+20; i++) {
			System.out.println(String.format("%5d: %s", i, lines.get(i)));
		}
		
		for (int i = end-20; i < end; i++) {
			System.out.println(String.format("%5d: %s", i, lines.get(i)));
		}

//		for (int i = start; i < end; i++) {
//			System.out.println(String.format("%5d: %s", i, lines.get(i)));
//		}
		
	}

}
