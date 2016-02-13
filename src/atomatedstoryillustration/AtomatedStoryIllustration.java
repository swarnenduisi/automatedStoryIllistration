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

    static String id, name, filepath, lang_en, en_caption, lang_de, de_caption, lang_fr, fr_caption;
    public static final String FILES_TO_INDEX_DIRECTORY = "filesToIndex";
    public static final String INDEX_DIRECTORY = "indexDirectory";
    static IndexWriter writer;

    static org.apache.lucene.document.Document getDocument() throws Exception {

        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

        doc.add(new Field("Image_Id", id,
                Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("Image_FilePath", filepath,
                Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("Lang_En", lang_en,
                Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("En_Caption", en_caption,
                Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("Lang_de", lang_de,
                Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("De_Caption", de_caption,
                Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("Lang_fr", lang_fr,
                Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("Fr_Caption", fr_caption,
                Field.Store.YES, Field.Index.ANALYZED));

        return doc;

    }

    static void init() throws IOException {
        File indexDir = new File("/Users/swarnenduchakraborty/study/indexNew_1/");
        //Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_4_9);
        // boolean recreateIndexIfExists = true;
        IndexWriterConfig iwcfg = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
        iwcfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        //   IndexWriter indexWriter = new IndexWriter(INDEX_DIRECTORY, analyzer, recreateIndexIfExists);
        writer = new IndexWriter(FSDirectory.open(indexDir), iwcfg);

    }

    public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException, Exception {

        org.apache.lucene.document.Document document = getDocument();
        writer.addDocument(document);

    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, Exception {

        for (int j = 1; j <= 26; j++) {
            File dir = new File("/Users/swarnenduchakraborty/study/dissertation/all_text/metadata/" + j + "/");
            System.out.println("/Users/swarnenduchakraborty/study/dissertation/all_text/metadata/" + j + "/");
            File[] directoryListing = dir.listFiles();
//
            init();

            if (directoryListing != null) {
                for (File child : directoryListing) {
                    //System.out.println(child);
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(child);

                    //System.out.println("Root element :" + doc.getDocumentElement().getAttribute("id"));
                    //System.out.println("Root element :" + doc.getDocumentElement().getAttribute("file"));
                    id = doc.getDocumentElement().getAttribute("id");
                    filepath = doc.getDocumentElement().getAttribute("file");

                    // extract the name of the image
                    NodeList nlist_name = doc.getElementsByTagName("name");
                    Node nNode_name = nlist_name.item(0);
                    //System.out.println("Image name :: " + nNode_name.getTextContent());

                    //extarct all other info
                    NodeList nList = doc.getElementsByTagName("text");

                    for (int temp = 0; temp < nList.getLength(); temp++) {
                        Node nNode = nList.item(temp);
                        //System.out.println("\nCurrent Element :" + nNode.getNodeName());

                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            Element eElement = (Element) nNode;
                            //System.out.println("Language : " + eElement.getAttribute("xml:lang"));

                            // english
                            if (eElement.getAttribute("xml:lang").equals("en")) {

                                lang_en = eElement.getAttribute("xml:lang");
                                en_caption = eElement.getElementsByTagName("caption").item(0).getTextContent();
                                //System.out.println("Description: " + eElement.getElementsByTagName("description").item(0).getTextContent());
                                //System.out.println("Comment: " + eElement.getElementsByTagName("comment").item(0).getTextContent());
                                //System.out.println("Caption : " + eElement.getElementsByTagName("caption").item(0).getTextContent());
                            } // german
                            else if (eElement.getAttribute("xml:lang").equals("de")) {

                                lang_de = eElement.getAttribute("xml:lang");
                                de_caption = eElement.getElementsByTagName("caption").item(0).getTextContent();

                                //System.out.println("Description: " + eElement.getElementsByTagName("description").item(0).getTextContent());
                                //System.out.println("Comment: " + eElement.getElementsByTagName("comment").item(0).getTextContent());
                                //System.out.println("Caption : " + eElement.getElementsByTagName("caption").item(0).getTextContent());
                            } // french
                            else if (eElement.getAttribute("xml:lang").equals("fr")) {

                                lang_fr = eElement.getAttribute("xml:lang");
                                fr_caption = eElement.getElementsByTagName("caption").item(0).getTextContent();

                                //System.out.println("Description: " + eElement.getElementsByTagName("description").item(0).getTextContent());
                                //System.out.println("Comment: " + eElement.getElementsByTagName("comment").item(0).getTextContent());
                                //System.out.println("Caption : " + eElement.getElementsByTagName("caption").item(0).getTextContent());
                            }
                        }
                    }

                    //Properties prop = new Properties();
                    //prop.load(new FileReader("propety.list"));
                    //Indexer indexer = new Indexer(prop, "index");
                    createIndex();
                }

            }

            writer.close();
        }
    }
}
