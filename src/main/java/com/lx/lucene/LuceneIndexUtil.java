package com.lx.lucene;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * lucene 索引增删改 工具类
 * 
 * @author lx
 *
 */
public class LuceneIndexUtil {
	
	private TrackingIndexWriter trackingIndexWriter;
	
	public LuceneIndexUtil() throws IOException {
		IndexWriter writer = createIndexWriter();
		trackingIndexWriter = new TrackingIndexWriter(writer);
	}
	
	/**
	 * 获取索引文件存放路径
	 * @return
	 */
	public static String getIndexDirPath() {
		
		try {
			String classesPath = CreateIndexTable.class.getResource("/").getPath();
			classesPath = classesPath.startsWith("/") ? classesPath.substring(1) : classesPath;
			String indexDir = "";
			if(classesPath.indexOf("WEB-INF") > -1) {
				String webPath = classesPath.substring(0, classesPath.indexOf("WEB-INF"));
				indexDir = webPath + "luceneData/luceneIndex";	//索引存放路径
			} else if(classesPath.indexOf("target") > -1) {	//junit 测试获取的路径
				String webPath = classesPath.substring(0, classesPath.indexOf("target"));
				indexDir = webPath + "src/main/webapp/luceneData/luceneIndex";	//索引存放路径
			}
			System.out.println("indexDir=="+ indexDir);
			return indexDir;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 创建Directory
	 * @return
	 * @throws URISyntaxException 
	 * @throws IOException
	 */
	public static Directory createDirectory() throws IOException {
		
		String indexPath = getIndexDirPath();
		Directory directory = FSDirectory.open(Paths.get(indexPath));
		return directory;
	}
	
	/**
	 * 初始化搜索器（工具方法）
	 * @return
	 * @throws IOException
	 */
	public static IndexSearcher createIndexSearcher() throws IOException {
		
		Directory directory = createDirectory();
		DirectoryReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		return searcher;
	}
	
	public static IndexWriterConfig createIndexWriterConfig() {
		Analyzer analyzer = new SmartChineseAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		return indexWriterConfig;
	}
	
	public static IndexWriter createIndexWriter() throws IOException {
		Directory directory = createDirectory();
		IndexWriterConfig writerConfig = createIndexWriterConfig();
		IndexWriter indexWriter = new IndexWriter(directory, writerConfig);
		return indexWriter;
	}
	
	/**
	 * 添加索引
	 * @param document
	 */
	public void addIndex(Document document) {
		
//		trackingIndexWriter.addDocument();
	}
	
	/**
	 * 删除索引
	 * @throws IOException 
	 */
	public void deleteIndex() throws IOException {
		
//		IndexWriter writer = createIndexWriter();
//		Term term = new Term("", "");
////		DirectoryReader reader = DirectoryReader.open(directory);
////		Field field = new TextField("", reader);
//		writer.updateDocValues(term, field);
		
		trackingIndexWriter.deleteAll();
	}
	
	/**
	 * 修改索引
	 * @throws IOException 
	 */
	public void updateIndex() throws IOException {
		
//		trackingIndexWriter.updateDocument(t, d)
	}
	
	/**
	 * 提交
	 * @throws IOException
	 */
	public void commit() throws IOException {
		IndexWriter writer = createIndexWriter();
		writer.commit();
	}
	
}
