package comun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import apertiumV2.GetDataV2;


public class CalculateSingleCycle {
	
	
	/**
	 *  It takes two languages as input (input and output languages for which discover cycles-based translations)
	 *  and a root word.
	 *  
	 *  For the root word its context is obtained (that is, the chain of translations in the translation graph that 
	 *  starts from such a root term). Then, the program discovers the cycles to which the root term belongs to in the graph 
	 *  and calculate the cycle density. The output results
	 *  (candidate translations, along with their score and cycle path), are shown on screen
	 *  
	 */				
	public static void main(String [] args) {
	
		String langS = "en";
		String langT = "fr";
		
		String root = "dog";
		ArrayList <CycleDensity> cycles;
	
		Context context = GetDataV2.getContext(root, langS, langT);
		cycles = CalculateCycles.getCycles(root, context, langS, langT);
		for (CycleDensity c : cycles) {
			if (c.getTarget().substring(c.getTarget().lastIndexOf("-") + 1).equals(langT)) {
				c.print();
			}
		}
	}	
	

}
