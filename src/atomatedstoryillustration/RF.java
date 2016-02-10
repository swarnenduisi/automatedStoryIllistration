/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atomatedstoryillustration;

//import static atomatedstoryillustration.RelevanceFeedback.reader;
//import atomatedstoryillustration.Retrive.removeDupDocName;
import static atomatedstoryillustration.Retrive.searchInIndex;
import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    HashMap<String, String> relRethm = new HashMap<String, String>();
    //HashMap<String, HashMap<String, String>> resultHm = new HashMap<String, HashMap<String, String>>();  //  this map contains <Qid , DocId , SearchableString>
    HashMap<String, ArrayList> qrelHm = new HashMap<String, ArrayList>();

    static HashMap<String, HashMap<String, Terms>> hm_tf_tmp = new HashMap<String, HashMap<String, Terms>>();
    static HashMap<String, HashMap<String, Terms>> x = new HashMap<String, HashMap<String, Terms>>();

    int prevqid = 0;
    static IndexReader reader;
    static IndexSearcher searcher;

    // Constructor
    RF() throws IOException {
        fw_score = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/score.txt"));
        reader = DirectoryReader.open(FSDirectory.open(new File("/Users/swarnenduchakraborty/study/indexNew_2/")));
        searcher = new IndexSearcher(reader);
        loadQrelFileToHashMap();
        //hm_tf_tmp = hm_tf; 

    }

    void loadQrelFileToHashMap() throws FileNotFoundException, IOException {                   // loading qrel file to hashmap <Qid , Docid--s>

        BufferedReader br_qrel = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/qrel_new"));

        String line = "";
        while ((line = br_qrel.readLine()) != null) {
            //System.out.println(line);
            String[] qrelParts = line.split("\t");
            String qid = qrelParts[0];                          // extract the qid part
            String imageid = qrelParts[2];                      // Extract the image id part
            int rel_factor = Integer.parseInt(qrelParts[3]);    // Extrcat the relevance factor
            //System.out.println("This is qid " + qid);
            if (rel_factor > 0) {                                 // Only the relevant docid are stored in hashmap
                if (qrelHm.containsKey(qid)) {                    // if already the qid contain in  arraylist
                    ArrayList current = qrelHm.get(qid);
                    if (!current.contains(imageid)) {             // if imageid dosenot contains in the arraylist
                        current.add(imageid);                   // add the imageid to the arraylist
                    }

                    qrelHm.put(qid, current);                   // add the qid , docid arraylist to the hasmap

                } else {
                    ArrayList curr = new ArrayList();
                    curr.add(imageid);
                    qrelHm.put(qid, curr);
                }

            }
        }
        //printHashmapQrelHm();
        //printHashMapforTermFrequency();
    }

    // print the Qrel Hash MAp for debugging purpose
    void printHashmapQrelHm() {

        for (Map.Entry<String, ArrayList> entry : qrelHm.entrySet()) {

            String key = entry.getKey();
            System.out.println("QueryId  :: " + key);
            ArrayList a = new ArrayList();
            a = entry.getValue();

            for (int g = 0; g < a.size(); g++) {
                System.out.print(a.get(g).toString());
                System.out.println(" ");
            }
            System.out.print("Size :: " + a.size());
            System.out.println();

        }
    }

    //void write
    void scoreCalculatorDriving() throws IOException {
        for (Map.Entry<String, HashMap<String, Terms>> entry : x.entrySet()) {

            String key = entry.getKey();
            System.out.println("Query Id  :: " + key);
            HashMap<String, Terms> z = entry.getValue();
            for (Map.Entry<String, Terms> entry_y : z.entrySet()) {
                String docid = entry_y.getKey();
                Terms terms = entry_y.getValue();
                TermsEnum term = terms.iterator(null);
                System.out.println("docid :: " + docid);
                while (term.next() != null) {
                    long fieldSize = terms.size();
                    System.out.println("Size of the field :: " + fieldSize);
                    String t = term.term().utf8ToString();

                    System.out.println("term :: " + term.term().utf8ToString());
                    //System.out.println("Doc freq " + term.docFreq());
                    System.out.println("term frq :: here :: " + term.totalTermFreq());
                    float f = (float) term.totalTermFreq();
                    System.out.println("f Value :: " + f);

                    float g = (float) fieldSize;
                    System.out.println("g Value :: " + g);
                    double tf = f / g;
                    System.out.println("rf ::::::: " + tf);
                    scoreCalculator(key, t, tf);  // input is  qid key , term , termfrequency 

                }
                System.out.println("--------------------------------------------------------");

                //System.out.println(value);
            }
            //String[] terms = value.split("\\s+");
            //Term thisTerm = null;

        }
        fw_score.close();
    }

    // This function takes two thing --1 > Hashmap of qrel file , ie. qrelHm<string(qid) , Arraylist(list of relevant docid)> and 2 > the file result_with SearchString.txt
    //This function produces a hashmap of relevant retrieved docs in a hasmap ds where <Qid , Searchable string from relevant retrieved> is found
    void relevantRetrival() throws FileNotFoundException, IOException {

        int f = 0;

        for (Map.Entry<String, HashMap<String, Terms>> entry : hm_tf_tmp.entrySet()) {

            String tf_qid = entry.getKey();               // Extract the qid for the doc-id arraylist of qrelhm
            System.out.println("tf qid :: " + tf_qid);
            HashMap<String, Terms> innerhm = hm_tf_tmp.get(tf_qid);      // Extract the arraylist of doc id
            HashMap<String, Terms> innerTmphm = new HashMap<String, Terms>();
            System.out.println("InnerHm Size :: " + innerhm.size());

            if (qrelHm.containsKey(tf_qid)) {                   // if the qrel file contains the qid then only do the below
                ArrayList qrel_docid = qrelHm.get(tf_qid);

                try {
                    System.out.println("qid :: " + tf_qid + "Lengh :: " + qrel_docid.size());
                    for (int g = 0; g < qrel_docid.size(); g++) {
                        System.out.println("el in arraylist qrel :: " + qrel_docid.get(g));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (Map.Entry<String, Terms> entryInner : innerhm.entrySet()) {
                    String innerkey = entryInner.getKey();
                    System.out.println("inner doc id key:: " + innerkey);
                    Terms tmpterms = entryInner.getValue();
                    if (qrel_docid.contains(innerkey)) {
                        System.out.println(" Matched" + " Added :: doc id " + innerkey);
                        innerTmphm.put(innerkey, tmpterms);
                        TermsEnum term = tmpterms.iterator(null);
                        while (term.next() != null) {
                            System.out.println("Thi sis ::" + term.totalTermFreq());
                        }
                        f++;
                        //System.out.println("Removed");
                    }
                }

                x.put(tf_qid, innerTmphm);

            }
        }
        System.out.println("Printing HashMap Term Freq :: ");
        scoreCalculatorDriving();
        System.out.println("Count == " + f);

    }
    
    // this is score calculating driving function. Write to the output file is not mandatory but this is done for debugging purpose

    void scoreCalculator(String qid, String t, double tf) throws IOException {

        Term trm = new Term("Searchable_Field", t);
        double idf = calciDF(trm);
        double tfIdfScore = tf * idf;
        System.out.println("qid --> " + qid);
        System.out.println("term --> " + t);
        System.out.println("tf --> " + tf);
        System.out.println("idf --> " + idf);
        System.out.println("Score --> " + tfIdfScore);

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
        System.out.println("term is" + term.toString());
        System.out.println("doc_fre" + (reader.docFreq(term)));
        //Term termInstance = new Term("Searchable_Field", t);

        double idf = Math.log(reader.numDocs() / (reader.docFreq(term) + 1));
        System.out.println("idf -->" + idf);
        return idf;

    }

    

    // this function add the vector term and write to the file
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
        FileWriter fw_s = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/final_score.txt");

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
                if(val.split(" ").length <=10){
                    val = val + " " + t;
                }
                p.put(qid, val);
            }
            
            else{
                p.put(qid, t);
            }
        }
        
        Retrive ret = new Retrive(1.5f, 0.75f);
        
        String indexDir1 = "/Users/swarnenduchakraborty/study/indexNew_2/";
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

    public static void main(String[] args) throws IOException, Exception {

        Retrive retrive = new Retrive(1.5f, 0.5f);  // Create object with K and V value
        retrive.parseXmlTest();                     // parsing XML file to create query and get the retrived docs
        retrive.removeDupDocName();
        hm_tf_tmp = retrive.getHashMapForTermVector();
        
        
        RF r = new RF();            
        r.relevantRetrival();                       // relevant doc extraction                
        r.addSimilarTermTFIDFScore();               // Vector Adding
        r.reRankedRF();                               // This function outputs a file in trec format
                          // Remove the 
        
    }

}
