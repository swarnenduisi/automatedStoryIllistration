/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atomatedstoryillustration;

/**
 *
 * @author swarnenduchakraborty
 */
import java.io.*;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.LockObtainFailedException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class AtomatedStoryIllustration {

    static String id, name, filepath, lang_en, en_caption, lang_de, de_caption, lang_fr, fr_caption, en_description, en_comment, all_comment;
    public static final String FILES_TO_INDEX_DIRECTORY = "filesToIndex";
    public static final String INDEX_DIRECTORY = "indexDirectory";
    static IndexWriter writer;
    static String Searchable = "";
    static String procName = "";
    static int count;

    static org.apache.lucene.document.Document getDocument() throws Exception {

        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

        doc.add(new Field("Image_Id", id,
                Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("Image_FilePath", filepath,
                Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("Image_Name", procName,
                Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("En_Caption", en_caption,
                Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("En_Description", en_description,
                Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("En_Comment", en_comment,
                Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("All_Comment", all_comment,
                Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("Searchable_Field", Searchable,
                Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));

        return doc;

    }

    static void init(Properties prop) throws IOException {
        
        String indexDirectory = prop.getProperty("index");
        File indexDir = new File(indexDirectory);
        Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_4_9);
        IndexWriterConfig iwcfg = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
        iwcfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        writer = new IndexWriter(FSDirectory.open(indexDir), iwcfg);

    }

    public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException, Exception {

        org.apache.lucene.document.Document document = getDocument();
        writer.addDocument(document);

    }

    public static void stringProcess() {

        name = name.replace(".", ",");
        procName = name.split(",")[0];
        //Searchable = en_description + " " + en_comment + " " + en_caption + " " + procName;
        
        Searchable = procName.trim() + " " + en_caption.trim() ;
        Searchable = Searchable.trim().toLowerCase();
        

    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, Exception {
        int fileno = 0;
        
        Properties prop = new Properties();
        InputStream input = new FileInputStream("/Users/swarnenduchakraborty/study/dissertation/init.properties");
        prop.load(input);
        
        String metadataPath = prop.getProperty("image.metadata");
        
        
        for (int j = 1; j <= 26; j++) {
            File dir = new File(metadataPath + j + "/");
            System.out.println(metadataPath + j + "/");
            File[] directoryListing = dir.listFiles();

            init(prop);

            if (directoryListing != null) {
                for (File child : directoryListing) {
                    
                    fileno++;
                    
                    try {
                        
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(child);

                    
                        id = doc.getDocumentElement().getAttribute("id");
                        filepath = doc.getDocumentElement().getAttribute("file");

                        // extract the name of the image
                        NodeList nlist_name = doc.getElementsByTagName("name");
                        Node nNode_name = nlist_name.item(0);
                        name = nNode_name.getTextContent();
                    

                        //extarct all other info
                        NodeList nList = doc.getElementsByTagName("text");

                        for (int temp = 0; temp < nList.getLength(); temp++) {
                            Node nNode = nList.item(temp);
                            

                            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                                Element eElement = (Element) nNode;
                            

                                // english
                                if (eElement.getAttribute("xml:lang").equals("en")) {

                                    lang_en = eElement.getAttribute("xml:lang");
                                    en_caption = eElement.getElementsByTagName("caption").item(0).getTextContent();
                                    en_description = eElement.getElementsByTagName("description").item(0).getTextContent();
                                    en_comment = eElement.getElementsByTagName("comment").item(0).getTextContent();
                            
                                } // german
                                else if (eElement.getAttribute("xml:lang").equals("de")) {

                                    lang_de = eElement.getAttribute("xml:lang");
                                    de_caption = eElement.getElementsByTagName("caption").item(0).getTextContent();

                                } // french
                                else if (eElement.getAttribute("xml:lang").equals("fr")) {

                                    lang_fr = eElement.getAttribute("xml:lang");
                                    fr_caption = eElement.getElementsByTagName("caption").item(0).getTextContent();

                                }
                            }
                        }

                        NodeList nlist_comment = doc.getElementsByTagName("comment");
                        
                        Node nNode_comment = nlist_comment.item(3);
                    

                        all_comment = nNode_comment.getTextContent();
                        all_comment = all_comment.replaceAll("https?://\\S+\\s?", "");
                        all_comment = all_comment.replaceAll(" ", "Z");
                        all_comment = all_comment.replaceAll("[^A-Za-z]+", "");
                        all_comment = all_comment.replaceAll("Z", " ");
                    
                        stringProcess();
                        createIndex();
                    } catch (Exception e) {
                        System.out.println(e);
                        count++;
                    }
                }
            }

            writer.close();
        }
        
        
        System.out.println("number of file :: " +fileno);
    }
}
