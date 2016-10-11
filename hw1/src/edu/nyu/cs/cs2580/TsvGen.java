package edu.nyu.cs.cs2580;

import java.io.*;
import java.util.*;


public class TsvGen {
    public static void generate(Vector<ScoredDocument> results, String fileName) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter("results/"+fileName+".tsv",true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        for (ScoredDocument result : results) {
            sb.append(result.asTextResult()).append("\n");
        }

        pw.append(sb.toString());
        pw.close();
    }
}