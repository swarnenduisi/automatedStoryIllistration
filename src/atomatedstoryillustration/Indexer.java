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
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
    
    Analyzer analyzer;
    IndexWriter writer;
    IndexReader reader;
    IndexSearcher searcher;
    
    public Indexer(){
        
    }
    
    public Indexer(Properties prop,String indexName) throws Exception {        
		analyzer = new EnglishAnalyzer(Version.LUCENE_4_9);
                init(prop, indexName, analyzer);
	}
    
    void init(Properties prop, String indexConfigName, Analyzer analyzer) throws Exception {
		String indexDirPath = prop.getProperty(indexConfigName);
                File indexDir = new File(indexDirPath);
		IndexWriterConfig iwcfg = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
		iwcfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        
		writer = new IndexWriter(FSDirectory.open(indexDir), iwcfg);
        if (DirectoryReader.indexExists(FSDirectory.open(indexDir))) {
            reader = DirectoryReader.open(FSDirectory.open(indexDir));
            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new LMJelinekMercerSimilarity(0.6f));
        }        
    }
    
    public void close() throws IOException {
        if (reader!=null) reader.close();
		writer.close();        
    }
}
