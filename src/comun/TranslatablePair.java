package comun;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class TranslatablePair{
	
	final static String SEPARATOR = "', '";
	String sourceLabel;
	String targetLabel;
	String pos;
	String posApertium;
	
	public TranslatablePair(String sourceLabel, String targetLabel, String pos) {		
		this.sourceLabel = sourceLabel;
		this.targetLabel = targetLabel;
		this.pos = pos;
		this.posApertium = "";
	}
	
	public String getSourceLabel(){ return this.sourceLabel;}
	public String getTargetLabel(){ return this.targetLabel;}
	public String getPos(){ return this.pos;}
	public String getPosApertium(){ return this.posApertium;}
	
	public void setSourceLabel(String sourceLabel){ this.sourceLabel = sourceLabel;}
	public void setTargetLabel(String targetLabel){ this.targetLabel = targetLabel;}
	public void setPos(String pos){ this.pos = pos;}
	public void setPosApertium(String posApertium){ this.posApertium = posApertium;}

	public void printSourceTargetPosToFile(PrintWriter writer) {
		setSourceLabel(getSourceLabel().substring(0, getSourceLabel().indexOf("@")));
		setTargetLabel(getTargetLabel().substring(0, getTargetLabel().indexOf("@")));
		setPos(getPos().substring(getPos().indexOf("#") + 1));		
		try {			
			String source_UTF8 = URLDecoder.decode(sourceLabel, "UTF-8");
			String target_UTF8 = URLDecoder.decode(targetLabel, "UTF-8");
			String pos_UTF8 = URLDecoder.decode(pos, "UTF-8");			
			writer.println(source_UTF8 + "\t" + target_UTF8 + "\t" + pos_UTF8);
			writer.flush();			
		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
	}

	
	public String toString(){		
		return "['" + getSourceLabel() + SEPARATOR + getTargetLabel() + "']";		
	}
	
	public void print(){		
		System.out.print("[" + getSourceLabel() + SEPARATOR + getTargetLabel() + "], "); 		
	}
	
	public void printGoldStdToFile(PrintWriter writer) {		
		String source = getSourceLabel().substring(0, getSourceLabel().indexOf("@"));
		String target = getTargetLabel().substring(0, getTargetLabel().indexOf("@"));	
		String pos = getPos().substring(getPos().indexOf("#") + 1);	
//		try {			
//			String source_UTF8 = URLDecoder.decode(source, "UTF-8");
//			String target_UTF8 = URLDecoder.decode(target, "UTF-8");			
			writer.println(source + "\t" + target + "\t" + pos);
			writer.flush();
//		}catch(UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}		
	}
}
