package cs451.Custom.Helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConfigReaderLattice {
	
	private File configFile;
	
	private int nbOfProposals;
	private int maxValuesPerProposal;
	private int maxTotalDifferentVals;
	
	public ConfigReaderLattice(String path) {
		configFile = new File(path);
	}
	
	
	public void extractValuesLattice() {
		Scanner scanner;
		try {
			scanner = new Scanner(configFile);
			nbOfProposals = scanner.nextInt();
			maxValuesPerProposal = scanner.nextInt();
			maxTotalDifferentVals = scanner.nextInt();
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public int getNbOfProposals() {
		return nbOfProposals;
	}

	public int getMaxValuesPerProposal() {
		return maxValuesPerProposal;
	}
	
	public int getMaxTotalDifferentVals() {
		return maxTotalDifferentVals;
	}
}
