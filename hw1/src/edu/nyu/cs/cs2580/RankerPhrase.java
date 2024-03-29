package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Use this template to implement the phrase ranker for HW1.
 * 
 * @author congyu
 * @author fdiaz
 */
public class RankerPhrase extends Ranker {

  public RankerPhrase(Options options,
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
    // generate csv file for all document records
    TsvGen.generate(all, "hw1.1-phrase");
    return results;
  }

  protected ScoredDocument scoreDocument(Query query, int did) {
    // Get the document tokens.
    Document doc = _indexer.getDoc(did);
    Vector<String> docTokens = ((DocumentFull) doc).getConvertedTitleTokens();

    // Score the document.
    double score = 0.0;
    for (String docToken : docTokens) {
      for (String queryToken : query._tokens) {
        if (docToken.equals(queryToken)) {
          score += 1;
        }
      }
    }
    
    return new ScoredDocument(query._query, doc, score);
  }
}
