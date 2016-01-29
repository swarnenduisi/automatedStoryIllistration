/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atomatedstoryillustration;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author swarnenduchakraborty
 */
public class TfIdf {

    static float tf = 1;
    static float idf = 0;
    private static float tfidf_score;
    static float[] tfidf = null;

    static IndexReader indexreader;

    //public TfIdf() throws IOException {
    //    this.indexreader = DirectoryReader.open(FSDirectory.open(new File("/Users/swarnenduchakraborty/study/indexNew_1/")));
    //}
    public static void scoreCalculator(String field, String term) throws IOException {

        indexreader = DirectoryReader.open(FSDirectory.open(new File("/Users/swarnenduchakraborty/study/indexNew_1/")));
        /* TFIDFSimilarity tfidfSIM = new DefaultSimilarity();

         Bits liveDocs = MultiFields.getLiveDocs(indexreader);
         TermsEnum termEnum = MultiFields.getTerms(indexreader, field).iterator(null);

         BytesRef bytesRef = null;
         while ((bytesRef = termEnum.next()) != null) {
         //System.out.println("term == " + term);
         if (bytesRef.utf8ToString().trim().equals(term.trim())) {
         if (termEnum.seekExact(bytesRef)) {
         idf = tfidfSIM.idf(termEnum.docFreq(), indexreader.numDocs());
         DocsEnum docsEnum = termEnum.docs(liveDocs, null);
         if (docsEnum != null) {
         int doc = 0;
         while ((doc = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
         tf = tfidfSIM.tf(docsEnum.freq());
         tfidf_score = tf * idf;
         System.out.println(tfidf_score);
         }
         }
         }
         }
         }*/

        DefaultSimilarity similarity = new DefaultSimilarity();
        int docnum = indexreader.numDocs();
        Fields fields = MultiFields.getFields(indexreader);
        System.out.println(fields.toString());
        //Iterator i = new Iterator();
        //i = fields.iterator();
        //System.out.println(i);
        
        System.out.println(docnum);
        Terms terms = fields.terms(field);
        TermsEnum termsEnum = terms.iterator(null);
        while (termsEnum.next() != null) {
            double idf = similarity.idf(termsEnum.docFreq(), docnum);
            //tf = similarity.tf(docsEnum.freq());
            System.out.println("" + field + ":" + termsEnum.term().utf8ToString() + " idf=" + idf);
        }
    }


public static void main(String[] args) throws IOException {
        //TfIdf tdidf = new TfIdf();
        scoreCalculator("En_Caption", "Fox");

    }
}
