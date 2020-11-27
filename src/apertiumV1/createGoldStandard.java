package apertiumV1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import comun.TranslatablePair;

public class createGoldStandard {

    public static void createGoldStd(String lang1, String lang2, PrintWriter writer) {
        ArrayList<TranslatablePair> pairs = SPARQLsearches.obtainTranslationSet(lang1, lang2);
        for(TranslatablePair p : pairs) {
            p.printSourceTargetPosToFile(writer);
            p.print();
        }
    }
    
    public static ArrayList<String> getSourceLabelFromGoldStd(String inputFile) {
        ArrayList<String> sourceLabels = new ArrayList<>();
        ArrayList<String> sourceLabelsWithoutRepetition = new ArrayList<>();
        try {
            BufferedReader br  = new BufferedReader(new FileReader(inputFile));
            String line, root;
            while ((line = br.readLine()) != null) {
                root = line.substring(0, line.indexOf("\t"));
                sourceLabels.add(root);
                if(!sourceLabelsWithoutRepetition.contains(root))
                    sourceLabelsWithoutRepetition.add(root);
            }
            br.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return sourceLabelsWithoutRepetition;
    }

    public static void main(String [] args) {
        try {
 
            String lang1 = args[0];
            String lang2 = args[1];
            String outputFile = "data/goldApertium/GoldStdApertium_" + lang1 + "-" + lang2 + "_v1.txt";
            PrintWriter writer = new PrintWriter(outputFile);            
            createGoldStd(lang1, lang2, writer);
            writer.close();
            
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
