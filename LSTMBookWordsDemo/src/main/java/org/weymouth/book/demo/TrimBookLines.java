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
		
		for (int i = 87; i < 3075; i++) {
			System.out.println(String.format("%5d: %s", i, lines.get(i)));
		}
		
//		for (int i = 0; i < 3075; i++) {
//			System.out.println(String.format("%5d: %s", i, lines.get(i)));
//		}
		
	}

}
