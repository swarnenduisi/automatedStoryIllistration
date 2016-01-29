/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atomatedstoryillustration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
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
        fw_1 = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/relfeed/filepath.txt"));
        fr = new FileReader(new File("/Users/swarnenduchakraborty/study/dissertation/relfeed/filepath.txt"));
        fw_relevant = new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/extracted_relevantdata.txt"));
        reader = DirectoryReader.open(FSDirectory.open(new File("/Users/swarnenduchakraborty/study/indexNew_2/")));
        searcher = new IndexSearcher(reader);
        pathExtraction();
        loadfilePath();
    }

    void loadfilePath() throws IOException {
        String line;
        br = new BufferedReader(fr);
        while ((line = br.readLine()) != null) {

            String[] parts = line.split(" ");
            hm.put(parts[0], parts[1]);
        }
    }

    // find the filepath of a file
    void pathExtraction() throws IOException {
        String initpath = "/Users/swarnenduchakraborty/study/dissertation/all_text/metadata/";
        for (int j = 1; j <= 26; j++) {
            File dir = new File("/Users/swarnenduchakraborty/study/dissertation/all_text/metadata/" + j + "/");
            //System.out.println("/Users/swarnenduchakraborty/study/dissertation/all_text/metadata/" + j + "/");
            File[] directoryListing = dir.listFiles();

            if (directoryListing != null) {
                for (File child : directoryListing) {
                    //String path = initpath + "j/" + child;
                    String path = child.getPath();

                    //System.out.println(path);
                    String[] s = child.getName().replace(".", ",").split(",");
                    //System.out.println(s[0]);
                    fw_1.write(s[0].trim());
                    fw_1.write(" ");
                    fw_1.write(path);
                    fw_1.write("\n");

                }
            }
        }

        fw_1.close();
    }

    void dataRetrival() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
        FileSearch fileSearch = new FileSearch();
        File inputQrel = new File("/Users/swarnenduchakraborty/study/dissertation/qrel_new");
        BufferedReader br = new BufferedReader(new FileReader(inputQrel));
        String line;
        String pathToImageData = "/Users/swarnenduchakraborty/study/dissertation/all_text/metadata/";
        int lineNumber = 0;
        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            String[] parts = line.split("\\s+");
            //System.out.println(parts.length);
            String imageId = parts[2];
            //System.out.print(parts[0] + " ");
            fw.write(parts[0]);
            fw.write("\t");
            fw.write(parts[3]);
            fw.write("\t");
            fw.write(parts[2]);
            fw.write("\t");
            System.out.println(imageId + " ");
            //searchFilePath(imageId);
            lineNumber++;
            System.out.println("Line Number :: " + lineNumber);

        }
        fw.close();
    }

    // given image id it find the file path from hashmap and getsvalue from it
    void searchFilePath(String imageId, int qid) throws ParserConfigurationException, SAXException, IOException {

        System.out.println("Searching for the file path" + imageId);
        String path = "";
        if (hm.containsKey(imageId)) {
            path = hm.get(imageId);
            System.out.println("path :: " + path);
            getValueFromFile(path, qid);
        }

    }

    // Give imageid and get corresponding metadata of the file
    void getValueFromFile(String filepath, int qid) throws ParserConfigurationException, SAXException, IOException {

        File file = new File(filepath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);

        String extractedId = doc.getDocumentElement().getAttribute("id");
        String extractedPath = doc.getDocumentElement().getAttribute("file");

        // extract the name of the image
        NodeList nlist_name = doc.getElementsByTagName("name");
        Node nNode_name = nlist_name.item(0);

        String imageName = nNode_name.getTextContent();
        imageName = imageName.replace(".", ",");
        String[] imagenamePart = imageName.split(",");

        fw_relevant.write(String.valueOf(qid));
        fw_relevant.write("\t");

        fw_relevant.write(imagenamePart[0]);
        fw_relevant.write("\t");

        //fw.write(imageName);
        //fw.write("\t");
        //extarct all other info
        NodeList nList = doc.getElementsByTagName("text");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                // english
                if (eElement.getAttribute("xml:lang").equals("en")) {

                    String lang_en = eElement.getAttribute("xml:lang");
                    String en_caption = eElement.getElementsByTagName("caption").item(0).getTextContent();
                    System.out.println("Caption :: " + en_caption);
                    fw_relevant.write(en_caption);
                    fw_relevant.write("\n");

                }
                // german
                /*else if (eElement.getAttribute("xml:lang").equals("de")) {

                 String lang_de = eElement.getAttribute("xml:lang");
                 String de_caption = eElement.getElementsByTagName("caption").item(0).getTextContent();

                 } // french
                 else if (eElement.getAttribute("xml:lang").equals("fr")) {

                 String lang_fr = eElement.getAttribute("xml:lang");
                 String fr_caption = eElement.getElementsByTagName("caption").item(0).getTextContent();

                 } */

            }
        }

        String comment = doc.getDocumentElement().getAttribute("comment");

    }

    void makeTextforScoreCalculation() throws FileNotFoundException, IOException {

        // File f = new File("/Users/swarnenduchakraborty/study/dissertation/relfeed/retrived.txt");
        BufferedReader br1 = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/relfeed/ret.txt"));
        System.out.println(br1.readLine());
        FileWriter fw_relevant = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/relfeed/relevant.txt");
        //BufferedReader br_relevant = new BufferedReader(new FileReader(fw_relevant));

        FileWriter fw_nonrelevant = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/relfeed/nonrelevant.txt");
        //BufferedReader br_nonrelevant = new BufferedReader(new FileReader(fw_nonrelevant));

        String line;
        //String 
        int count = 0;
        int flag = 0;
        try {

            while ((line = br1.readLine()) != null) {
                System.out.println(line);
                String[] parts = line.split("\t");
                if (Integer.parseInt(parts[0]) == count) {
                    if (Integer.parseInt(parts[1]) > 0) {
                        fw_relevant.write(parts[3]);
                        if (parts.length == 5) {
                            fw_relevant.write(" ");
                            fw_relevant.write(parts[4]);
                        }
                    } else if (Integer.parseInt(parts[1]) == 0) {
                        fw_nonrelevant.write(parts[3]);
                        fw_nonrelevant.write(" ");
                        fw_nonrelevant.write(parts[4]);
                    }

                } else {
                    count++;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        fw_relevant.close();
        fw_nonrelevant.close();
        br1.close();
    }

    // this function extracts the relevant file by matching the qrel and the retrived file
    void findRelevantRetrived() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {

        BufferedReader br_qrel = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/qrel_new"));

        FileWriter relevant_fw = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/relevant_ret.txt");

        String line = "";
        while ((line = br_qrel.readLine()) != null) {
            //System.out.println(line);
            String[] qrelParts = line.split("\t");
            int qid = Integer.parseInt(qrelParts[0]);
            int imageid = Integer.parseInt(qrelParts[2]);
            int rel_factor = Integer.parseInt(qrelParts[3]);
            //System.out.println("This is qid " + qid);
            //System.out.println("This is imageid " + imageid);
            String retline = "";
            BufferedReader br_retrived = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/result0.5_0.5.txt"));
            while ((retline = br_retrived.readLine()) != null) {
                //System.out.println(retline);
                String[] retParts = retline.split(" ");
                int qid_ret = Integer.parseInt(retParts[0]);
                int imageid_ret = Integer.parseInt(retParts[2]);
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
                    relevant_fw.write(" ");

                    relevant_fw.write("Q0");
                    relevant_fw.write(" ");

                    relevant_fw.write(String.valueOf(imageid));
                    relevant_fw.write(" ");

                    relevant_fw.write("0");
                    relevant_fw.write(" ");

                    relevant_fw.write(retParts[4]);
                    relevant_fw.write(" ");

                    relevant_fw.write("run");
                    relevant_fw.write("\n");

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
        FileWriter fw_tfScore = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/tf_idf_Score.txt");
        BufferedReader br = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/relevant_ret.txt"));
        FileWriter fw_Score = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/tf_idf_Score.txt");
        
        // put to a hashmap qid and string
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            String qid = parts[0];
            String text = parts[1];
            //String refinedtext = text.replaceAll("\\-\\+\\.\\^:,@!`~$%&*[^0-9]?;#","");
            //String refinedtext = text.replaceAll("^\\p{L}+(?: \\p{L}+)*$", " ").toLowerCase();
            //refinedtext = refinedtext.replaceAll("\\d", "");
            //refinedtext = refinedtext.replaceAll(".,:;-", " ");
            //refinedtext = refinedtext.replaceAll("\\W", " ");
            //refinedtext = refinedtext.trim();
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
            System.out.println(key);
            System.out.println(value);

            
            

            for (String term : terms) {
                term = term.replaceAll("[^a-zA-Z]", "");
                thisTerm = new Term("Searchable_Field", term);
                double idf = calciDF(thisTerm);
                double tf = tfCalculator(terms, term);
                double tfIdfScore = tf * idf;
                
                // write the query id
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
        
        
        fw_Score.close();
        
        
        
    }
    // add the score of the similar term in the file
   void addScoreOfSimilarTerm() throws FileNotFoundException, IOException{
        BufferedReader br_Score = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/tf_idf_Score.txt"));
        FileWriter fw_accuScore = new FileWriter("/Users/swarnenduchakraborty/study/dissertation/tf_idf_accu_Score.txt");
        
        //HashMap<String,String> map = new HashMap<String,String>();
        
        String line = "";
        
        while ((line = br_Score.readLine()) != null) {
            String[] parts = line.split(" ");
            String qid = parts[0];
            String term =parts[1];
            //double score = Integer.valueOf(parts[4]);
            double aggScore=0.0;
            
            fw_accuScore.write(qid);
            fw_accuScore.write(" ");

            fw_accuScore.write(term);
            fw_accuScore.write(" ");
            
            BufferedReader temp_br = new BufferedReader(new FileReader("/Users/swarnenduchakraborty/study/dissertation/tf_idf_Score.txt"));
            String temp_line = "";
            while((temp_line = temp_br.readLine())!=null){
                String[] temp_parts= temp_line.split(" ");
                String temp_qid = temp_parts[0];
                String temp_term = temp_parts[1];
                
                if(qid.equals(temp_qid) && term.equals(temp_term)){
                    aggScore = aggScore + Double.parseDouble(temp_parts[4]);
                }
            }
            
            fw_accuScore.write(String.valueOf(aggScore));
            fw_accuScore.write("\n");
            temp_br.close();
            

        }
        
        fw_accuScore.close();
        
        
    }
    
    

    double calciDF(Term term) throws IOException {
        System.out.println("term is" +term.toString());
        System.out.println("doc_fre" + (reader.docFreq(term) ));
        
        double idf = Math.log(reader.numDocs() / (reader.docFreq(term) + 1));
        
        return idf;

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
    
    

    public static void main(String[] args) throws IOException, FileNotFoundException, ParserConfigurationException, SAXException {
        RelevanceFeedback rel = new RelevanceFeedback();
        //rel.pathExtraction();
        //rel.dataRetrival();

        //rel.makeTextforScoreCalculation();
        //rel.findRelevantRetrived();
        //rel.calcTF();
            rel.ScoreCalculator();        //fw.close();
            rel.addScoreOfSimilarTerm();
            // after that remove duplicate lines
            //sort tf_idf_accu_Score.txt | uniq > tf_idf_final.txt
            //sort -t " " -nk1 -r -nk3 tf_idf_final.txt > tf_idf_final_sort.txt
    }
}
