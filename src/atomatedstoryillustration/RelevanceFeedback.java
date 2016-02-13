/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atomatedstoryillustration;

import static atomatedstoryillustration.Retrive.BM_k;
import static atomatedstoryillustration.Retrive.analyzer;
import static atomatedstoryillustration.Retrive.searchInIndex;
import static atomatedstoryillustration.Retrive.searcher;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author swarnenduchakraborty
 */
public class RelevanceFeedback {

    FileWriter fw, fw_1;
    FileWriter fw_relevant;
    FileReader fr;
    BufferedReader br;
    HashMap<String, String> hm = new HashMap<String, String>();
    int prevqid = 0;
    static IndexReader reader;
    static IndexSearcher searcher;

    RelevanceFeedback() throws IOException {

        fw = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/relfeed/retrived.txt"));
        //fw_1 = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/relfeed/filepath.txt"));
        //fr = new FileReader(new File("/Users/swarnenduchakraborty/study/dissertation/relfeed/filepath.txt"));
        fw_relevant = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/extracted_relevantdata.txt"));
        reader = DirectoryReader.open(FSDirectory.open(new File("/Users/swarnenduchakraborty/study/indexNew_2/")));
        searcher = new IndexSearcher(reader);
        //pathExtraction();
        //loadfilePath();
    }

    
    // This file takes the docid of the query and search the index to get the searchable field to calculate the tf and idf of the terms
    
    void extractSearchableFieldfromIndex() throws IOException, ParseException {

        IndexReader reader;
        IndexSearcher searcher;
        reader = DirectoryReader.open(FSDirectory.open(new File("/Users/swarnenduchakraborty/study/indexNew_2/")));
        searcher = new IndexSearcher(reader);
        BufferedReader br = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/relevant_ret.txt"));
        FileWriter fw = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/relevant_ret_with_searchstring.txt");
        String line = "";
        while ((line = br.readLine()) != null) {

            String[] parts = line.split("\\s+");
            String qid = parts[0];
            String docid = parts[1];
            System.out.println(qid);
            System.out.println(docid);

            QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "Image_Id", analyzer);
            searcher.setSimilarity(new BM25Similarity(1.5f, 0.5f));

            System.out.println("Query before :: " + docid);

            // Making the query 
            Query query = parser.parse(QueryParser.escape(docid));
            //Query query = buildQuery(q);

            System.out.println("Query is :: " + query.toString());

            // collecting top documents by searcher method
            TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
            searcher.search(query, collector);

            TopDocs topdocs = searcher.search(query, 1);
            ScoreDoc[] hits = topdocs.scoreDocs;
            org.apache.lucene.document.Document hitDoc = searcher.doc(hits[0].doc);
            String p = (hitDoc.get("Searchable_Field"));
            fw.write(qid);
            fw.write("\t");

            fw.write(docid);
            fw.write("\t");

            fw.write(p.trim());
            fw.write("\n");

        }

        fw.close();

    }

    

    // this function extracts the relevant file by matching the qrel and the retrived file
    void findRelevantRetrived() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {

        BufferedReader br_qrel = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/qrel_new"));
        FileWriter relevant_fw = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/relevant_ret.txt");

        String line = "";
        while ((line = br_qrel.readLine()) != null) {
            
            String[] qrelParts = line.split("\t");
            int qid = Integer.parseInt(qrelParts[0]);
            int imageid = Integer.parseInt(qrelParts[2]);
            int rel_factor = Integer.parseInt(qrelParts[3]);
            
            String retline = "";
            BufferedReader br_retrived = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/result1.5_0.5.txt"));
            while ((retline = br_retrived.readLine()) != null) {
                //System.out.println(retline);
                String[] retParts = retline.split(" ");
                int qid_ret = Integer.parseInt(retParts[0]);
                int imageid_ret = Integer.parseInt(retParts[2]);
                //String s1 = "";
                //''for (int h = 4; h < retParts.length; h++) {
                //    s1 = s1 + " " + retParts[h];
                //}
                //String searable_text = retline[6];
                //System.out.println("This is qid_ret" + qid_ret);
                //System.out.println("This is imageid_ret " + imageid_ret);
                if (qid == qid_ret && imageid == imageid_ret && rel_factor > 0) {
                    System.out.println("Matched");

                    /*relevant_fw.write(String.valueOf(qid));
                     relevant_fw.write("\t");
                    
                     relevant_fw.write(retParts[6]);
                     relevant_fw.write("\n");
                     */
                    relevant_fw.write(String.valueOf(qid));
                    relevant_fw.write("\t");

                    //relevant_fw.write("Q0");
                    //relevant_fw.write(" ");
                    relevant_fw.write(String.valueOf(imageid));
                    relevant_fw.write("\n");

                    //relevant_fw.write("0");
                    //relevant_fw.write(" ");
                    //relevant_fw.write(s1);
                    //relevant_fw.write("\n");

                    //relevant_fw.write("run");
                    //relevant_fw.write("\n");
                    // Call the function searchFilePath() to get the metadata of the file.This will search for the file path in the 
                    // file named filepath and get the metadata of the file and store the metadata
                    //searchFilePath(String.valueOf(imageid_ret),qid);
                }

            }
            br_retrived.close();
        }

        relevant_fw.close();
        //fw_relevant.close();

    }

    void ScoreCalculator() throws IOException {

        HashMap<String, String> hm = new HashMap<String, String>();
        //FileWriter fw_tfScore = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/tf_idf_Score.txt");
        BufferedReader br = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation//relevant_ret_with_searchstring.txt"));
        FileWriter fw_Score = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/relevant_tfidf.txt");

        // put to a hashmap qid and string
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            String qid = parts[0];
            String text = parts[2];

            String refinedtext = text.trim().toLowerCase();
            System.out.println(refinedtext);
            if (!hm.containsKey(qid)) {
                hm.put(qid, refinedtext);
            } else {
                hm.put(qid, hm.get(qid) + " " + refinedtext);
            }

        }

        // calculate tf-idf score
        for (Map.Entry<String, String> entry : hm.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String[] terms = value.split("\\s+");
            Term thisTerm = null;
            System.out.println("QID >>>>>>>>>> " +key);
            System.out.println("Cumalated Text>>>>>>>>> :: " +value);

            for (String term : terms) {
                term = term.replaceAll("[^a-zA-Z]", "");
                thisTerm = new Term("Searchable_Field", term);
                double idf = calciDF(thisTerm);
                double tf = tfCalculator(terms, term);
                double tfIdfScore = tf * idf;

                System.out.println("term :: " +term.toString() + "  TF :: " + tf + "  IDF :: " +idf + "  score :: " +tfIdfScore);
                // write the query id
                
                if( tfIdfScore > 0.0){
                fw_Score.write(key);
                fw_Score.write(" ");
                //write the term
                String refineTerm = term.replaceAll("[^a-zA-Z]", "");
                
                
                fw_Score.write(refineTerm);
                fw_Score.write(" ");

                // write the tf
                fw_Score.write(String.valueOf(tf));
                fw_Score.write(" ");

                // write the idf
                fw_Score.write(String.valueOf(idf));
                fw_Score.write(" ");

                // Write the tf-idf score
                fw_Score.write(String.valueOf(tfIdfScore));
                fw_Score.write("\n");
                }
            }

        }

        fw_Score.close();

    }

    // add the score of the similar term in the file

    void addScoreOfSimilarTerm() throws FileNotFoundException, IOException {
        BufferedReader br_Score = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/test/final_score_sort.txt"));
        FileWriter fw_accuScore = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/tfidf_vector.txt");

        //HashMap<String,String> map = new HashMap<String,String>();
        String line = "";

        while ((line = br_Score.readLine()) != null) {

            String[] parts = line.split(" ");
            if (parts.length == 3) {
                String qid = parts[0];
                String term = parts[1];
                //double score = Integer.valueOf(parts[4]);
                double aggScore = 0.0;

                fw_accuScore.write(qid);
                fw_accuScore.write(" ");

                fw_accuScore.write(term);
                fw_accuScore.write(" ");

                aggScore = Double.parseDouble(parts[2]);

                BufferedReader temp_br = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/test/final_score_sort_copy.txt"));
                String temp_line = "";
                while ((temp_line = temp_br.readLine()) != null) {
                    String[] temp_parts = temp_line.split(" ");

                    
                        String temp_qid = temp_parts[0];
                        String temp_term = temp_parts[1];

                        if (qid.equals(temp_qid) && term.equals(temp_term) && temp_parts.length == 3) {
                            aggScore = aggScore + Double.parseDouble(temp_parts[2]);
                            System.out.println("Mer");
                        }

                }    

                    fw_accuScore.write(String.valueOf(aggScore));
                    fw_accuScore.write("\n");

                
                temp_br.close();

            }

        }

        fw_accuScore.close();

    }
    
    
    void addSimilarTermTFIDFScore() throws FileNotFoundException, IOException, InterruptedException {
        
        String commandtohome = "cd ~";
        String commandtoDir = " cd /Users/swarnenduchakraborty/study/dissertation/";
        Process p;
        
        String target = new String("/Users/swarnenduchakraborty/study/dissertation/filehandle.sh");
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(target);
        proc.waitFor();

        
        
        ArrayList<String> list = new ArrayList<String>() {      // This term has been excluded from the query building
            {
                add("i");
                add("you");
                add("your");
                add("he");
                add("she");
                add("our");
                add("on");
                add("above");
                add("her");
                add("it");
                add("this");
                add("that");
                add("his");
                add("along");
                add("they");
                add("them");
                add("am");
                add("me");
                add("who");
                add("whom");

            }
        };

        System.out.println("This is adding vector");
        BufferedReader br_1 = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/merged_tfidf_sort.txt"));
        FileWriter fw_s = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/added_vector.txt");

        int flag = 0;
        String line = "";
        HashMap<String, Double> h = new HashMap<String, Double>();
        while ((line = br_1.readLine()) != null) {
            String[] parts = line.split(" ");
            String qid_1 = parts[0];
            String term_1 = parts[1];
            double score = Double.parseDouble(parts[2]);
            String nkey = qid_1 + "_" + term_1;
            System.out.println("Nkey :: " + nkey);

            if (h.containsKey(nkey)) {
                double val = h.get(nkey);
                val = val + score;
                h.put(nkey, val);
            } else {
                h.put(nkey, score);
            }
        }

        for (Map.Entry<String, Double> en : h.entrySet()) {

            String nkey_1 = en.getKey();
            String val = String.valueOf(en.getValue());
            System.out.println("Nkey :: " + nkey_1);
            //String key = nkey_1.substring(0,1);
            //String t = nkey_1.substring(1, nkey_1.length());

            String key = nkey_1.split("_")[0];
            String t = nkey_1.split("_")[1];
            System.out.println("key :: " + key);
            System.out.println("term :: " + t);
            System.out.println("score :: " + val);
            if (!list.contains(t)) {
                fw_s.write(key);
                fw_s.write(" ");

                fw_s.write(t);
                fw_s.write(" ");

                fw_s.write(String.valueOf(val));
                fw_s.write("\n");

                
            }
        }

        fw_s.close();

    }
    
    
    
    void reRankedRF() throws FileNotFoundException, IOException, Exception{
        //Retrive retrive = new Retrive(1.5f, 0.5f);
        
        String target = new String("/Users/swarnenduchakraborty/study/dissertation/sort.sh");
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(target);
        proc.waitFor();
        
        BufferedReader br = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/final_score_sort.txt"));
        String line ="";
        HashMap<String , String> p = new HashMap<String ,String>();
        
        while((line = br.readLine()) != null){
            String[] parts = line.split(" ");
            String qid=parts[0];
            String t = parts[1];
            
            if(p.containsKey(qid)){
                String val = p.get(qid);
                if(val.split(" ").length <=100){
                    val = val + " " + t;
                }
                p.put(qid, val);
            }
            
            else{
                p.put(qid, t);
            }
        }
       Properties prop = new Properties();
        Retrive ret = new Retrive(1.5f, 0.5f,prop);
        
        String indexDir1 = "/Users/swarnenduchakraborty/study/indexNew_4/";
        HashMap<String , Terms> hm_curr = new HashMap<String , Terms>();
        for (Map.Entry<String, String> en : p.entrySet()) {
           
            String key = en.getKey();
            String val = en .getValue();
            System.out.println("Key == " + key);
            System.out.println("val == " + val);
            int k = Integer.parseInt(key);
            
            hm_curr = searchInIndex(indexDir1, val, k,100);
        }
        
        ret.removeDupDocName();    
    }


    double calciDF(Term term) throws IOException {
        System.out.println("term is" + term.toString());
        System.out.println("doc_fre" + (reader.docFreq(term)));

        double idf = Math.log(reader.numDocs() / (reader.docFreq(term) + 1));
        System.out.println("idf -->" + idf);
        return idf;

    }
    
    void removeDup() throws IOException{
        
        File f = new File("/Users/swarnenduchakraborty/study/dissertation/result.txt");     // Output file in qrel format for retrieved docs
        FileWriter fw = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/result1.txt")); // This file contains the entry with duplicate removed
        
        BufferedReader br = new BufferedReader(new FileReader(f));

        String line = null;
        ArrayList<String> list = new ArrayList<String>();
        int checkcount = 0;
        int prevQueryid = 0;

        while ((line = br.readLine()) != null) {

            String[] parts = line.split("\\s+");                        // Splliting the line <qid , qid-t , docid,  0  ,score , runanme>
            if (parts.length != 6) {                                    // Continue if any value is missing
                continue;
            } else {
                String imageid = parts[2];                              // Extract the imageid
                String qidimageid = parts[0] + parts[2];                // 
                if (list.contains(qidimageid)) {                        // if this is true that means previouls this imageid has been seen

                    System.out.println("queryid is -- " + qidimageid);  // Dont add this to qrel file
                    checkcount++;
                    continue;
                } else {

                    list.add(qidimageid);                               // Add to the qrel file

                    //Qid
                    fw.write(parts[0]);                                 // Writting the qrel file
                    fw.write(" ");
                    // Query Second Id

                    fw.write(parts[1]);
                    fw.write(" ");
                    // Document Retrieved
                    fw.write(parts[2]);
                    fw.write(" ");
                    // rank
                    fw.write(parts[3]);
                    fw.write(" ");

                    fw.write(parts[4]);
                    fw.write(" ");

                    fw.write(parts[5]);
                    fw.write("\n");

                }

            }
        }
        fw.close();
    }
    
    

    double tfCalculator(String[] totalterms, String termToCheck) {
        double count = 0;  //to count the overall occurrence of the term termToCheck
        for (String s : totalterms) {
            if (s.equalsIgnoreCase(termToCheck)) {
                count++;
            }
        }
        return count / totalterms.length;
    }

    // Find the tf-idf score of the story text
    void storyTextScoreCalculator() throws FileNotFoundException, IOException {

        BufferedReader br_text = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/story_qid_text.txt"));
        FileWriter fw_text = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/story_text_tf_idf.txt");

        String line = "";
        HashMap<String, String> strory_text_hm = new HashMap<String, String>();
        while ((line = br_text.readLine()) != null) {
            String[] parts = line.split("\t");
            String qid = parts[0];
            String text = parts[1];
            String refinedtext = text.trim().toLowerCase();
            System.out.println(refinedtext);
            strory_text_hm.put(qid, refinedtext);

        }

        // calculate tf-idf score
        for (Map.Entry<String, String> entry : strory_text_hm.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String[] terms = value.split("\\s+");
            Term thisTerm = null;
            System.out.println(key);
            System.out.println(value);

            for (String term : terms) {
                term = term.replaceAll("[^a-zA-Z]", "");
                thisTerm = new Term("Searchable_Field", term);
                double idf = calciDF(thisTerm);
                double tf = tfCalculator(terms, term);
                double tfIdfScore = tf * idf;

                // write the query id
                fw_text.write(key);
                fw_text.write(" ");
                //write the term
                String refineTerm = term.replaceAll("[^a-zA-Z]", "");
                fw_text.write(refineTerm);
                fw_text.write(" ");

                // write the tf
                fw_text.write(String.valueOf(tf));
                fw_text.write(" ");

                // write the idf
                fw_text.write(String.valueOf(idf));
                fw_text.write(" ");

                // Write the tf-idf score
                fw_text.write(String.valueOf(tfIdfScore));
                fw_text.write("\n");

                System.out.println("TERM :: " + refineTerm);
                System.out.println("TF :: " + tf);
                System.out.println("IDF :: " + idf);
                System.out.println("Score :: " + tfIdfScore);

            }

        }

        fw_text.close();

    }
    
    void fileMerge() throws IOException, InterruptedException{
        
        String commandtohome = "cd ~";
        String commandtoDir = " cd /Users/swarnenduchakraborty/study/dissertation/";
        Process p;

        String target = new String("/Users/swarnenduchakraborty/study/dissertation/filehandle.sh");
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(target);
        proc.waitFor();
    }

    public static void main(String[] args) throws IOException, FileNotFoundException, ParserConfigurationException, SAXException, ParseException, InterruptedException, Exception {
        
        Properties prop = new Properties();
        InputStream input = new FileInputStream("/Users/swarnenduchakraborty/study/dissertation/init.properties");
        
        prop.load(input);
        
        
        
        
        Retrive retrive = new Retrive(1.5f, 0.5f,prop);  // Create object with K and V value
        retrive.parseXmlTest();                     // parsing XML file to create query and get the retrived docs
        retrive.removeDupDocName();
        
        
        
        RelevanceFeedback rel = new RelevanceFeedback();
        
        
        
        //rel.pathExtraction();
        //rel.dataRetrival();

        //rel.makeTextforScoreCalculation();
        
        //********************************
         
        rel.findRelevantRetrived();
        rel.extractSearchableFieldfromIndex();
        rel.ScoreCalculator();
        
        rel.fileMerge();
        //Next merge the score file from  story text
        // awk <qid docid score>
        // sort with sort -t " " -nk1 -r -nk3 final_score_refine.txt > final_score_sort.txt
        
        rel.addSimilarTermTFIDFScore();
        
        //uniq tfidf_vector.txt > final_vector.txt
        
        // retrive with the scored term
        
        
        rel.reRankedRF();
        rel.removeDup();
        
        
        //************************************
        
        
        
        //rel.calcTF();
        // rel feed score cacl
        // rel.ScoreCalculator();        //fw.close();
        //story text score calc
        //rel.storyTextScoreCalculator();
        //rel.storyTextScoreCalculator();
        //rel.addScoreOfSimilarTerm();
        //rel.addSimilarTermTFIDFScore();
        //rel.reRankedRF();
        //rel.removeDup();
        // after that remove duplicate lines
        //sort tf_idf_accu_Score.txt | uniq > tf_idf_final.txt
        //sort -t " " -nk1 -r -nk3 tf_idf_final.txt > tf_idf_final_sort.txt
    }
}
