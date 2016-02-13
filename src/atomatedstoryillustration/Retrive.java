/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atomatedstoryillustration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author swarnenduchakraborty
 */
// This class retrive the documents depending on the query
public class Retrive {

    public static IndexReader reader;
    public static IndexSearcher searcher;
    static final float LM_LAMBDA = 0.4f;
    static Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_4_9);
    static FileWriter fw, fw_rel;
    static FileWriter fw_p5;
    static float BM_k, BM_v;
    static float LMJelink_K;
    Properties prop = null;
    String indexDir = null;
    String resultDumpDir = null;
    HashMap<String, HashMap<String, Terms>> hm_tf = new HashMap<String, HashMap<String, Terms>>();

    // Construtor taking K,V value for the BM25 parameter
    public Retrive(float BM_k, float BM_v, Properties prop) throws IOException {
        this.BM_k = BM_k;
        this.BM_v = BM_v;
        this.prop = prop;
        this.indexDir = prop.getProperty("index");
        this.resultDumpDir = prop.getProperty("resultDump");
        this.fw = new FileWriter(new File(resultDumpDir + "result.txt"));

    }

    HashMap getHashMapForTermVector() {
        return hm_tf;
    }

    public static HashMap searchInIndex(String indexDir, String q, int temp, int noOfDocTobeRet) throws IOException, Exception {

        QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "Searchable_Field", analyzer);
        reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
        searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(BM_k, BM_v));     // BM25 Search

        Query query = parser.parse(QueryParser.escape(q));          // Making the query
        TopScoreDocCollector collector = TopScoreDocCollector.create(noOfDocTobeRet, true);// collecting top documents by searcher method
        searcher.search(query, collector);

        TopDocs topdocs = searcher.search(query, noOfDocTobeRet);
        ScoreDoc[] hits = topdocs.scoreDocs;

        HashMap<String, Terms> hm_temp = new HashMap<String, Terms>();

        for (int i = 0; i < hits.length; i++) {

            int count = 10;
            Document hitDoc = searcher.doc(hits[i].doc);
            String p = (hitDoc.get("Image_FilePath"));
            Terms terms = reader.getTermVector(hits[i].doc, "Searchable_Field");
            float score = hits[i].score;
            String strScore = Float.toString(score);
            String Searchable = (hitDoc.get("Searchable_Field"));

            p = p.replace("/", ",");
            String[] path = p.split(",");
            String tmp = path[path.length - 1];

            tmp = tmp.replace(".", ",");
            String[] only = tmp.split(",");

            hm_temp.put(only[0], terms);            // put the doc id and the terms vector to the hashmap

                                                    // Write to original qrel_format result file
            fw.write(String.valueOf(temp));         // Query Id
            fw.write(" ");

            String secondField = "Q" + temp;        // Query Second Id
            fw.write(secondField);
            fw.write(" ");

            fw.write(only[0]);                      // Document Retrieved
            fw.write(" ");

            fw.write("0");                          // rank
            fw.write(" ");

            fw.write(strScore);                     // sim Score
            fw.write(" ");

            fw.write("run");                        // Run name
            fw.write("\n");

            /*
             // Write to a file for RF method <Qid , docid , Score , Searchable_Field_Text>
             fw_rel.write(String.valueOf(temp));                     // Qid
             fw_rel.write(
             "\t");
             fw_rel.write(only[0]);                                  // Doc id
             fw_rel.write(
             "\t");
             fw_rel.write(strScore);                                 // Score

             fw_rel.write(
             "\t");
             fw_rel.write(Searchable);                               // Searchable_string

             fw_rel.write("\n");
             */
        }

        reader.close();
        return hm_temp;

    }

    static HashMap<String, Terms> updateMap(HashMap<String, Terms> mapcurr, HashMap<String, Terms> maptmp) {

        for (Map.Entry e : maptmp.entrySet()) {
            if (!mapcurr.containsKey(e.getKey())) {
                String docid = e.getKey().toString();
                Terms termVector = (Terms) e.getValue();
                mapcurr.put(docid, termVector);
            }

        }
        return mapcurr;

    }

    // This function parse the test story xml file and call the searchInIndex
    void parseXmlTest() throws IOException {

        String indexDir = prop.getProperty("index");
        String queryDir = prop.getProperty("story");
        HashMap<String, Terms> hm_curr = new HashMap<String, Terms>();
        HashMap<String, Terms> hm_tmp = new HashMap<String, Terms>();

        File inputStory = new File(queryDir + "short-stories-test-set.xml");                    // file for query

        // read and parse Xml
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(inputStory);

            // parent tree
            NodeList nList1 = doc.getElementsByTagName("stories");
            NodeList nList3 = doc.getElementsByTagName("story");

            for (int prevtmp = 0; prevtmp < nList3.getLength(); prevtmp++) {

                Node n = nList3.item(prevtmp);
                Element el = (Element) n;

                //Searcing by title
                String title = el.getElementsByTagName("title").item(0).getTextContent().trim();
                hm_tmp = searchInIndex(indexDir, title, prevtmp, 100);

                hm_curr = updateMap(hm_curr, hm_tmp);      // update the hm_curr with new docids and term vector

                // Searching by text only
                String text = el.getElementsByTagName("text").item(0).getTextContent().trim();
                hm_tmp = searchInIndex(indexDir, text, prevtmp, 100);

                hm_curr = updateMap(hm_curr, hm_tmp);      // update the hm_curr with new docids and term vector

                // Searching by Entities Only
                String entities = el.getElementsByTagName("entities").item(0).getTextContent().trim();
                String[] entityPart = entities.split("\n");
                for (int a = 0; a < entityPart.length; a++) {
                    String entity = entityPart[a].trim();
                    hm_tmp = searchInIndex(indexDir, entity, prevtmp, 100);
                    hm_curr = updateMap(hm_curr, hm_tmp);
                }

                // Searching by Actions
                String actions = el.getElementsByTagName("actions").item(0).getTextContent().trim();
                String[] actionPart = actions.split("\n");
                for (int a = 0; a < actionPart.length; a++) {
                    String action = actionPart[a].trim();
                    //hm_curr_tmp = searchInIndex(indexDir, action, prevtmp,1000);

                }

                // Searching by events
                String s = el.getElementsByTagName("events").item(0).getTextContent().trim();
                String[] s1 = s.split("\n");
                for (int h = 0; h < s1.length; h++) {
                    String event = s1[h].trim();
                    hm_tmp = searchInIndex(indexDir, event, prevtmp, 100);
                    hm_curr = updateMap(hm_curr, hm_tmp);
                }

                String c = String.valueOf(prevtmp);

                hm_tf.put(c, hm_curr);                  // put the <Qid , hm<Docid , terms>> to final hashmap

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        fw.close();                         // Close the qrel_format retrived file
        //fw_rel.close();                  // Close the qrel_format retrieved file with Searchable_Field for RF
    }

    // This function removes duplicate doc number from result file as for each query of a story one doc can be retrieved multiple time
    void removeDupDocName() throws IOException {

        File f = new File(resultDumpDir + "result.txt");     // Output file in qrel format for retrieved docs
        BufferedReader br = new BufferedReader(new FileReader(f));

        FileWriter fw = new FileWriter(new File(resultDumpDir + "result" + BM_k + "_" + BM_v + ".txt")); // This file contains the entry with duplicate removed

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
                    checkcount++;                                       // Dont add this to qrel file
                    continue;
                    
                } else {

                    list.add(qidimageid);                               // Add to the qrel file

                    fw.write(parts[0]);                                //Qid
                    fw.write(" ");                              
                    
                    fw.write(parts[1]);                                 // Query Second Id
                    fw.write(" ");
                    
                    fw.write(parts[2]);                                 // Document Retrieved
                    fw.write(" ");
                    
                    fw.write(parts[3]);                                 // rank
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

   

    public static void main(String[] args) throws IOException, Exception {

        Properties prop = new Properties();
        InputStream input = new FileInputStream("/Users/swarnenduchakraborty/study/dissertation/init.properties");
        prop.load(input);

        Retrive r = new Retrive(1.5f, 0.5f, prop);      // Create object with K and V value
        r.parseXmlTest();                               // parse the xml file for test set
        r.removeDupDocName();                           // remove the duplicate docs

    }

}
