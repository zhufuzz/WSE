package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import java.util.Vector;

import edu.nyu.cs.cs2580.ScoredDocument;

public class TsvGen {
    public static void generate(Vector<ScoredDocument> results, String fileName){
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("results/"+fileName+".tsv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        for (ScoredDocument result : results) {
            sb.append(result.asTextResult()).append("\n");
        }

        pw.write(sb.toString());
        pw.close();
    }
}