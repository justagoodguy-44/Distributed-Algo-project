package cs451.Custom.Lattice;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class LatticeReader {
	
	private File latticeFile;
	private Scanner latticeFileScanner;
	
	public LatticeReader(String path) {
		latticeFile = new File(path);
		try {
			latticeFileScanner = new Scanner(latticeFile);
			//Skip first line
			latticeFileScanner.nextLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	public List<HashSet<Integer>> read(int nbOfLines) {
		int nbRead = 0;
		List<HashSet<Integer>> linesContents = new LinkedList<>();
		while(nbRead < nbOfLines && latticeFileScanner.hasNextLine()) {
			String line = latticeFileScanner.nextLine();
			String[] strVals = line.split(" ");
			HashSet<Integer> intVals = new HashSet<>();
			for(int i = 0; i < strVals.length; ++i) {
				intVals.add(Integer.parseInt(strVals[i]));
			}
			linesContents.add(intVals);
		}
		return linesContents;
	}
	
	public void close() {
		latticeFileScanner.close();
	}

	
	

}
