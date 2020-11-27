package comun;

import java.util.ArrayList;


public class Context {
	
	String root;
	ArrayList<TranslatablePair> trans_pairs;
	ArrayList<String> uniq_words;
	
	public 	Context(String root, ArrayList<TranslatablePair> trans_pairs, ArrayList<String> uniq_words) {		
		this.root = root;
		this.trans_pairs = trans_pairs;
		this.uniq_words = uniq_words;	
	}
	
	public String getRoot() {return this.root;	}	
	public ArrayList<TranslatablePair> getPairs() {return this.trans_pairs;}	
	public ArrayList<String> getUniq_words() {return this.uniq_words;}
	
	public void setRoot(String root) {this.root = root;}	
	public void setPairs(ArrayList<TranslatablePair> trans_pairs) {this.trans_pairs = trans_pairs;}	
	public void setUniq_words(ArrayList<String> uniq_words) {this.uniq_words = uniq_words;}
	
}