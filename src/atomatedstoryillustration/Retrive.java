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
public class Retrive {

    static IndexReader reader;
    static IndexSearcher searcher;
    //static String indexDir = "";
    static final float LM_LAMBDA = 0.4f;
    static Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_4_9);
    static FileWriter fw,fw_rel;
    static FileWriter fw_p5;

    static float BM_k, BM_v;
    static float LMJelink_K;

    public Retrive(float BM_k, float BM_v) throws IOException {
        this.BM_k = BM_k;
        this.BM_v = BM_v;
        this.fw = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/result.txt"));
        this.fw_rel = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/result_with_searchstring.txt"));
        //this.fw_p5= new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/result_P5.txt"));
    }

    // This is for LM Jelink
    public Retrive(float LMJelink_K) throws IOException {
        this.LMJelink_K = LMJelink_K;
        //this.BM_v = BM_v;
        this.fw = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/result.txt"));
    }

    public static void searchInIndex(String indexDir, String q, int temp) throws IOException, Exception {

        QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "Searchable_Field", analyzer);

        reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));

        searcher = new IndexSearcher(reader);

        //LM Jelink Search
        //searcher.setSimilarity(new LMJelinekMercerSimilarity(LMJelink_K));
        // BM25 Search
        searcher.setSimilarity(new BM25Similarity(BM_k, BM_v));

        //Query query = buildQuery(q);QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "Searchable_Field", analyzer);
        reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));

        searcher = new IndexSearcher(reader);

        //LM Jelink Search
        //searcher.setSimilarity
        //Query query = parser.parse(q);
        Query query = parser.parse(QueryParser.escape(q));
        //String a = QueryParser.escape(q)

        System.out.println("Query is :: " + query);
        TopScoreDocCollector collector = TopScoreDocCollector.create(20, true);
        searcher.search(query, collector);
        TopDocs t = searcher.search(query, 20);

        //searcher.
        TopDocs topRetrived = collector.topDocs();
        ScoreDoc[] hits = topRetrived.scoreDocs;

        String part = "/Users/swarnenduchakraborty/study/dissertation/";

        for (int i = 0; i < hits.length; i++) {

            Document hitDoc = searcher.doc(hits[i].doc);
            float score = hits[i].score;
            String strScore = Float.toString(score);

            String p = (hitDoc.get("Image_FilePath"));
            String Searchable = (hitDoc.get("Searchable_Field"));
            
            p = p.replace("/", ",");
            String[] path = p.split(",");
            String tmp = path[path.length - 1];

            tmp = tmp.replace(".", ",");
            String[] only = tmp.split(",");

            // Query Id
            fw.write(String.valueOf(temp));
            fw.write(" ");
            
            fw_rel.write(String.valueOf(temp));
            fw_rel.write("\t");
            
            // Query Second Id
            String secondField = "Q" + temp;
            fw.write(secondField);
            fw.write(" ");
            
            fw_rel.write(secondField);
            fw_rel.write("\t");
            
            // Document Retrieved
            fw.write(only[0]);
            fw.write(" ");
            
            fw_rel.write(only[0]);
            fw_rel.write("\t");
            
            // rank
            fw.write("0");
            fw.write(" ");
            
            fw_rel.write("0");
            fw_rel.write("\t");
            // sim Score

            fw.write(strScore);
            fw.write(" ");
            
            fw_rel.write(strScore);
            fw_rel.write("\t");

            // Run name
            fw.write("run");
            fw.write("\n");
            
            fw_rel.write("run");
            fw_rel.write("\t");
            
            fw_rel.write(Searchable);
            fw_rel.write("\n");
            
            

        }

        reader.close();

    }

    static Query buildQuery(String queryStr) throws Exception {
        BooleanQuery q = new BooleanQuery();
        Term thisTerm = null;
        Query tq = null;
        String[] terms = queryStr.split("\\s+");

        for (String term : terms) {
            thisTerm = new Term("Searchable_Field", term);
            tq = new TermQuery(thisTerm);
            q.add(tq, BooleanClause.Occur.SHOULD);

            /*thisTerm = new Term(NewsDoc.FIELD_TITLE, term);
             tq = new TermQuery(thisTerm);
             q.add(tq, BooleanClause.Occur.SHOULD);

             thisTerm = new Term(NewsDoc.FIELD_DESC, term);
             tq = new TermQuery(thisTerm);
             q.add(tq, BooleanClause.Occur.SHOULD);
             */
        }
        return q;
    }

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

    void parseXmlTest() throws IOException {
        String indexDir = "/Users/swarnenduchakraborty/study/indexNew_2/";
        String queryDir = "/Users/swarnenduchakraborty/study/dissertation/stories-testset/";

        File inputStory = new File(queryDir + "short-stories-test-set.xml");

        // read Xml
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

                String s = el.getElementsByTagName("events").item(0).getTextContent().trim();
                String title = el.getElementsByTagName("title").item(0).getTextContent().trim();

                // Searching by text
                String text = el.getElementsByTagName("text").item(0).getTextContent().trim();
                //System.out.println("text is " + text);
                //System.out.println("text is " + text);
                //String[] line =text.split(".");

                //for(int i=0;i<line.length;i++){
                //    System.out.println("length is ::" + line.length);
                //    System.out.println(line[i]);
                //}
                
                searchInIndex(indexDir, text, prevtmp);
                // Searching for title
                //System.out.println("This is searching by ttitle :: ");
                //searchInIndex(indexDir, title, prevtmp);
                //String f = QueryParser.escape(text);
                //String[] parts = f.split(" ");
                //for(int h =0;h<parts.length;h++){
                //System.out.println(parts[h]);
                //searchInIndex(indexDir,parts[h] , prevtmp);
                //}
                // searching for entity
                
                String entities = el.getElementsByTagName("entities").item(0).getTextContent().trim();
                String[] entityPart = entities.split("\n");

                //System.out.println("This is searching by entity :: ");

                for (int a = 0; a < entityPart.length; a++) {
                    String entity = entityPart[a].trim();

                    //searchInIndex(indexDir, entity, prevtmp);

                }

                String actions = el.getElementsByTagName("actions").item(0).getTextContent().trim();

                String[] actionPart = actions.split("\n");

                for (int a = 0; a < actionPart.length; a++) {
                    String action = actionPart[a].trim();
                    //System.out.println(action + prevtmp);
                    //searchInIndex(indexDir, action, prevtmp);

                }
                //System.out.println("This is searching by event :: ");
                // search by event
                String[] s1 = s.split("\n");
                for (int h = 0; h < s1.length; h++) {

                    String event = s1[h].trim();
                    //searchInIndex(indexDir, event, prevtmp);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        fw.close();
        fw_rel.close();
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
                        searchInIndex(indexDir, queryText, queryid);
                        searchInIndex(indexDir, partsimagename[0], queryid);
                    }
                }
            }
        }
    }

    void removeDupDocName() throws IOException {

        File f = new File("/Users/swarnenduchakraborty/study/dissertation/result.txt");
        //File checkfile = new File("/Users/swarnenduchakraborty/study/dissertation/check.txt");
        FileWriter fw = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/result" + BM_k + "_" + BM_v + ".txt"));
        //BufferedReader br_checkfile = new BufferedReader(new FileReader(checkfile));
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = null;
        ArrayList<String> list = new ArrayList<String>();
        int checkcount = 0;
        int prevQueryid = 0;

        while ((line = br.readLine()) != null) {

            String[] parts = line.split("\\s+");

            if (parts.length != 6) {
                //System.out.println(line);
                break;
            } else {
                String imageid = parts[2];
                String qidimageid = parts[0] + parts[2];
                if (list.contains(qidimageid)) {
                    //if (list.contains(imageid)) {
                    System.out.println("queryid is -- " + qidimageid);
                    checkcount++;
                    //System.out.println("Count :: " + checkcount);
                    //System.out.println(line);
                    continue;
                } else {
                    
                    list.add(qidimageid);
                    //list.add(imageid);
                    fw.write(parts[0]);
                    fw.write(" ");
                    // Query Second Id
                    //String secondField = "Q" + temp;
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

    
    void relFeedback(){
        //get top 20 docs from the result_k_v file by score.
        
        
        
    }
    public static void main(String[] args) throws IOException, Exception {

        Retrive r = new Retrive(0.5f, 0.5f);
        r.parseXmlTest();
        
        //r.queryByrelevancefeedback();
        r.removeDupDocName();
        r.computeMAP();
        
    }

}
