/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atomatedstoryillustration;

//import static atomatedstoryillustration.TfIdf.indexreader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
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
    HashMap<String , HashMap<String , Terms>> hm_tf = new HashMap<String , HashMap<String , Terms>>();

    // Construtor taking K,V value for the BM25 parameter
    public Retrive(float BM_k, float BM_v) throws IOException {
        this.BM_k = BM_k;
        this.BM_v = BM_v;

        // this file contains the extracted document in the qrel format
        this.fw = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/result.txt"));
        //this file contains the extracted document with "Searchable_Field" also in qrel format .Mainly for relevance feedback
        this.fw_rel = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/result_with_searchstring.txt"));

    }
    
    HashMap getHashMapForTermVector(){
        return hm_tf; 
    }

    public static HashMap searchInIndex(String indexDir, String q, int temp, int noOfDocTobeRet) throws IOException, Exception {

        //BM_k =1.5f;
        //BM_v = 0.75f;
        
        QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "Searchable_Field", analyzer);
        reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
        searcher = new IndexSearcher(reader);

        // BM25 Search
        searcher.setSimilarity(new BM25Similarity(BM_k, BM_v));

        System.out.println("Query before :: " + q);

        // Making the query 
        Query query = parser.parse(QueryParser.escape(q));
        //Query query = buildQuery(q);

        System.out.println("Query is :: " + query.toString());

        // collecting top documents by searcher method
        TopScoreDocCollector collector = TopScoreDocCollector.create(noOfDocTobeRet, true);
        searcher.search(query, collector);

        TopDocs topdocs = searcher.search(query, noOfDocTobeRet);
        ScoreDoc[] hits = topdocs.scoreDocs;

        String part = "/Users/swarnenduchakraborty/study/dissertation/";

        System.out.println("Length :: " + hits.length);
        HashMap<String , Terms> hm_temp = new HashMap<String , Terms>();
        // write in the form of qrel file
        for (int i = 0; i < hits.length; i++) {

            
            Document hitDoc = searcher.doc(hits[i].doc);
            String p = (hitDoc.get("Image_FilePath"));
            //System.out.println("file path :: " +p);
            Terms terms = reader.getTermVector(hits[i].doc, "Searchable_Field");
            
            
            int count = 10;
            
            
            //System.out.println("doc " + i + " had " + numTerms + " terms");

            //Terms term = reader.
            float score = hits[i].score;
            String strScore = Float.toString(score);

            //String p = (hitDoc.get("Image_FilePath"));
            //System.out.println("file path :: " +p);
            String Searchable = (hitDoc.get("Searchable_Field"));

            p = p.replace("/", ",");
            String[] path = p.split(",");
            String tmp = path[path.length - 1];

            tmp = tmp.replace(".", ",");
            String[] only = tmp.split(",");

            // put to the hashmap
            hm_temp.put(only[0], terms);
            
            // Write to original qrel_format result file
            // Query Id
            fw.write(String.valueOf(temp));
            fw.write(" ");
            // Query Second Id
            String secondField = "Q" + temp;

            fw.write(secondField);

            fw.write(" ");
            // Document Retrieved
            fw.write(only[0]);
            fw.write(" ");
            // rank
            fw.write("0");
            fw.write(" ");
            // sim Score
            fw.write(strScore);

            fw.write(" ");
            // Run name
            fw.write("run");
            fw.write("\n");

            // Write to a file for RF method <Qid , docid , Score , Searchable_Field_Text>
            /*fw_rel.write(String.valueOf(temp));                     // Qid
            fw_rel.write(
                    "\t");
            fw_rel.write(only[0]);                                  // Doc id
            fw_rel.write(
                    "\t");
            fw_rel.write(strScore);                                 // Score

            fw_rel.write(
                    "\t");
            fw_rel.write(Searchable);                               // Searchable_string

            fw_rel.write(
                    "\n");*/

        }

        reader.close();
        return hm_temp;

    }

   /*

    // This is for parsing the validation stories
    void parseXml() throws Exception {

        String indexDir = "/Users/swarnenduchakraborty/study/indexNew_2/";
        String queryDir = "/Users/swarnenduchakraborty/study/";

        File inputStory = new File(queryDir + "stories-devset.xml");

        // read Xml
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(inputStory);

            // parent tree
            NodeList nList = doc.getElementsByTagName("story");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    // Entities Extraction
                    NodeList nListforEntity = doc.getElementsByTagName("events");

                    for (int temp1 = 0; temp1 < nListforEntity.getLength() - 1; temp1++) {

                        Node nNodeforEntity = nListforEntity.item(temp1);

                        if (nNodeforEntity.getNodeType() == Node.ELEMENT_NODE) {

                            Element eElementforEntity = (Element) nNodeforEntity;

                            String event = eElement.getElementsByTagName("event").item(temp1).getTextContent();

                            searchInIndex(indexDir, event, temp);
                        }

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        fw.close();
        //fw_rel.close();

    }

    // This function inputs one by one the term in the rel feed method and get back the retrieved docs
    void queryByRelvenceFeedback() throws FileNotFoundException, IOException, Exception {
        String indexDir = "/Users/swarnenduchakraborty/study/indexNew_3/";
        BufferedReader s2 = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/tf_idf_final_sort.txt"));
        String test = "";
        while ((test = s2.readLine()) != null) {

            System.out.println(test);
            String[] lineparts = test.split("\\s+");
            if (lineparts.length == 3) {
                int t1 = Integer.parseInt(lineparts[0]);        // Getting the query id
                String str = lineparts[1];                      // Getting the term
                searchInIndex(indexDir, str, t1);                // searInIndex
            }

        }

    }
*/
    
    
    // This function parse the test story xml file and call the searchInIndex
    void parseXmlTest() throws IOException {
        HashMap<String , Terms> hm_curr = new HashMap<String , Terms>();
        HashMap<String , Terms> hm_curr_tmp = new HashMap<String , Terms>();
        String indexDir = "/Users/swarnenduchakraborty/study/indexNew_3/";                      // index directory
        String queryDir = "/Users/swarnenduchakraborty/study/dissertation/stories-testset/";    // query set story director
        File inputStory = new File(queryDir + "short-stories-test-set.xml");                    // file for query

        // read and parse Xml
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(inputStory);

            // parent tree
            NodeList nList1 = doc.getElementsByTagName("stories");
            NodeList nList3 = doc.getElementsByTagName("story");
            System.out.println(nList3.getLength());

            for (int prevtmp = 0; prevtmp < nList3.getLength(); prevtmp++) {

                Node n = nList3.item(prevtmp);
                Element el = (Element) n;

                //Searcing by title
                String title = el.getElementsByTagName("title").item(0).getTextContent().trim();
                hm_curr_tmp = searchInIndex(indexDir, title, prevtmp,1000);
                
                
                // Searching by text only
                String text = el.getElementsByTagName("text").item(0).getTextContent().trim();
                hm_curr = searchInIndex(indexDir, text, prevtmp,1000);
                String c= String.valueOf(prevtmp);
                hm_tf.put(c, hm_curr);
                // Put this hasmap to 

                // Searching by Entities Only
                String entities = el.getElementsByTagName("entities").item(0).getTextContent().trim();
                String[] entityPart = entities.split("\n");
                for (int a = 0; a < entityPart.length; a++) {
                    String entity = entityPart[a].trim();
                    hm_curr_tmp = searchInIndex(indexDir, entity, prevtmp,1000);
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
                    hm_curr_tmp = searchInIndex(indexDir, event, prevtmp,1000);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        fw.close();                     // Close the qrel_format retrived file
        fw_rel.close();                 // Close the qrel_format retrieved file with Searchable_Field for RF
    }

    void queryByrelevancefeedback() throws FileNotFoundException, IOException, Exception {

        String indexDir = "/Users/swarnenduchakraborty/study/indexNew_2/";
        File retrive_relfeedback = new File("/Users/swarnenduchakraborty/study/dissertation/relfeed/retrived.txt");
        BufferedReader br = new BufferedReader(new FileReader(retrive_relfeedback));

        String line = "";
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            //System.out.println(line);
            //System.out.println("lenght == " + parts.length);
            if (parts.length == 5) {
                int queryid = Integer.valueOf(parts[0]);
                int relvalue = Integer.valueOf(parts[1]);
                String queryText = parts[4];
                String imagename = parts[3];
                imagename = imagename.replace(".", ",");
                String[] partsimagename = imagename.split(",");

                if (relvalue > 0) {

                    if (!queryText.equals(null)) {
                        //System.out.println("Querytext :: " + queryText);
                        //System.out.println("Queryid :: " + queryid);
                        searchInIndex(indexDir, queryText, queryid,1000);
                        searchInIndex(indexDir, partsimagename[0], queryid,1000);
                    }
                }
            }
        }
    }

    // This function removes duplicate doc number from result file as for each query of a story one doc can be retrieved multiple time
    void removeDupDocName() throws IOException {

        File f = new File("/Users/swarnenduchakraborty/study/dissertation/result.txt");     // Output file in qrel format for retrieved docs
        FileWriter fw = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/result" + BM_k + "_" + BM_v + ".txt")); // This file contains the entry with duplicate removed
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

    void computeMAP() throws IOException {

        String qrel_file = "/Users/swarnenduchakraborty/study/dissertation/qrel_new";
        String result_file = "/Users/swarnenduchakraborty/study/dissertation/result" + BM_k + "_" + BM_v + ".txt";
        String commandtohome = "cd ~";
        String commandtoDir = " cd /Users/swarnenduchakraborty/study/dissertation/trec_eval.8.1/";
        String command = "./trec_eval.8.1/trec_eval " + qrel_file + " " + result_file;

        FileWriter fw = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/combined_res.txt"), true);
        FileWriter fw_p5 = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/combined_P5.txt"), true);

        Process p;
        try {
            p = Runtime.getRuntime().exec(commandtohome);
            p.waitFor();

            p = Runtime.getRuntime().exec("pwd");
            p.waitFor();
            BufferedReader reader1
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            p = Runtime.getRuntime().exec(commandtoDir);
            p.waitFor();

            p = Runtime.getRuntime().exec("pwd");
            p.waitFor();
            BufferedReader reader2
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            p = Runtime.getRuntime().exec(command);
            p.waitFor();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");

                if (parts[0].equals("P5")) {
                    //System.out.println("P5" + parts[2]);
                }
                if (parts[0].equals("map")) {
                    //System.out.println(parts[2]);
                    fw.write(String.valueOf(BM_k));

                    fw.write(" ");

                    fw.write(String.valueOf(BM_v));

                    fw.write(" ");
                    fw.write(String.valueOf(parts[2]));
                    fw.write("\n");

                }

                if (parts[0].equals("P5")) {

                    //System.out.println("This is for P5");
                    fw_p5.write(String.valueOf(BM_k)); // for P5 
                    fw_p5.write(" ");
                    fw_p5.write(String.valueOf(BM_v)); // for P5 
                    fw_p5.write(" ");
                    fw_p5.write(String.valueOf(parts[2]));
                    fw_p5.write("\n");
                }
                //output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        fw.close();
        fw_p5.close();

    }

    public static void main(String[] args) throws IOException, Exception {

        Retrive r = new Retrive(1.5f, 0.5f);            // Create object with K and V value
        r.parseXmlTest();                               // parse the xml file for test set

        //r.queryByrelevancefeedback();                   // Query by relevance Feedback terms
        r.removeDupDocName();                           // remove the duplicate docs
        //r.computeMAP();                               // compute the MAP

    }

}
