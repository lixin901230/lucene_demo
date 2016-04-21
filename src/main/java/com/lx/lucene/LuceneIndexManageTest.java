package com.lx.lucene;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

/**
 * lucene 索引增删改 示例
 * 
 * @author lx
 */
public class LuceneIndexManageTest {
	
	/**
	 * 创建文档
	 * @return
	 */
	public static List<Document> getDocuments() {
		List<Document> docs = new ArrayList<Document>();
		for (int i = 1; i <= 5; i++) {
			Document document = new Document();
			document.add(new TextField("addIndex_test_"+i, "我的lucene学习案例，添加索引测试_"+i, Field.Store.YES));
			
			// 用于测试，只在第一个文件所在的文档中添加下面这些域
			if(i == 1) {
				//创建long、duble和int类型的field并添加到索引文档中（还可以添加其他的数字类型Field）
				document.add(new LongField("longContent", 100, Field.Store.YES));
				document.add(new DoubleField("doubleContent", 26.5, Field.Store.YES));
				document.add(new IntField("intContent", 30, Field.Store.YES));
			}
			docs.add(document);
		}
		return docs;
	}
	
	/**
	 * 添加索引
	 */
	@Test
	public void addIndex() {
		
		IndexWriter writer = null;
		try {
			writer = createIndexWriter();
			
			List<Document> docs = getDocuments();
			
			
			// 方式1、使用IndexWriter直接去操作索引
			// 1）、循环添加多个文档
			for (Document document : docs) {
				writer.addDocument(document);
			}
			// 2）、批量添加文档
			/*writer.addDocuments(docs);
			
			
			// 方式2、使用IndexWriter的委派对象trackingIndexWriter去操作索引
			TrackingIndexWriter trackingIndexWriter = new TrackingIndexWriter(writer);
			// 1）、循环添加多个文档
			for (Document document : docs) {
				trackingIndexWriter.addDocument(document);
			}
			// 2）、批量添加文档
			trackingIndexWriter.addDocuments(docs);*/

			
			// 提交
			writer.forceMerge(1);	//武力合并策略，合并片段直到小于等于设置的最大的的片段数（Forces merge policy to merge segments until there are <= maxNumSegments.）
			writer.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 删除索引
	 */
	@Test
	public void deleteIndex() {
		
		IndexWriter writer = null;
		try {
			writer = createIndexWriter();
			
			// 创建两个词条对象
			Term term = new Term("addIndex_test_1", "添加索引测试");
			Term term2 = new Term("content", "案例");
			
			// 数值范围查询
			NumericRangeQuery<Double> query = NumericRangeQuery.newDoubleRange("doubleContent", 26.5, 27.5, true, false);//从doubleContent中查找[26.5~27.5)之间的数组
			// 词条查询
			TermQuery termQuery = new TermQuery(term);
			
			
			//方式1：直接使用IndexWriter操作索引
			// 1)、删除全部索引
//			writer.deleteAll();

			// 2)、根据查询条件删除索引
			writer.deleteDocuments(query);
			writer.deleteDocuments(new Query[]{query, termQuery});
			// 3)、根据词条删除索引
//			writer.deleteDocuments(term);
//			writer.deleteDocuments(new Term[]{term, term2});
			
			
			// 方式2：使用IndexWriter的委派对象trackingIndexWriter操作索引
//			TrackingIndexWriter trackingIndexWriter = new TrackingIndexWriter(writer);
			// 1)、删除全部索引
//			trackingIndexWriter.deleteAll();
			// 2)、根据查询条件删除索引
//			trackingIndexWriter.deleteDocuments(query);
//			trackingIndexWriter.deleteDocuments(new Query[]{query, termQuery});
			// 3)、根据词条删除索引
//			trackingIndexWriter.deleteDocuments(term);
//			trackingIndexWriter.deleteDocuments(new Term[]{term, term2});
			
			
			// 提交
			writer.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 修改索引
	 */
	public static void updateIndex() {
		
		IndexWriter writer = null;
		try {
			writer = createIndexWriter();
			
			List<Document> docs = getDocuments();
			
			// 创建词条对象（根据该词条查找需要修改的文档）
			Term term = new Term("addIndex_test_1", "测试1");
			
			
			// 方式1：直接使用IndexWriter操作索引
			// 1）、修改一个文档
			for (Document document : docs) {
				writer.updateDocument(term, document);	//更新一个文档首先删除包含指定词条的所有文档,然后添加新文档（Updates a document by first deleting the document(s) containing term and then adding the new document.）
			}
			// 2）、批量修改文档
			writer.updateDocuments(term, docs);
			// 3）、修改文档中的域值
			TextField textField = new TextField("", new StringReader("示例"));
			DoubleField doubleField = new DoubleField("", 6.5, Field.Store.YES);
			writer.updateDocValues(term, new Field[]{textField, doubleField});	//用给定的值更新文档 DocValues域(Updates documents' DocValues fields to the given values.)
			// 4）、修改数值类型的文档值
			writer.updateNumericDocValue(term, "longContent", 102);
			
			
			// 方式2：使用IndexWriter的委派对象trackingIndexWriter操作索引
			TrackingIndexWriter trackingIndexWriter = new TrackingIndexWriter(writer);
			// 1）、修改一个文档
			for (Document document : docs) {
				trackingIndexWriter.updateDocument(term, document);	//更新一个文档首先删除包含指定词条的所有文档,然后添加新文档（Updates a document by first deleting the document(s) containing term and then adding the new document.）
			}
			// 2）、批量修改文档
			trackingIndexWriter.updateDocuments(term, docs);
			
			
			// 提交
			writer.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * 获取索引文件存放路径（用于测试）
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
	 * 创建索引存储目录对象Directory<br/>
	 * 	若要对索引文件进行分开存储，则可根据业务数据将不同类型数据的索引分别存放在多个不同的目录中
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
	
	/**
	 * 创建索引操配置对象
	 * @return
	 */
	public static IndexWriterConfig createIndexWriterConfig() {
		Analyzer analyzer = new SmartChineseAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		return indexWriterConfig;
	}
	
	/**
	 * 创建索引文件操作对象
	 * @return
	 * @throws IOException
	 */
	public static IndexWriter createIndexWriter() throws IOException {
		Directory directory = createDirectory();
		IndexWriterConfig writerConfig = createIndexWriterConfig();
		if(!checkExistsIndex()) {
			writerConfig.setOpenMode(OpenMode.CREATE);	//Create a new index in the directory, removing any previously indexed documents
		} else {
			writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);	// Add new documents to an existing index
		}
		IndexWriter indexWriter = new IndexWriter(directory, writerConfig);
		return indexWriter;
	}

	/**
	 * 检查是否存在索引文件
	 * @return boolean true：已存在索引文件；false：不存在索引文件
	 */
	public static boolean checkExistsIndex() {
		
		boolean isExists = false;
		String indexDirPath = getIndexDirPath();
		File file = new File(indexDirPath);
		File[] files = file.listFiles();
		for (File _file : files) {
			if(_file.exists() && _file.isFile() && _file.exists() 
					&& _file.getName().contains("segments")) {
				isExists = true;
			}
		}
		return isExists;
	}
	
}
