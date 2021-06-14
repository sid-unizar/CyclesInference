package apertiumV2;


import java.io.File;
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import comun.Context;
import comun.TranslatablePair;

public class GetDataV2 {	
		
	/**
	 * 
	 * Given a root word, it calculates its 'context' (list of translation pairs when looking for trans(trans(trans(root)))
	 * that is, until a depth of four chained translations.
	 * The method returns a Context Object, whose fields are: root word, translation pairs and uniq words
	 *  
	 */
	public static Context getContext(String root, String lang1, String lang2) {		
		// get root translations: trans(root)
		ArrayList<TranslatablePair> pairs = SPARQLsearchesV2.obtainTranslationsFromRootWord(root, lang1);
		pairs = removeTranslationPairs(pairs, lang1, lang2);
		String posApertium;
		if(!pairs.isEmpty()) {
			String posApertium_aux = pairs.get(0).getSourceLabel();
			int cont = 0;
			while(posApertium_aux.indexOf("-") > -1) {
				posApertium_aux = posApertium_aux.substring(posApertium_aux.indexOf("-")+1,posApertium_aux.length());
				cont+=1;
			}
			if(cont==2)
				posApertium = pairs.get(0).getSourceLabel().substring(pairs.get(0).getSourceLabel().indexOf("-")+1, pairs.get(0).getSourceLabel().lastIndexOf("-"));
			else
				posApertium = null;
			String pos = pairs.get(0).getPos();
			ArrayList<String> trans = new ArrayList<>();
			ArrayList<String> allTargets = new ArrayList<>();
			for (TranslatablePair p : pairs) 
				trans.add(p.getTargetLabel());
			ArrayList<String> trans2 = new ArrayList<>();
			ArrayList<String> trans3 = new ArrayList<>();
			// for each root translation
			for (String t : trans) {
				// get trans(trans(root)
				trans2 = SPARQLsearchesV2.obtainTranslationsFromURI(t);
				for (String t2 : trans2) {
					// appends to pairs the translations founded
					pairs.add(new TranslatablePair(t, t2, pos));
					pairs = removeTranslationPairs(pairs, lang1, lang2);
					// to avoid target duplicates and asking again for root
					if (!t2.equals(pairs.get(0).getSourceLabel()) && !allTargets.contains(t2)) {
						allTargets.add(t2);
						// get trans(trans(trans(root)))
						trans3 = SPARQLsearchesV2.obtainTranslationsFromURI(t2);
						for (String t3 : trans3) {
							// appends to pairs the translations founded
							pairs.add(new TranslatablePair(t2, t3, pos));
						}
						pairs = removeTranslationPairs(pairs, lang1, lang2);
					}
				}
			}
			// convert long URIs into 'words'
			for (TranslatablePair p : pairs) {
				p.setSourceLabel(p.getSourceLabel().substring(p.getSourceLabel().lastIndexOf('/') + 1)); //, p.getSourceLabel().indexOf("-")));
				p.setTargetLabel(p.getTargetLabel().substring(p.getTargetLabel().lastIndexOf('/') + 1));
				p.setPos(p.getPos().substring(p.getPos().lastIndexOf("#") + 1));
				p.setPosApertium(posApertium);
			}
		}
		ArrayList<TranslatablePair> pairs_uniq = removeDuplicatedTranslations(pairs);
		pairs_uniq = removeTranslationPairs(pairs_uniq, lang1, lang2);
		ArrayList<String> uniq = getUniqWords(pairs_uniq);
		System.out.print("'" + root + "': ");
		ArrayList<String> pairs_uniq_toString = new ArrayList<>();
		for (TranslatablePair p : pairs_uniq) 
			pairs_uniq_toString.add(p.toString());
		System.out.print("[" + Arrays.toString(pairs_uniq_toString.toArray()));
		System.out.println(", " + Arrays.toString(uniq.toArray()) + "]");		
		return new Context(root, pairs_uniq, uniq);		
	}	
	
	/**
	 *  Get a list of the (non-duplicated) words in pairs
	 */	
	public static ArrayList<String> getUniqWords(ArrayList<TranslatablePair> pairs){		
		ArrayList<String> uniq = new ArrayList<>();		
		for(TranslatablePair p : pairs) {			
			boolean isSourceInUniq = false;
			boolean isTargetInUniq = false;			
			for(String u : uniq) {			
				if(p.getSourceLabel().equals(u))
					isSourceInUniq = true;							
				if(p.getTargetLabel().equals(u))
					isTargetInUniq= true;				
			}			
			if(!isSourceInUniq)
				uniq.add(p.getSourceLabel());			
			if(!isTargetInUniq)
				uniq.add(p.getTargetLabel());			
		}		
		return uniq;
	}	
	
	public static ArrayList<TranslatablePair> removeDuplicatedTranslations(ArrayList<TranslatablePair> pairs){		
		ArrayList<TranslatablePair> removedDuplicated = new ArrayList<>();		
		boolean isDuplicated;		
		for(TranslatablePair p : pairs) {
			isDuplicated = false;			
			for(TranslatablePair p1: removedDuplicated) {				
				if(p.getSourceLabel().equals(p1.getSourceLabel()) && p.getTargetLabel().equals(p1.getTargetLabel())) 
					isDuplicated = true;				
			}			
			if(!isDuplicated) 
				removedDuplicated.add(p);				
		}		
		return removedDuplicated;
	}	
	
	/**
	 * Given two languages, it removes the translation pairs that contains them
	 */	
	public static ArrayList<TranslatablePair> removeTranslationPairs(ArrayList<TranslatablePair> pairs, String lang1, String lang2){		
		ArrayList<TranslatablePair> removedPairs = new ArrayList<>();		
		boolean isPair;		
		for(TranslatablePair p : pairs) {		
			isPair = false;			
			if((p.getSourceLabel().substring(p.getSourceLabel().lastIndexOf("-") + 1).equals(lang1) &&
					p.getTargetLabel().substring(p.getTargetLabel().lastIndexOf("-") + 1).equals(lang2)) ||
					(p.getSourceLabel().substring(p.getSourceLabel().lastIndexOf("-") + 1).equals(lang2) &&
							p.getTargetLabel().substring(p.getTargetLabel().lastIndexOf("-") + 1).equals(lang1)))
				isPair = true;			
			if(!isPair) 
				removedPairs.add(p);				
		}		
		return removedPairs;		
	}

	/**
	 * Creates a file that contains a list of all the words (in Apertium) from a given language
	 * @param lang
	 */
	public static void createLexiconFile(String lang){
		String outputFile = "data/lexicons/lexicon-" + lang + ".txt";
		try {
			PrintWriter writer = new PrintWriter(new File(outputFile), "UTF-8");
			ArrayList<String> lexicon = SPARQLsearchesV2.obtainLexiconFromLanguage(lang);			
			for (String word : lexicon){
				word = word.substring(0,word.lastIndexOf("@"));			
				writer.println(word);				
				writer.flush();				
			}
			writer.close();
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(UnsupportedEncodingException ee) {
			ee.printStackTrace();
		}
	}	
	
	public static void main (String [] args) {
//		getContext("abbess");
		createLexiconFile("as");
	}
	
}
