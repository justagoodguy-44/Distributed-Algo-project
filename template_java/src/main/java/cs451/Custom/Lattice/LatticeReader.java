package cs451.Custom.Lattice;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class LatticeReader {
	
	private File latticeFile;
	
	public LatticeReader(String path) {
		latticeFile = new File(path);
	}
	
	public List<HashSet<Integer>> read(int nbOfLines) {
		Scanner scanner;
		try {
			scanner = new Scanner(latticeFile);
			int nbRead = 0;
			List<HashSet<Integer>> linesContents = new LinkedList<>();
			while(nbRead < nbOfLines && scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] strVals = line.split(" ");
				HashSet<Integer> intVals = new HashSet<>();
				for(int i = 0; i < strVals.length; ++i) {
					intVals.add(Integer.parseInt(strVals[i]));
				}
			}
			scanner.close();
			return linesContents;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	

}
