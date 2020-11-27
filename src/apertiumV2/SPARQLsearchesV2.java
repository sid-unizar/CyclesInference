package apertiumV2;

import java.util.ArrayList;
import org.apache.jena.query.Query; 
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import comun.TranslatablePair;

public class SPARQLsearchesV2 {

	//private static String sparqlEndpoint = "http://localhost:8080/fuseki/ds/query";
	private static String sparqlEndpoint = "http://dbserver.acoli.cs.uni-frankfurt.de:5005/ds/query";
	
	public SPARQLsearchesV2 (String endpoint){
		setSparqlEndpoint(endpoint);
	}

	public static String getSparqlEndpoint() {return sparqlEndpoint;}	
	public static void setSparqlEndpoint(String endpoint) {SPARQLsearchesV2.sparqlEndpoint = endpoint;}	
	
	
	public static ArrayList<TranslatablePair> obtainTranslationsFromRootWord(String word, String lang) {		
		ArrayList<TranslatablePair> pairs = new ArrayList<>();	
		String queryString =
				"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" + 
				"PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>" + 
				"PREFIX vartrans: <http://www.w3.org/ns/lemon/vartrans#>" + 
				"PREFIX lime: <http://www.w3.org/ns/lemon/lime#>" + 
				"SELECT DISTINCT ?source ?target ?pos" + 
				"\nWHERE {" + 
				//"GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" + 
				//"    {" + 
				"   ?form_source ontolex:writtenRep \"" + word + "\"@" + lang + "." +
				"   ?source ontolex:lexicalForm ?form_source ." + 
				"	?source lexinfo:partOfSpeech ?pos ." + 
				"	?sense_source ontolex:isSenseOf  ?source ." + 
				"	{?trans vartrans:source ?sense_source;" + 
				"           vartrans:target ?sense_target}UNION" + 
				"	{?trans vartrans:target ?sense_source;" + 
				"           vartrans:source ?sense_target}" + 
				"	?sense_target ontolex:isSenseOf  ?target ." + 
				"	?lexicon lime:entry ?target ." + 
				"	FILTER (?source != ?target)" + 
				//"  }" + 
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
					"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" + 
					"PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>" + 
					"PREFIX vartrans: <http://www.w3.org/ns/lemon/vartrans#>" + 
					"PREFIX lime: <http://www.w3.org/ns/lemon/lime#>" + 
					"SELECT DISTINCT ?source ?target" + 
					"\nWHERE { " + 
					//"GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" + 
					//"    {" + 
					"?sense_source ontolex:isSenseOf <" + sourceURI + ">." +
					"{?trans vartrans:source ?sense_source;" + 
					"        vartrans:target ?sense_target}UNION" + 
					"{?trans vartrans:target ?sense_source;" + 
					"        vartrans:source ?sense_target}" + 
					"?sense_source ontolex:isSenseOf  ?source . " + 
					"?sense_target ontolex:isSenseOf  ?target . " + 
					"FILTER (?source != ?target)" + 
					//"  }" + 
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
				"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" + 
				"PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>" + 
				"PREFIX vartrans: <http://www.w3.org/ns/lemon/vartrans#>" + 
				"PREFIX lime: <http://www.w3.org/ns/lemon/lime#>" + 
				"SELECT DISTINCT ?writtenRep_source ?writtenRep_target ?pos_source" + 
				"\nWHERE {" + 
				//"GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" + 
				//"    {" + 
				"	?transSet a vartrans:TranslationSet ;" + 
				"	     lime:language \"" + lang1 + "\" ;" +
				"	     lime:language \"" + lang2 + "\" ;" +
				"            	vartrans:trans ?trans ." + 
				"	{?trans vartrans:source ?source_sense;" + 
				"            vartrans:target ?target_sense}UNION" + 
				"    {?trans vartrans:target ?source_sense;" + 
				"            vartrans:source ?target_sense}" + 
				"    ?source_sense ontolex:isSenseOf ?lex_entry_source ." + 
				"    ?target_sense ontolex:isSenseOf ?lex_entry_target ." + 
				"    ?lex_entry_source lexinfo:partOfSpeech ?pos_source ;" + 
				"					  ontolex:lexicalForm ?form_source ." + 
				"	?form_source ontolex:writtenRep ?writtenRep_source ." + 
				"    ?lex_entry_target lexinfo:partOfSpeech ?pos_target ;" + 
				"					  ontolex:lexicalForm ?form_target ." + 
				"	?form_target ontolex:writtenRep ?writtenRep_target ." + 
				"    FILTER(?pos_source = ?pos_target)" + 
				"    FILTER(?lex_entry_source != ?lex_entry_target)" + 
				//"}" +
				"}" + 
				"ORDER BY ?writtenRep_source";	
		
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
				"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" + 
				"PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>" + 
				"PREFIX lime: <http://www.w3.org/ns/lemon/lime#>" + 
				"SELECT DISTINCT ?written_rep" + 
				"\nWHERE {" + 
				//"GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" + 
				//"    {" + 
				"        ?lexicon lime:entry ?lex_entry ;" + 
				"              	  lime:language \"" + lang + "\" ." + 
				"        ?lex_entry ontolex:lexicalForm ?lex_form ." + 
				"        ?lex_form ontolex:writtenRep ?written_rep .}" + 
				//"}" + 
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
