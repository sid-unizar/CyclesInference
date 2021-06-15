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


public class CalculateCycles {
	
	/**
	 * Given a root 'word' and its 'context' identify and evaluate potential targets.
	 * Context contains the list of translation pairs computed by the class apertium.GetData
	 * Potential targets are those words in the graph that:
	 * (i) occur in some cycle together with the root word and
	 * (ii) are not (directly) linked to the root.
	 * Then, write the results in a file ('writer')
	 */	
	public static void getCycles(String word, Context context, String lang1, String lang2, PrintWriter writer) {
		if (!context.getPairs().isEmpty()) {
			String posLexinfo = context.getPairs().get(0).getPos();
			String posApertium = context.getPairs().get(0).getPosApertium();
			String root = word + "-" + posApertium + "-" + lang1; // Rewrite input word following 'Apertium format'.
			ArrayList<String> apertium = new ArrayList<>(); //Used to store already known translations for root.
			ArrayList<String> pila = new ArrayList<>(); //Used to control 'already visited nodes'.
			ArrayList<ArrayList<String>> cycles = new ArrayList<>();			
			for (TranslatablePair pair : context.getPairs()) { //Read pairs in context and trigger findCycles for each X-Y pair where X=root.
				if (pair.getSourceLabel().equals(root)) {
					cycles = findCycles(pair.getTargetLabel(), pila, context.getPairs(), root, cycles);
					apertium.add(pair.getTargetLabel());
				}
			}
			if (cycles.size() > 0) {
				ArrayList<ArrayList<String>> cleanedCycles = cleanCycles(cycles, root, apertium); // Remove 'duplicates' in cycles (abc=cba; abcd=abdc) and require len(cycles) > 5 for big contexts (more than 5 known translations)
				if (cleanedCycles.size() > 0) {					
					ArrayList<String> targets = findTargets(cleanedCycles, root, context.getPairs(), lang1); //Identify potential Targets in cycles (nodes not linked to root)					
					ArrayList<CycleDensity> cyclesDensity = calculateDensity(cleanedCycles, targets, context.getPairs()); //Calculate the cycle density for each potential target. For each cycle with a potential target we get: target, density and cycle
					ArrayList<CycleDensity> cycles_dict = compute_results(cyclesDensity); //Compute the final score
					for (CycleDensity c : cycles_dict) {
						if (c.getTarget().substring(c.getTarget().lastIndexOf("-") + 1).equals(lang2)) {
							c.print();
							//c.printToFile(writer, word, posLexinfo);
							c.printToFile(writer, word, posLexinfo, posApertium);
						}
					}
				}
			}
		}		
	}	
	
	
	/**
	 * Given a root 'word' and its 'context' identify and evaluate potential targets.
	 * Context contains the list of translation pairs computed by the class apertium.GetData
	 * Potential targets are those words in the graph that:
	 * (i) occur in some cycle together with the root word and
	 * (ii) are not (directly) linked to the root.
	 * Then, return the discovered cycles as an array list
	 */	
	public static ArrayList<CycleDensity> getCycles(String word, Context context, String langS, String langT) {
		
		ArrayList<CycleDensity> returned_cycles =  new ArrayList<>();	
		
		if (!context.getPairs().isEmpty()) {
		//	String posLexinfo = context.getPairs().get(0).getPos();
			String posApertium = context.getPairs().get(0).getPosApertium();
			String root = word + "-" + posApertium + "-" + langS; // Rewrite input word following 'Apertium format'.
			ArrayList<String> apertium = new ArrayList<>(); //Used to store already known translations for root.
			ArrayList<String> pila = new ArrayList<>(); //Used to control 'already visited nodes'.
			ArrayList<ArrayList<String>> cycles = new ArrayList<>();			
			for (TranslatablePair pair : context.getPairs()) { //Read pairs in context and trigger findCycles for each X-Y pair where X=root.
				if (pair.getSourceLabel().equals(root)) {
					cycles = findCycles(pair.getTargetLabel(), pila, context.getPairs(), root, cycles);
					apertium.add(pair.getTargetLabel());
				}
			}
			if (cycles.size() > 0) {
				ArrayList<ArrayList<String>> cleanedCycles = cleanCycles(cycles, root, apertium); // Remove 'duplicates' in cycles (abc=cba; abcd=abdc) and require len(cycles) > 5 for big contexts (more than 5 known translations)
				if (cleanedCycles.size() > 0) {					
					ArrayList<String> targets = findTargets(cleanedCycles, root, context.getPairs(), langS); //Identify potential Targets in cycles (nodes not linked to root)					
					ArrayList<CycleDensity> cyclesDensity = calculateDensity(cleanedCycles, targets, context.getPairs()); //Calculate the cycle density for each potential target. For each cycle with a potential target we get: target, density and cycle
					ArrayList<CycleDensity> cycles_dict = compute_results(cyclesDensity); //Compute the final score
					returned_cycles = cycles_dict;
				}
			} 
			
		}
		return returned_cycles;		
		
	}	
	
	
	/**
	 * 
	 * Find cycles in pairs(Context) containing root
	 * 
	 * This is a recursive function that starts with node in a 'root-node' pair
     * and ends when root is reached (node-root pair).
	 */
	public static ArrayList<ArrayList<String>> findCycles(String node, ArrayList<String> pila, ArrayList<TranslatablePair> pairs, String root, ArrayList<ArrayList<String>> cycles) {		
		ArrayList<String> pila2add = new ArrayList<>();		
		pila.add(node); //Add node to potential cycle
		int n = 0 ;			
		for (TranslatablePair p : pairs){ //Look for node/Y pairs in the pairs of Context			
			if(p.getSourceLabel().equals(node) && pila.size()<7) { //len(cycles) restricted to < 7 to avoid computation problems				
				if(p.getTargetLabel().equals(root)) { //Termination: When pair is "X->root", we reach the end of cycle					
					if(pila.size()>1) {	//We want cycles bigger than 2 nodes.		
						for(String m : pila) 
							pila2add.add(m);												
						cycles.add(pila2add);
					}					
				}else {					
					if(!pila.contains(p.getTargetLabel())) //Check that node is not repeated in cycle
						findCycles(p.getTargetLabel(), pila, pairs, root, cycles);					
				}				
			}			
			n++;
			if(n == pairs.size()) 
				pila.remove(pila.lastIndexOf(node));			
		}		
		return cycles;		
	}
	

	/**
	 * Remove 'redundant' cycles (cycles with same nodes) and
	 * short cycles in big contexts (those with more than 5 already known targets for root).
	 */	
	public static ArrayList<ArrayList<String>> cleanCycles (ArrayList<ArrayList<String>> cycles, String root, ArrayList<String> apertium){
		ArrayList<ArrayList<String>> cyclesCleaned = new ArrayList<>();
		ArrayList<ArrayList<String>> cyclesSorted = new ArrayList<>();		
		for(ArrayList<String> c : cycles) {
			ArrayList<String> raw = new ArrayList<>(c);
			raw.add(root);					
//			if( apertium.size()<6 || (apertium.size()>5 && raw.size()>5)) {	//If root has more than 5 translations, remove short cyles		
				ArrayList<String> check = new ArrayList<>(raw);
				Collections.sort(check);				
				if(!cyclesSorted.contains(check))
					cyclesCleaned.add(raw);				
				cyclesSorted.add(check);				
//			}
		}
		return cyclesCleaned;		
	}
	
	
	/**
	 * Identifies 'potential' Targets in cycles (those not linked to root).
	 */	
	public static ArrayList<String> findTargets (ArrayList<ArrayList<String>> cycles, String root, ArrayList<TranslatablePair> pairs, String lang1){		
		ArrayList<String> targets = new ArrayList<>();		
		for (ArrayList<String> cycle: cycles) {				
			ArrayList<String> cycle_reduced = new ArrayList<>();			
			for(String word : cycle) {				
				if(!word.equals(root))
					cycle_reduced.add(word);
			}				
			for(String w : cycle_reduced) {			
				boolean exists = false;				
				for(TranslatablePair p : pairs) {						
					if(p.getSourceLabel().equals(root) && p.getTargetLabel().equals(w)) 
						exists = true;						
				}				
				if(!exists && !targets.contains(w) && !w.substring(w.lastIndexOf("-") + 1).equals(lang1)) 
					targets.add(w);					
			}			
		}		
		return targets;		
	}	
	
	/**
	 * Take cycles containing some Target word and calculate cycle density.
	 * For each cycle with a potential target we get: target, density and cycle
	 */	
	public static ArrayList<CycleDensity> calculateDensity(ArrayList<ArrayList<String>> cleanedCycles, ArrayList<String> targets, ArrayList<TranslatablePair> pairs) {		
		ArrayList<CycleDensity> cyclesDensity = new ArrayList<>();		
		for(ArrayList<String> cycle: cleanedCycles) {			
			ArrayList<String> targetsInCycle = new ArrayList<>();			
			for(String target : targets) {				
				if(cycle.contains(target)) //Check if cycle contains any target
					targetsInCycle.add(target);				
			}			
			if(targetsInCycle.size()>0) {				
				double score = getDensity(cycle, pairs);				
				for(String t : targetsInCycle) {
					cyclesDensity.add(new CycleDensity(t,score,cycle));
				}				
			}			
		}		
		return cyclesDensity;		
	}
	
	/**
	 * Calculate the density of a cycle, where Density = V / N*(N-1).
	 *  We need to compute the number of vertices between the nodes in the cycle.
	 */	
	public static double getDensity(ArrayList<String> cycle, ArrayList<TranslatablePair> pairs) {		
		int vertice = 0;		
		for(String node : cycle) {			
			for(String nextNode : cycle) {				
				for (TranslatablePair p : pairs) {	//Check if all node pairs in the cycle are linked	
					if(p.getSourceLabel().equals(node) && p.getTargetLabel().equals(nextNode)) 
						vertice += 1;					
				}				
			}
		}		
		int l = cycle.size();
		return vertice / (float)(l * (l-1));		
	}	
	
	/**
	 * Get languages involved in already known translations
	 */	
	public static ArrayList<String> getLanguages (ArrayList<String> apertium){		
		ArrayList<String> languages = new ArrayList<>();		
		for (String a : apertium)		
			languages.add(a.substring(a.lastIndexOf("-")+ 1));				
		return languages;		
	}	
	
	/**
	 * For each potential target compute the final score
	 * Input --> some CycleDensity: (target,score,[cycle]) , ...
	 * Output --> one CycleDensity per potential target: (target,highest score,[cycle])
	 */
	public static ArrayList<CycleDensity> compute_results(ArrayList<CycleDensity> cyclesDensity) {		
		ArrayList<CycleDensity> cycles_dict = new ArrayList<>();		
		Collections.sort(cyclesDensity, new Comparator<CycleDensity>(){ //Sort cyclesDensity so that for each target we have the higher score first
            @Override
            public int compare(CycleDensity o1, CycleDensity o2) {
            	int compareByTarget = o1.getTarget().compareTo(o2.getTarget());              
              	    if (compareByTarget != 0)
              	        return compareByTarget;              	    
                return new Integer(Double.compare(o2.getScore(), o1.getScore()));
            }
        });			
		for(CycleDensity inputCycle : cyclesDensity) {			
			boolean inDict = false;					
			for(CycleDensity cd : cycles_dict) {				
				if(cd.getTarget().equals(inputCycle.getTarget()))
					inDict = true;
			}			
			if(!inDict) {
				cycles_dict.add(inputCycle);
			}			
		}		
		return cycles_dict;		
	}	
	
	/**
	 *  It takes two lists of languages as input (input and output languages for which discover cycles-based translations).
	 *  
	 *  For each input language, it reads its corresponding lexicon file which contains one root word per line, as they appear 
	 *  in the translation graph (Apertium RDF). The lexicon file needs to be pre-calculated and placed in the folder ./data/lexicons.
	 *  For each root word, its context is obtained (that is, the chain of translations in the translation graph that 
	 *  starts from such a root term). Then, it discovers the cycles to which the root term belongs to in the graph 
	 *  and calculate the cycle density. The output results
	 *  (candidate translations, along with their score and cycle path), are stored in an output file, in a folder ./data/translations/ .
	 *  
	 *  
	 *  The pseudocode is:
	 *    inputFile <- lexicon file for lang_source
	 *    while inputFile not empty
	 *    	root <- next line
	 *      context <- getContext (root, langSource, langTarget)
	 *      cycles <- getCycles (root, context, langSource_langTarget)
	 *      write cycles in outputFile    
	 *    end of while
	 */				
	public static void main(String [] args) {
		
		//String [] langS = {args[0]};
		//String [] langT = {args[1]};
	
		String [] langS = {"en"};
		String [] langT = {"fr"};
		
		String root;
		try {
			for (int i = 0; i < langS.length; i++) {
				String inputFile = "data/lexicons/lexicon-" + langS[i] + ".txt";
//				String outputFile = "data/translations/cycles_ " + langS[i] + "-" + langT[i] + "_APv1.tsv";
				String outputFile = "data/translations/cycles_ " + langS[i] + "-" + langT[i] + "_APv2.tsv";
				BufferedReader br = new BufferedReader(new FileReader(inputFile));
				PrintWriter writer = new PrintWriter(new File(outputFile), "UTF-8");				
				while ((root = br.readLine()) != null) {
					getCycles(root, GetDataV2.getContext(root, langS[i], langT[i]), langS[i], langT[i], writer);
//					getCycles(root, GetData.getContext(root, langS[i], langT[i]), langS[i], langT[i], writer);
				}
				br.close();
				writer.close();
			}
		}catch(IOException e){
			e.printStackTrace();
		}	
	}




}
