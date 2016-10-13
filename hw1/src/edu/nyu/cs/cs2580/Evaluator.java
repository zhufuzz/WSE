package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Evaluator for HW1.
 * 
 * @author fdiaz
 * @author congyu
 */
class Evaluator {
  public static class DocumentRelevances {
    private Map<Integer, Double> relevances = new HashMap<Integer, Double>();
    
    public DocumentRelevances() { }
    
    public void addDocument(int docid, String grade, boolean binary) {
      if(binary)
        relevances.put(docid, convertToBinaryRelevance(grade));
      else {
        if(grade.equals("Perfect")){
          relevances.put(docid,10.0);
        }
        else if(grade.equals("Excellent")){
          relevances.put(docid,7.0);
        }
        else if(grade.equals("Good")){
          relevances.put(docid,5.0);
        }
        else if(grade.equals("Fair")){
          relevances.put(docid,1.0);
        }
        else if(grade.equals("Bad")){
          relevances.put(docid,0.0);
        }
      }
    }
    
    public boolean hasRelevanceForDoc(int docid) {
      return relevances.containsKey(docid);
    }
    
    public double getRelevanceForDoc(int docid) {
      return relevances.get(docid);
    }
    
    private static double convertToBinaryRelevance(String grade) {
      if (grade.equalsIgnoreCase("Perfect") ||
          grade.equalsIgnoreCase("Excellent") ||
          grade.equalsIgnoreCase("Good")) {
        return 1.0;
      }
      return 0.0;
    }
  }
  
  /**
   * Usage: java -cp src edu.nyu.cs.cs2580.Evaluator [labels] [metric_id]
   */
  public static void main(String[] args) throws IOException {
    Map<String, DocumentRelevances> judgments = new HashMap<String, DocumentRelevances>();
    SearchEngine.Check(args.length == 2, "Must provide labels and metric_id!");
    if(args[1].equals("5"))
      readRelevanceJudgments(args[0], judgments, false);

    else readRelevanceJudgments(args[0], judgments, true);
    evaluateStdin(Integer.parseInt(args[1]), judgments);
  }

  public static void readRelevanceJudgments(String judgeFile, Map<String, DocumentRelevances> judgements, boolean binary) throws IOException {
    String line = null;
    BufferedReader reader = new BufferedReader(new FileReader(judgeFile));
    while ((line = reader.readLine()) != null) {
      // Line format: query \t docid \t grade
      Scanner s = new Scanner(line).useDelimiter("\t");
      String query = s.next();
      DocumentRelevances relevances = judgements.get(query);
      if (relevances == null) {
        relevances = new DocumentRelevances();
        judgements.put(query, relevances);
      }
      relevances.addDocument(Integer.parseInt(s.next()), s.next(), binary);
      s.close();
    }
    reader.close();
  }

  // @CS2580: implement various metrics inside this function
  public static void evaluateStdin(int metric, Map<String, DocumentRelevances> judgments) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    List<Integer> results = new ArrayList<Integer>();
    String line = null;
    String currentQuery = "";
    while ((line = reader.readLine()) != null) {
      Scanner s = new Scanner(line).useDelimiter("\t");
      final String query = s.next();
      if (!query.equals(currentQuery)) {
        if (results.size() > 0) {
          switch (metric) {
          case -1:
            evaluateQueryInstructor(currentQuery, results, judgments);
            break;
          case 0:
          case 1:
          case 2:
            evaluateQueryMetric2(currentQuery, results, judgments);
          case 3:
          case 4:
            evaluateQueryMetric4(currentQuery, results, judgments);
          case 5:
            evaluateQueryMetric5(currentQuery, results, judgments);
            break;
          case 6:
            evaluateQueryMetric6(currentQuery, results, judgments);
            break;
          default:
            // @CS2580: add your own metric evaluations above, using function
            // names like evaluateQueryMetric0.
            System.err.println("Requested metric not implemented!");
          }
          results.clear();
        }
        currentQuery = query;
      }
      results.add(Integer.parseInt(s.next()));
      s.close();
    }
    reader.close();
    if (results.size() > 0) {
      evaluateQueryInstructor(currentQuery, results, judgments);
    }
  }
  
  public static void evaluateQueryInstructor(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments) {
    double R = 0.0;
    double N = 0.0;
    for (int docid : docids) {
      DocumentRelevances relevances = judgments.get(query);
      if (relevances == null) {
        System.out.println("Query [" + query + "] not found!");
      } else {
        if (relevances.hasRelevanceForDoc(docid)) {
          R += relevances.getRelevanceForDoc(docid);
        }
        ++N;
      }
    }
    System.out.println(query + "\t" + Double.toString(R / N));
  }

  // perform F0.5
  public static void evaluateQueryMetric2(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments) {

    String outputResult = query
     + "\t" + evaluateF(query, docids, judgments, 1)
     + "," + evaluateF(query, docids, judgments, 5)
     + "," + evaluateF(query, docids, judgments, 10);

    System.out.println(outputResult);
  }

  // average precision
  public static void evaluateQueryMetric4(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments) {
    double recall = -1.0;
    double precision = 0.0;
    double totalR = 0.0;
    int collectionSize = docids.size();

    for (int i = 1; i <= collectionSize; i++) {
      double currRecall = evaluateRecall(query, docids, judgments, i);
      if (currRecall > recall) {
        precision += evaluatePrecision(query, docids, judgments, i);
        recall = currRecall;
        totalR++;
      }
    }

    System.out.println(query + "\t" + (precision/totalR));
  }

  private static double evaluateF(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments, int retrievedSize) {
      double  recall, precision;

      recall = evaluateRecall(query, docids, judgments, retrievedSize);
      precision = evaluatePrecision(query, docids, judgments, retrievedSize);
      
      return 2 * recall * precision / (recall + precision);
  }

  private static double evaluateRecall(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments, int retrievedSize) {
    double R = 0.0;
    double totalR = 0.0;
    int collectionSize = docids.size();

    for (int i = 0; i < collectionSize; i++) {
      int docid = docids.get(i);
      DocumentRelevances relevances = judgments.get(query);
      if (relevances == null) {
        System.out.println("Query [" + query + "] not found!");
      } else {
        if (relevances.hasRelevanceForDoc(docid)) {
          if (i < retrievedSize) {
            R += relevances.getRelevanceForDoc(docid);
          }
          totalR += relevances.getRelevanceForDoc(docid);
        }
      }
    }

    return R/totalR;
  }

  private static double evaluatePrecision(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments, int retrievedSize) {
    double R = 0.0;
    int collectionSize = docids.size();

    for (int i = 0; i < retrievedSize && i < collectionSize; i++) {
      int docid = docids.get(i);
      DocumentRelevances relevances = judgments.get(query);
      if (relevances == null) {
        System.out.println("Query [" + query + "] not found!");
      } else {
        if (relevances.hasRelevanceForDoc(docid)) {
          R += relevances.getRelevanceForDoc(docid);
        }
      }
    }

    return R/retrievedSize;
  }
//  Metric5: NDCG at 1, 5, and 10 (using the gain values presented in Lecture 2)
  public static void evaluateQueryMetric5(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments){
    DocumentRelevances relevances = judgments.get(query);
    double[] p1 = new double[1];
    double[] p5 = new double[5];
    double[] p10 = new double[10];
    for(int i=0; i<10; i++){
      double rel = 0.0;
      if(relevances.hasRelevanceForDoc(docids.get(i))){
        rel = relevances.getRelevanceForDoc(docids.get(i));
      }
      if (i < 1) {
        p1[i]= rel;
        p5[i]= rel;
        p10[i]= rel;
      }
      if (i < 5){
        p5[i]= rel;
        p10[i]= rel;
      }
      p10[i]= rel;
    }
  }

  private double discountedCumulativeGain(int[] scores) {
    double result = 0.0;
    for (int i = 0; i < scores.length; i++) {
//      if (i == 0) {
//        result += scores[i];
//      } else {
        result += scores[i]/(Math.log(i+1)/Math.log(2));
//      }
    }
    return result;
  }

  
//  Metric6: Reciprocal rank
  public static void evaluateQueryMetric6(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments){
    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null) {
      System.out.println("Query [" + query + "] not found!");
    }
    else{
//      because docids are sorted based on relavence
      for(int i = 0; i<docids.size(); i++){
        Integer docid = docids.get(i);
        if (relevances.hasRelevanceForDoc(docid) && relevances.getRelevanceForDoc(docid)==1.0) {
          System.out.println(query + "\t" + Double.toString(1/i));
          break;
        }
      }
    }
    System.out.println("Reciprocal rank: very bad result!");
  }
}
