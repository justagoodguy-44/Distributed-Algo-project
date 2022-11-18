package cs451.Custom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cs451.Custom.Network.NetworkParams;


public class CompactedValueRecord {
	
	private Set<Integer> line;
	private int highestConsecutiveVal;
	
	public CompactedValueRecord() {
		this.line = new HashSet<>();
		this.highestConsecutiveVal = 0;
	}
	
	public boolean contains(int val) {
		if(val <= highestConsecutiveVal) {
			return true;
		} else {
			return line.contains(val);
		}
	}
	
	public void add(int val) {
		if(val != highestConsecutiveVal + 1) {
			line.add(val);
			return;
		}
		highestConsecutiveVal++;
		updateHighestConsecutive();
	}
	
	
	private void updateHighestConsecutive(){
		boolean checkNext = true;
		while(checkNext) {
			boolean containsNextVal = line.remove(highestConsecutiveVal+1);
			if(containsNextVal) {			
				highestConsecutiveVal++;
			} else {
				checkNext = false;
			}
		}
	}


	
	
	
}
