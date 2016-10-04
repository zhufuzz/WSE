package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.StringJoiner;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Use this template to implement the cosine ranker for HW1.
 * 
 * @author congyu
 * @author fdiaz
 */
public class RankerCosine extends Ranker {

  public RankerCosine(Options options,
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
    TsvGen.generate(results,"hw1.3-vsm");
    return results;
  }


  private ScoredDocument scoreDocument(Query query, int did){
      Document doc = _indexer.getDoc(did);
      double docNum = _indexer._numDocs;
      Vector<String> docTokens = ((DocumentFull) doc).getConvertedBodyTokens();
      Vector<String> queryTokens = query._tokens;

      double l2DNorm = 0.0;
      double l2QNorm = 0.0;

      for(String queryToken: queryTokens){
          double nk = _indexer.corpusDocFrequencyByTerm(queryToken);
          double fik = Collections.frequency(docTokens,queryToken);
          double qk = Collections.frequency(queryTokens,queryToken);
          double prodD = Math.pow((Math.log(fik)+1)*(Math.log(docNum/nk)),2);
          double prodQ = Math.pow((Math.log(qk)+1),2);
          l2DNorm += prodD;
          l2QNorm += prodQ;
      }




      double nominator = 0.0;
      double dSum = 0.0;
      double qSum = 0.0;
      for(String queryToken:queryTokens){
          double nj = _indexer.corpusDocFrequencyByTerm(queryToken);
          double fij = Collections.frequency(docTokens,queryToken);
          double dij = fij==0? 0: (Math.log(fij)+1)*Math.log(docNum/nj)/Math.sqrt(l2DNorm);

          double queryFreq = Collections.frequency(queryTokens,queryToken);
          double qj = (Math.log(queryFreq)+1)/l2QNorm;

          nominator+=dij*qj;
          dSum+=dij*dij;
          qSum+=qj*qj;
      }
      double denominator = Math.sqrt(dSum*qSum);
      double score = denominator == 0? 0: nominator/denominator;
      return new ScoredDocument(query._query, doc, score);
  }

}
