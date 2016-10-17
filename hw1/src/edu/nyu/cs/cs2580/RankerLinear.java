package edu.nyu.cs.cs2580;

import java.util.*;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Use this template to implement the linear ranker for HW1. You must
 * use the provided _betaXYZ for combining the signals.
 * 
 * @author congyu
 * @author fdiaz
 */
public class RankerLinear extends Ranker {
  private float _betaCosine = 1.0f;
  private float _betaQl = 1.0f;
  private float _betaPhrase = 1.0f;
  private float _betaNumviews = 1.0f;
  private RankerCosine rankerCosine;
  private RankerQl rankerQl;
  private RankerPhrase rankerPhrase;
  private RankerNumviews rankerNumviews;
  private List<Double> cosineScores = new ArrayList<Double>();
  private List<Double> qlScores = new ArrayList<Double>();
  private List<Double> phraseScores = new ArrayList<Double>();
  private List<Double> numviewsScores = new ArrayList<Double>();
  /*
  private HashMap<Document, Double> documentScoreHashMap = new HashMap<Document, Double>();
  */

  public RankerLinear(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
    _betaCosine = options._betaValues.get("beta_cosine");
    _betaQl = options._betaValues.get("beta_ql");
    _betaPhrase = options._betaValues.get("beta_phrase");
    _betaNumviews = options._betaValues.get("beta_numviews");

    rankerCosine = new RankerCosine(options, arguments, indexer);
    rankerQl = new RankerQl(options, arguments, indexer);
    rankerPhrase = new RankerPhrase(options, arguments, indexer);
    rankerNumviews = new RankerNumviews(options, arguments, indexer);

  }

  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
    System.out.println("  with beta values" +
        ": cosine=" + Float.toString(_betaCosine) +
        ", ql=" + Float.toString(_betaQl) +
        ", phrase=" + Float.toString(_betaPhrase) +
        ", numviews=" + Float.toString(_betaNumviews));
    Vector<ScoredDocument> all = new Vector<ScoredDocument>();
    // @CS2580: fill in your code here.
    getAllScores(query);
    normalization(cosineScores);
    normalization(qlScores);
    normalization(phraseScores);
    normalization(numviewsScores);
    for (int i = 0; i < _indexer.numDocs(); ++i) {
      all.add(scoreDocument(query, i));
    }
    Collections.sort(all, Collections.reverseOrder());


    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    for (int i = 0; i < all.size() && i < numResults; ++i) {
      results.add(all.get(i));
    }
    TsvGen.generate(all, "hw1.1-linear");

    return results;
  }

  private void getAllScores(Query query) {
    for (int i = 0; i < _indexer.numDocs(); ++i) {
      cosineScores.add(rankerCosine.scoreDocument(query, i).getScore());
      qlScores.add(rankerQl.scoreDocument(query, i).getScore());
      phraseScores.add(rankerPhrase.scoreDocument(query, i).getScore());
      numviewsScores.add(rankerNumviews.scoreDocument(query, i).getScore());
    }
  }

  private void normalization(List<Double> l) {
    double maxVal = l.get(0);
    double minVal = l.get(0);
    for (double d : l) {
      if (d > maxVal) {
        maxVal = d;
      }
      if (d < minVal) {
        minVal = d;
      }
    }
    for (int i = 0; i < l.size(); i++) {
      double val = (l.get(i) - minVal)/(maxVal - minVal);
      l.set(i, val);
    }
  }

  private ScoredDocument scoreDocument(Query query, int did) {
    Document doc = _indexer.getDoc(did);
    double score = cosineScores.get(did) * _betaCosine
        + qlScores.get(did) * _betaQl
        + phraseScores.get(did) * _betaPhrase
        + numviewsScores.get(did) * _betaNumviews;
    return new ScoredDocument(query._query, doc, score);
  }
}
