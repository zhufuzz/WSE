package edu.nyu.cs.cs2580;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

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
        TsvGen.generate(all, "hw1.1-vsm");
        //        Evaluator evaluator = new Evaluator();
        //        evaluator.evalRanker(query._query, results, "data/labels.tsv", "cosine");
        return results;
    }


    protected ScoredDocument scoreDocument(Query query, int did){
        Document doc = _indexer.getDoc(did);
        Vector<String> docTokens = ((DocumentFull) doc).getConvertedBodyTokens();
        double docNum = _indexer._numDocs;
        Hashtable<String, Double> termFreqTable = doc.getTermFrequencyTable();

        ////      construct un-normalized document vector
        //      Hashtable<String, Double> termFreqTable = new Hashtable<String, Double>();
        //      for(String docToken : docTokens){
        //          if(termFreqTable.get(docToken)==null) {
        //              double freq = Collections.frequency(docTokens, docToken);
        //              double tf = freq / docTokens.size();
        //              double nk = _indexer.corpusDocFrequencyByTerm(docToken);
        //              double idf = Math.log(docNum / nk);
        //              termFreqTable.put(docToken, tf * idf);
        //          }
        //      }
        ////      calculate l2 nomalization factor
        //      Enumeration<Double> valEnum = termFreqTable.elements();
        //      double l2Norm = 0.0;
        //      while(valEnum.hasMoreElements()){
        //          l2Norm+=Math.pow(valEnum.nextElement(),2);
        //      }
        //      l2Norm = Math.sqrt(l2Norm);
        //
        ////      normalize the vector
        //      Enumeration<String> keySet = termFreqTable.keys();
        //      while(keySet.hasMoreElements()){
        //          String key = keySet.nextElement();
        //          Double val = termFreqTable.get(key);
        //          val = val/l2Norm;
        //          termFreqTable.put(key,val);
        //      }

        //      construct query vector
        Vector<String> queryTokens = query._tokens;
        Hashtable<String, Double> queryVec = new Hashtable<String, Double>();
        for(String queryToken : queryTokens){
            if(queryVec.get(queryToken)==null){
                double freq = Collections.frequency(queryTokens, queryToken);
                queryVec.put(queryToken,freq/queryTokens.size());
            }

        }

        //      calculate the cosine dist
        double nominator = 0.0;
        double l2NormQ = 0.0;
        for(String queryToken : queryTokens){
            double dij = 0.0;
            if(termFreqTable.get(queryToken)!=null){dij = termFreqTable.get(queryToken);}
            double qj = queryVec.get(queryToken);
            nominator += dij*qj;
            l2NormQ += qj*qj;
        }


        Enumeration<Double> dijEnum = termFreqTable.elements();
        double dijSquares = 0.0;
        while(dijEnum.hasMoreElements()){
            dijSquares+=Math.pow(dijEnum.nextElement(),2);
        }


        double denominator = Math.sqrt(dijSquares*l2NormQ);
        double score = nominator/denominator;
        return new ScoredDocument(query._query, doc, score);
    }

}
