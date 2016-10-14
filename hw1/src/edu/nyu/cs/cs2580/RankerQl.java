package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Use this template to implement the query likelihood ranker for HW1.
 * 
 * @author congyu
 * @author fdiaz
 */
public class RankerQl extends Ranker {

  public RankerQl(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
    Vector<ScoredDocument> all = new Vector<ScoredDocument>();
    // @CS2580: fill in your code here.
    for (int i = 0; i < _indexer.numDocs(); ++i) {
      all.add(scoreDocument(query, i));
    }
    Collections.sort(all, Collections.reverseOrder());
    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    for (int i = 0; i < all.size() && i < numResults; ++i) {
      results.add(all.get(i));
    }
    TsvGen.generate(results, "hw1.1-ql");
    //        Evaluator evaluator = new Evaluator();
    //        evaluator.evalRanker(query._query, results, "data/labels.tsv", "cosine");
    return results;
  }


  protected ScoredDocument scoreDocument(Query query, int did){
    Document doc = _indexer.getDoc(did);
    double docNum = _indexer._numDocs;
    Vector<String> docTokens = ((DocumentFull) doc).getConvertedBodyTokens();
    Vector<String> queryTokens = query._tokens;
    boolean init = true;
    double score = 0.0;
    for(String queryToken: queryTokens){
      double fqi = Collections.frequency(docTokens,queryToken);
      double pi = fqi/docTokens.size();
      if(init){
        score = pi;
        init = false;
      }
      else
        score *= score;
    }
    return new ScoredDocument(query._query, doc, score);
  }

}
