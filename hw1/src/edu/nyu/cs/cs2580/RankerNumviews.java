package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;
import edu.nyu.cs.cs2580.ScoredDocument;
import edu.nyu.cs.cs2580.MyCSV;

/**
 * @CS2580: Use this template to implement the numviews ranker for HW1.
 * 
 * @author congyu
 * @author fdiaz
 */
public class RankerNumviews extends Ranker {

  public RankerNumviews(Options options,
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
    
    // generate csv file for all document records
    MyCSV.generate(all, "hw1.1-numviews");

    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    for (int i = 0; i < all.size() && i < numResults; ++i) {
      results.add(all.get(i));
    }
    return results;
  }

  private ScoredDocument scoreDocument(Query query, int did) {
    // Process the raw query into tokens.
    query.processQuery();

    // Get the document tokens.
    Document doc = _indexer.getDoc(did);
    Vector<String> docTokens = ((DocumentFull) doc).getConvertedTitleTokens();

    // Score the document. Here we have provided a very simple ranking model,
    // where a document is scored 1.0 if it gets hit by at least one query term.
    double score = doc.getNumViews();
    
    return new ScoredDocument(query._query, doc, score);
  }
}
