package apertiumV1;

import java.util.ArrayList;
import org.apache.jena.query.Query; 
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import comun.TranslatablePair;

public class SPARQLsearches {
	
	private static String sparqlEndpoint = "http://linguistic.linkeddata.es/sparql/";
	
	public SPARQLsearches (String endpoint){
		setSparqlEndpoint(endpoint);
	}

	public static String getSparqlEndpoint() {return sparqlEndpoint;}	
	public static void setSparqlEndpoint(String endpoint) {SPARQLsearches.sparqlEndpoint = endpoint;}	
	
	public static ArrayList<TranslatablePair> obtainTranslationsFromRootWord(String word, String lang) {		
		ArrayList<TranslatablePair> pairs = new ArrayList<>();
		String queryString =
				"PREFIX lemon: <http://www.lemon-model.net/lemon#>" +
				"PREFIX tr: <http://purl.org/net/translation#>" +
				"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" +
				"SELECT DISTINCT ?source ?target ?pos " +
				"WHERE {" +
				"        ?form_source lemon:writtenRep \"" + word + "\"@" + lang + "." +
				"        ?source lemon:lexicalForm ?form_source . " +
				"?source lexinfo:partOfSpeech ?pos ." +
				"?sense_source lemon:isSenseOf  ?source ." +
				"?trans  tr:translationSense  ?sense_source ." +
				"?trans  tr:translationSense  ?sense_target ." +
				"?sense_target lemon:isSenseOf  ?target . " +
				"?lexicon lemon:entry ?target ." +
				"FILTER (?source != ?target)" +
				"}";
		Query query = QueryFactory.create(queryString);		 
		QueryExecution qe = QueryExecutionFactory.sparqlService(getSparqlEndpoint(), query);
		ResultSet results = qe.execSelect();
		//Review results
		for ( ; results.hasNext() ; )    {  					
			QuerySolution soln = results.nextSolution() ;
			pairs.add(new TranslatablePair(soln.get("source").toString(),soln.get("target").toString(), soln.get("pos").toString()));								
		}  			
		// Important - free up resources used running the query
		qe.close();			
		return pairs;		 
	}
	
	public static ArrayList<String> obtainTranslationsFromURI(String sourceURI){
		ArrayList<String> targetURIs = new ArrayList<>();	
		String queryString = 
					"PREFIX lemon: <http://www.lemon-model.net/lemon#>" +
					"PREFIX tr: <http://purl.org/net/translation#>" +
					"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" +
					"SELECT DISTINCT ?source ?target" +
					"\nWHERE { " +
						"?sense_source lemon:isSenseOf <" + sourceURI + ">." +
						"?trans  tr:translationSense  ?sense_source ." +
						"?trans  tr:translationSense  ?sense_target ." +
						"?sense_source lemon:isSenseOf  ?source . " +
						"?sense_target lemon:isSenseOf  ?target . " +
						"FILTER (?source != ?target)" +
					"}";		
		Query query = QueryFactory.create(queryString);		 
		QueryExecution qe = QueryExecutionFactory.sparqlService(getSparqlEndpoint(), query);
		ResultSet results = qe.execSelect();
		//Review results
		for ( ; results.hasNext() ; )    {
			QuerySolution soln = results.nextSolution() ;
			targetURIs.add(soln.get("target").toString());
		}		
		// Important - free up resources used running the query
		qe.close();			
		return targetURIs;		
	}
	
	
	public static ArrayList<TranslatablePair> obtainTranslationSet(String lang1, String lang2){		
		ArrayList<TranslatablePair> pairs = new ArrayList<>();
		String queryString =
				"PREFIX lemon: <http://www.lemon-model.net/lemon#>" +
				"PREFIX tr: <http://purl.org/net/translation#>" +
				"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" +
				"SELECT DISTINCT ?writtenRep_source ?writtenRep_target ?pos_source" +
				"\nWHERE {" +
				"?transSet a tr:TranslationSet ;" +
				"	     lemon:language \"" + lang1 + "\" ;" +
				"	     lemon:language \"" + lang2 + "\" ;" +
				"             tr:trans ?trans ." +
				"        ?trans tr:translationSource ?source_sense ;" +
				"             tr:translationTarget ?target_sense ." +
				"        ?source_sense lemon:isSenseOf ?lex_entry_source ." +
				"        ?target_sense lemon:isSenseOf ?lex_entry_target ." +
				"        ?lex_entry_source lexinfo:partOfSpeech ?pos_source ;" +
				"				lemon:lexicalForm ?form_source ." +
				"		 ?form_source lemon:writtenRep ?writtenRep_source ." +
				"        ?lex_entry_target lexinfo:partOfSpeech ?pos_target ;" +
				"				lemon:lexicalForm ?form_target ." +
				"		 ?form_target lemon:writtenRep ?writtenRep_target ." +
				"        FILTER(?pos_source = ?pos_target)" +
				"        FILTER(?lex_entry_source != ?lex_entry_target)" +
				"} " +
				"ORDER BY ?writtenRep_source";
//		String queryString =
//				"PREFIX lemon: <http://www.lemon-model.net/lemon#>" +
//				"PREFIX tr: <http://purl.org/net/translation#>" +
//				"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" +
//				"SELECT DISTINCT ?lex_entry_source ?lex_entry_target ?pos_source" +
//				"\nWHERE {" +
//				"	?transSet a tr:TranslationSet ;" +
//				"	     lemon:language \"" + lang1 + "\" ;" +
//				"	     lemon:language \"" + lang2 + "\" ;" +
//				"             tr:trans ?trans ." +
//				"        ?trans tr:translationSource ?source_sense ;" +
//				"             tr:translationTarget ?target_sense ." +
//				"        ?source_sense lemon:isSenseOf ?lex_entry_source ." +
//				"        ?target_sense lemon:isSenseOf ?lex_entry_target ." +
//				"        ?lex_entry_source lexinfo:partOfSpeech ?pos_source ." +
//				"        ?lex_entry_target lexinfo:partOfSpeech ?pos_target ." +
//				"        FILTER(?pos_source = ?pos_target)" +
//				"        FILTER(?lex_entry_source != ?lex_entry_target)" +
//				"} " +
//				"ORDER BY ?lex_entry_source";		
		Query query = QueryFactory.create(queryString);		 
		QueryExecution qe = QueryExecutionFactory.sparqlService(getSparqlEndpoint(), query);
		ResultSet results = qe.execSelect();
		//Review results
		for ( ; results.hasNext() ; )    {  					
			QuerySolution soln = results.nextSolution() ;
			pairs.add(new TranslatablePair(soln.get("writtenRep_source").toString(),soln.get("writtenRep_target").toString(),soln.get("pos_source").toString()));
//			pairs.add(new TranslatablePair(soln.get("lex_entry_source").toString(),soln.get("lex_entry_target").toString(),soln.get("pos_source").toString()));
		}  			
		// Important - free up resources used running the query
		qe.close();		
		return pairs;		
	}

	public static ArrayList<String> obtainLexiconFromLanguage(String lang){
		ArrayList<String> lexicon = new ArrayList<>();
		String queryString =
				"PREFIX lemon: <http://www.lemon-model.net/lemon#>" +
						"PREFIX tr: <http://purl.org/net/translation#>" +
						"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" +
						"SELECT DISTINCT ?written_rep\n" +
						"WHERE {" +
						"        ?lexicon lemon:entry ?lex_entry ;" +
						"              lemon:language \"" + lang + "\" ." +
						"        ?lex_entry lemon:lexicalForm ?lex_form ." +
						"        ?lex_form lemon:writtenRep ?written_rep ." +
						"} " +
						"ORDER BY ?written_rep";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.sparqlService(getSparqlEndpoint(), query);
		ResultSet results = qe.execSelect();
		//Review results
		for ( ; results.hasNext() ; )    {
			QuerySolution soln = results.nextSolution() ;
			lexicon.add(soln.get("written_rep").toString());
		}
		// Important - free up resources used running the query
		qe.close();
		return lexicon;
	}
	
	
}
