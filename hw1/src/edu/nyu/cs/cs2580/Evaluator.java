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
            evaluateQueryMetric0(currentQuery, results, judgments);
            break;
          case 1:
            evaluateQueryMetric1(currentQuery, results, judgments);
            break;
          case 2:
            evaluateQueryMetric2(currentQuery, results, judgments);
            break;
          case 3:
            evaluateQueryMetric3(currentQuery, results, judgments);
            break;
          case 4:
            evaluateQueryMetric4(currentQuery, results, judgments);
            break;
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

  //  Metric0: Precision 1, 5, 10
  public static void evaluateQueryMetric0(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments){
    String result = "";
    Integer[] recalls = {1, 5, 10};
    for (int numToJudge : recalls) {
      result += "," + evaluateQueryPrecision(query, docids, judgments, numToJudge);
    }
    System.out.println(query + "\t" + result);
  }

  private static double evaluateQueryPrecision(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments, int numToJudge){
    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null) {
      System.out.println("Query [" + query + "] not found!");
      return -1;
    }
    double hitCount = 0.0;
    for (int i = 0; i < numToJudge && i < docids.size(); i++) {
      if (relevances.hasRelevanceForDoc(docids.get(i)) &&
          relevances.getRelevanceForDoc(docids.get(i)) > 0) {
        hitCount += 1;
      }
    }
    return hitCount/numToJudge;
  }

  //  Metric1: Recall 1, 5, 10
  public static void evaluateQueryMetric1(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments){
    String result = "";
    Integer[] recalls = {1, 5, 10};
    for (int numToJudge : recalls) {
      result += "," + evaluateQueryRecall(query, docids, judgments, numToJudge);
    }
    System.out.println(query + "\t" + result);
  }

  private static double evaluateQueryRecall(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments, int numToJudge) {
    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null) {
      System.out.println("Query [" + query + "] not found!");
      return -1;
    }
    double relevanceCount = 0.0;
    double hitCount = 0.0;
    for (int i = 0; i < docids.size(); i++) {
      if (relevances.hasRelevanceForDoc(docids.get(i)) &&
          relevances.getRelevanceForDoc(docids.get(i)) > 0) {
        relevanceCount += 1;
        if (i < numToJudge) {
          hitCount += 1;
        }
      }
    }
    return hitCount/relevanceCount;
  }

  // perform F0.5
  public static void evaluateQueryMetric2(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments) {

    String outputResult = "";
    Integer[] selections = {1, 5, 10};
    for (int numToJudge : selections) {
      outputResult += "," + evaluateF(query, docids, judgments, numToJudge);
    }

    System.out.println("query" + "\t" + outputResult);
  }

  private static double evaluateF(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments, int numToJudge) {
      double  recall, precision;

      recall = evaluateQueryRecall(query, docids, judgments, numToJudge);
      precision = evaluateQueryPrecision(query, docids, judgments, numToJudge);
      
      return 2 * recall * precision / (recall + precision);
  }

  // Precision at Recall Points
  public static void evaluateQueryMetric3(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments) {
    DocumentRelevances relevances = judgments.get(query);
    double relevanceCount = 0.0;
    for (int i = 0; i < docids.size(); i++) {
      if (relevances.hasRelevanceForDoc(docids.get(i)) &&
          relevances.getRelevanceForDoc(docids.get(i)) > 0) {
        relevanceCount += 1.0;
      }
    }
    // use 11 slots to remember the max precision in [0,0.1), [0.1,0.2) ... [0.9,1.0), {1.0}
    double[] maxPrecisionAtRecallPoints = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    double recallPoint;
    double recall = 0.0;
    double precision;
    int slot = 0;
    for (int i = 0; i < docids.size(); i++) {
      if (relevances.hasRelevanceForDoc(docids.get(i)) &&
          relevances.getRelevanceForDoc(docids.get(i)) > 0) {
        System.out.println(i+1);
        recall += 1.0;
        recallPoint = recall/relevanceCount;
        precision = recall/(i+1);
        slot = (int) Math.floor(recallPoint*10);
        if (precision > maxPrecisionAtRecallPoints[slot]){
          maxPrecisionAtRecallPoints[slot] = precision;
        }
      }
      if (slot == 10) {
        break;
      }
    }
    for (int i = 0; i < 11; i++) {
      System.out.print(maxPrecisionAtRecallPoints[i] + ",");
    }
    // interpolation
    double currentMax = maxPrecisionAtRecallPoints[10];
    for (int i = 9; i >= 0; i--) {
      if (maxPrecisionAtRecallPoints[i] < currentMax) {
        maxPrecisionAtRecallPoints[i] = currentMax;
      } else {
        currentMax = maxPrecisionAtRecallPoints[i];
      }
    }

    String outputResult = "";
    for (int i = 0; i < 11; i++) {
      outputResult += "," + maxPrecisionAtRecallPoints[i];
    }
    System.out.println("query" + "\t" + outputResult);
  }

  // average precision
  public static void evaluateQueryMetric4(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments) {
    double recall = 0.0;
    double sumPrecision = 0.0;
    double hitCount = 0.0;

    int collectionSize = docids.size();

    for (int numToJudge = 1; numToJudge <= collectionSize; numToJudge++) {
      double currRecall = evaluateQueryRecall(query, docids, judgments, numToJudge);
      if (currRecall > recall) {
        sumPrecision += evaluateQueryPrecision(query, docids, judgments, numToJudge);
        recall = currRecall;
        hitCount++;
      }
    }

    System.out.println(query + "\t" + (sumPrecision/hitCount));
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
