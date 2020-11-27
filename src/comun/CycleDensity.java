package comun;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class CycleDensity {
	
	String target;
	double score;
	ArrayList<String> cycle;
	
	public 	CycleDensity(String target, double score, ArrayList<String> cycle) {		
		this.target = target;
		this.score = score;
		this.cycle = cycle;	
	}
	
	public String getTarget() {return this.target;}	
	public double getScore() {return this.score;}	
	public ArrayList<String> getCycle() {return this.cycle;}
	
	public void setTarget(String target) {this.target = target;}	
	public void setScore(double score) {this.score = score;}	
	public void setCycle(ArrayList<String> cycle) {this.cycle = cycle;}
	
	public void print() {
		System.out.println(this.target + " --> score: " + this.score + ", cycle" + this.cycle);
	}
	
	public void printToFile(PrintWriter writer, String root, String pos , String posApertium) {	
		
		String targetShorted = getTarget().substring(0,getTarget().indexOf("-"));		
		try {	
			String target_UTF8 = URLDecoder.decode(targetShorted, "UTF-8");			
			writer.println(root + "\t" + target_UTF8 + "\t" + pos + "\t" + posApertium + "\t"+ (double)Math.round(getScore() * 100d) / 100d + "\t" + this.cycle);
			writer.flush();			
		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
	}	
	
	public void printToFile(PrintWriter writer, String root, String pos) {	
		
		String targetShorted = getTarget().substring(0,getTarget().indexOf("-"));		
		try {	
			String target_UTF8 = URLDecoder.decode(targetShorted, "UTF-8");			
			writer.println(root + "\t" + target_UTF8 + "\t" + pos + "\t"+ (double)Math.round(getScore() * 100d) / 100d + "\t" + this.cycle);
			writer.flush();			
		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
	}	
}