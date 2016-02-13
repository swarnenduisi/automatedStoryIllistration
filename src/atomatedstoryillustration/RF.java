/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atomatedstoryillustration;


import static atomatedstoryillustration.Retrive.searchInIndex;
import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author swarnenduchakraborty
 */
public class RF {

    FileWriter fw;
    FileWriter fw_relevant, fw_score;
    FileReader fr;
    BufferedReader br;
    Properties prop=null;
    int prevqid = 0;
    static IndexReader reader;
    static IndexSearcher searcher;
    String resultDumpDirectory = null;
    String indexDir = null;
    
    
    HashMap<String, String> relRethm = new HashMap<String, String>();
    HashMap<String, ArrayList> qrelHm = new HashMap<String, ArrayList>();
    static HashMap<String, HashMap<String, Terms>> hm_tf_tmp = new HashMap<String, HashMap<String, Terms>>();
    static HashMap<String, HashMap<String, Terms>> x = new HashMap<String, HashMap<String, Terms>>();

    

    // Constructor
    RF(Properties prop) throws IOException {
        this.prop = prop;
        resultDumpDirectory = prop.getProperty("resultDump");
        indexDir = prop.getProperty("index");
        
        fw_score = new FileWriter(new File(resultDumpDirectory + "score.txt"));
        reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
        searcher = new IndexSearcher(reader);
        loadQrelFileToHashMap();
        

    }

    void loadQrelFileToHashMap() throws FileNotFoundException, IOException {                   // loading qrel file to hashmap <Qid , Docid--s>

        String qrelFile = prop.getProperty("qrel");
        BufferedReader br_qrel = new BufferedReader(new FileReader(qrelFile));
        int c = 0;
        String line = "";
        while ((line = br_qrel.readLine()) != null) {
            
            String[] qrelParts = line.split("\t");
            String qid = qrelParts[0];                          // extract the qid part
            String imageid = qrelParts[2];                      // Extract the image id part
            int rel_factor = Integer.parseInt(qrelParts[3]);    // Extrcat the relevance factor
            
            if (rel_factor > 0) {                                 // Only the relevant docid are stored in hashmap
                if (qrelHm.containsKey(qid)) {                    // if already the qid contain in  arraylist
                    ArrayList current = qrelHm.get(qid);
                    if (!current.contains(imageid)) {             // if imageid dosenot contains in the arraylist
                        current.add(imageid);                   // add the imageid to the arraylist
                        c++;
                    }

                    qrelHm.put(qid, current);                   // add the qid , docid arraylist to the hasmap

                } else {
                    ArrayList curr = new ArrayList();
                    c++;
                    curr.add(imageid);
                    qrelHm.put(qid, curr);
                }

            }
        }
        

        System.out.println("Size of QrelHashMap :: " + c);
    }

    
    void scoreCalculatorDriving(Properties prop, HashMap<String, HashMap<String, Terms>> x) throws IOException {
        
        String resultDumpDirectory = prop.getProperty("resultDump");
                                                                                    //for testing purspose of the matched file write the <qid and relevant file as hashmap to a file>
        FileWriter fw3 = new FileWriter(resultDumpDirectory + "relevant.txt");
        for (Map.Entry<String, HashMap<String, Terms>> entry : x.entrySet()) {

            
            String key = entry.getKey();

            HashMap<String, Terms> z = entry.getValue();
            for (Map.Entry<String, Terms> entry_y : z.entrySet()) {
                String docid = entry_y.getKey();
                fw3.write(key);
                fw3.write("\t");

                fw3.write(docid);
                fw3.write("\n");

                Terms terms = entry_y.getValue();
                TermsEnum term = terms.iterator(null);
                
                while (term.next() != null) {
                    long fieldSize = terms.size();
                    String t = term.term().utf8ToString();
                    float f = (float) term.totalTermFreq();
                    float g = (float) fieldSize;
                    double tf = f / g;
                    scoreCalculator(key, t, tf);  // input is  qid key , term , termfrequency 

                }
                
            }
            

        }
        fw_score.close();
        fw3.close();
    }

    // This function takes two thing --1 > Hashmap of qrel file , ie. qrelHm<string(qid) , Arraylist(list of relevant docid)> and 2 > the file result_with SearchString.txt
    //This function produces a hashmap of relevant retrieved docs in a hasmap ds where <Qid , Searchable string from relevant retrieved> is found
    void relevantRetrival(Properties prop) throws FileNotFoundException, IOException {

        int doccount = 0;
        int f = 0;
        

        for (Map.Entry<String, HashMap<String, Terms>> entry : hm_tf_tmp.entrySet()) {

            String tf_qid = entry.getKey();                               // Extract the qid from hashmap
            HashMap<String, Terms> innerhm = hm_tf_tmp.get(tf_qid);      // Extract the <doid ,term> hashmap for the qid
            
            HashMap<String, Terms> innerTmphm = new HashMap<String, Terms>(); // to temporarily store the <docid , term> hashmap

            if (qrelHm.containsKey(tf_qid)) {                               // if the qrel file contains the qid then only do the below
                
                ArrayList qrel_docid = qrelHm.get(tf_qid);                  // get the relevant doclist corresponding to the qurty id
                //System.out.println("qid :: " + tf_qid + "Lengh :: " + qrel_docid.size());

                for(int j =0;j<qrel_docid.size();j++){
                    String doc = String.valueOf(qrel_docid.get(j));
                    if(innerhm.containsKey(doc)){
                        Terms trm = innerhm.get(doc);
                        innerTmphm.put(doc, trm);
                        f++;    
                    }
                    
                }
                doccount = doccount + innerTmphm.size();
                x.put(tf_qid, innerTmphm);
            }
        }
               
        System.out.println("Printing HashMap Term Freq :: ");
        scoreCalculatorDriving(prop,x);
        System.out.println("Count == " + f);
        System.out.println("DocCount == " + doccount);

    }

    // this is score calculating driving function. Write to the output file is not mandatory but this is done for debugging purpose
    void scoreCalculator(String qid, String t, double tf) throws IOException {

        Term trm = new Term("Searchable_Field", t);
        double idf = calciDF(trm);
        double tfIdfScore = tf * idf;
        

        fw_score.write(qid);
        fw_score.write(" ");

        fw_score.write(t);
        fw_score.write(" ");

        fw_score.write(String.valueOf(tf));
        fw_score.write(" ");

        fw_score.write(String.valueOf(idf));
        fw_score.write(" ");

        fw_score.write(String.valueOf(tfIdfScore));
        fw_score.write("\n");

    }

    double calciDF(Term term) throws IOException {                              // Calculate the idf of a term
        

        double idf = Math.log(reader.numDocs() / (reader.docFreq(term) + 1));
        return idf;

    }
    
    void mergeScoreWithStoryTextScore(Properties prop) throws IOException, InterruptedException{
        Process p;

        String fileHandle = prop.getProperty("filehandle.script");
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(fileHandle);
        proc.waitFor();
        
    }
    
    void sortingFileByScript(Properties prop) throws IOException, InterruptedException{
        
        String sortingScript= prop.getProperty("sorting.script");
        //String target = new String(sortingScript);
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(sortingScript);
        proc.waitFor();
        
    }

    // this function add the vector term and write to the file
    void addSimilarTermTFIDFScore(Properties prop) throws FileNotFoundException, IOException, InterruptedException {

        

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

        
        BufferedReader br_1 = new BufferedReader(new FileReader(resultDumpDirectory + "merged_tfidf_sort.txt"));
        FileWriter fw_s = new FileWriter(resultDumpDirectory + "final_score.txt");

        int flag = 0;
        String line = "";
        HashMap<String, Double> h = new HashMap<String, Double>();
        while ((line = br_1.readLine()) != null) {
            String[] parts = line.split(" ");
            String qid_1 = parts[0];
            String term_1 = parts[1];
            double score = Double.parseDouble(parts[2]);
            String nkey = qid_1 + "_" + term_1;
            

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
            

            String key = nkey_1.split("_")[0];
            String t = nkey_1.split("_")[1];
            
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

    void reRankedRF(Properties prop) throws FileNotFoundException, IOException, Exception {
        
        String resultDumpDirectory = prop.getProperty("resultDump");
        BufferedReader br = new BufferedReader(new FileReader(resultDumpDirectory + "final_score_sort.txt"));
        String line = "";
        HashMap<String, String> p = new HashMap<String, String>();

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(" ");
            String qid = parts[0];
            String t = parts[1];

            if (p.containsKey(qid)) {
                String val = p.get(qid);
                if (val.split(" ").length <= 120) {
                    val = val + " " + t;
                }
                p.put(qid, val);
            } else {
                p.put(qid, t);
            }
        }

        Retrive ret = new Retrive(1.5f, 0.5f,prop);

        String indexDir = prop.getProperty("index");
        HashMap<String, Terms> hm_curr = new HashMap<String, Terms>();
        for (Map.Entry<String, String> en : p.entrySet()) {

            String key = en.getKey();
            String val = en.getValue();
            
            int k = Integer.parseInt(key);

            hm_curr = searchInIndex(indexDir, val, k, 100);
        }

        

    }
    
    
    void removeDup() throws IOException{
        
        File f = new File(resultDumpDirectory +"result.txt");     // Output file in qrel format for retrieved docs
        FileWriter fw = new FileWriter(new File(resultDumpDirectory + "result1.txt")); // This file contains the entry with duplicate removed
        
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
    
   
    

    public static void main(String[] args) throws IOException, Exception {
        
        System.out.println("The Program Started ---------------------------");
        System.out.println("Properties File Loading.........................");
        Properties prop = new Properties();
        InputStream input = new FileInputStream("/Users/swarnenduchakraborty/study/dissertation/init.properties");
        
        prop.load(input);

        System.out.println("Retrieving the Initial documents according to the query........................");
        
        Retrive retrive = new Retrive(1.5f, 0.5f,prop);  // Create object with K and V value
        retrive.parseXmlTest();                     // parsing XML file to create query and get the retrived docs
        retrive.removeDupDocName();
        hm_tf_tmp = retrive.getHashMapForTermVector();

        System.out.println("The size of received HAM :: " + hm_tf_tmp.size());
        System.out.println("Retrieving Done.........");
        System.out.println("Relevance FeedBack Process Started...........");
        
        
        RF r = new RF(prop);
        System.out.println("Extracting the relevant docs according to the qrel file from Retrieved file");
        
        r.relevantRetrival(prop);                       // relevant doc extraction
        r.mergeScoreWithStoryTextScore(prop);
        
        System.out.println("Vector Adding Started....Finding the centroid.....");
        
        r.addSimilarTermTFIDFScore(prop);               // Vector Adding
        r.sortingFileByScript(prop);
        
        System.out.println("Retrieving Docs (by new query) by RF Method Started ");
        
        r.reRankedRF(prop);                               // This function outputs a file in trec format
        r.removeDup();
        
        System.out.println("The Process Ends.........................");
        System.out.println("Run the trec evaluation on the file " + r.resultDumpDirectory + "result1.txt");
        
        // Run the trec evaluation on the file result1.txt
      

    }

}
