package com.lx.lucene;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.junit.Test;

import com.mysql.jdbc.jdbc2.optional.SuspendableXAConnection;

/**
 * lucene 索引 增 删 改 查 示例
 * 
 * @author lx
 */
public class LuceneIndexManageTest {
	
	/**
	 * 创建文档
	 * @return
	 */
	public static List<Document> getDocuments() {
		
		String text1 = "美国，其首都是华盛顿，是一个科技、军事非常发达的发达大国，但总喜欢充当世界警察的角色";
		String text2 = "日本，是一个海岛国家，生鱼片是他们的最爱的食物";
		String text3 = "法国，是一个充满神秘，浪漫色彩的国度";
		String text4 = "朝鲜，与中国接壤，是一个王位世袭制的国家，如今三胖当家，老与美国对着干";
		String text5 = "中国，是一个有着5000年历史文化的泱泱大国，是个追求和平明主的社会主义国家";
		
		List<Document> docs = new ArrayList<Document>();
		Document doc1 = new Document();
		doc1.add(new TextField("content", text1, Field.Store.YES));
		docs.add(doc1);
		
		Document doc2 = new Document();
		doc2.add(new TextField("content", text2, Field.Store.YES));
		docs.add(doc2);
		
		Document doc3 = new Document();
		doc3.add(new TextField("content", text3, Field.Store.YES));
		docs.add(doc3);
		
		Document doc4 = new Document();
		doc4.add(new TextField("content", text4, Field.Store.YES));
		docs.add(doc4);
		
		Document doc5 = new Document();
		doc5.add(new TextField("content", text5, Field.Store.YES));
		docs.add(doc5);
		
		Document doc6 = new Document();
		doc6.add(new NumericDocValuesField("numberContent", 2016l));	//不会被持久化存储，在索引文件中找不到
		docs.add(doc6);
		
		Document doc7 = new Document();
		doc7.add(new BinaryDocValuesField("binaryContent", new BytesRef("二进制文档值".getBytes())));	//不会被持久化存储，在索引文件中找不到
		docs.add(doc7);
		
		return docs;
	}
	
	/**
	 * 添加索引
	 */
	@Test
	public void createIndex() {
		
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
	 * 插入索引（在删除某个索引后，使用该方法重新插入删除的索引，测试是否能重新添加成功）
	 */
	@Test
	public void insertIndex() {
		
		IndexWriter writer = null;
		try {
			writer = createIndexWriter();
			
			writer.addDocument(getDocuments().get(0));	//重新新建美国索引
			writer.addDocument(getDocuments().get(4));	//重新新建中国索引
			
		} catch (IOException e) {
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
			Term term = new Term("content", "中国");
			Term term2 = new Term("content", "美国");
			
			// 词条查询
			TermQuery query = new TermQuery(term);
			FuzzyQuery query2 = new FuzzyQuery(term2);
			
			//方式1：直接使用IndexWriter操作索引
			// 1)、删除全部索引
			writer.deleteAll();

			// 2)、根据查询条件删除索引
//			writer.deleteDocuments(query);
//			writer.deleteDocuments(new Query[]{query, query2});
			// 3)、根据词条删除索引
//			writer.deleteDocuments(term);
//			writer.deleteDocuments(new Term[]{term, term2});
			
			
			// 方式2：使用IndexWriter的委派对象trackingIndexWriter操作索引
//			TrackingIndexWriter trackingIndexWriter = new TrackingIndexWriter(writer);
			// 1)、删除全部索引
//			trackingIndexWriter.deleteAll();
			// 2)、根据查询条件删除索引
//			trackingIndexWriter.deleteDocuments(query);
//			trackingIndexWriter.deleteDocuments(new Query[]{query, query2});
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
	@Test
	public void updateIndex() {
		
		IndexWriter writer = null;
		try {
			writer = createIndexWriter();
			
			// 准备用来修改的文档
			Document doc1 = new Document();
			doc1.add(new TextField("content", "美国US，其首都是华盛顿", Field.Store.YES));
			writer.addDocument(doc1);
			
			Document doc2 = new Document();
			doc2.add(new TextField("content", "美国，有很多州，首都华盛顿", Field.Store.YES));
			writer.addDocument(doc2);

			List<Document> docs = new ArrayList<Document>();
			docs.add(doc1);
			docs.add(doc2);
			
			
			// 创建词条对象（根据该词条查找需要修改的文档）
			Term term = new Term("content", "美国");
			
			// 方式1：直接使用IndexWriter操作索引
			// 1）、修改一个文档
			writer.updateDocument(term, doc1);	//更新一个文档首先删除包含指定词条的所有文档,然后添加新文档（Updates a document by first deleting the document(s) containing term and then adding the new document.）
			// 2）、批量修改文档
//			writer.updateDocuments(term, docs);
			
			// 3）、修改BinaryDocValuesField域值
//			Field textField = new BinaryDocValuesField("binaryContent",  new BytesRef("update二进制文档值".getBytes()));
//			writer.updateDocValues(term = new Term("numberContent", "二进制文档值"), textField);	//用给定的值更新文档 DocValues域(Updates documents' DocValues fields to the given values.)
//			writer.updateDocValues(term = new Term("numberContent", "二进制文档值"), new Field[]{textField});	//用给定的值更新文档 DocValues域(Updates documents' DocValues fields to the given values.)
			// 4）、修改NumericDocValuesField域值
//			writer.updateNumericDocValue(term = new Term("numberContent", "2016"), "numberContent", 20160424l);
			
			
			// 方式2：使用IndexWriter的委派对象trackingIndexWriter操作索引
//			TrackingIndexWriter trackingIndexWriter = new TrackingIndexWriter(writer);
			// 1）、修改一个文档
//			trackingIndexWriter.updateDocument(term, doc1);	//更新一个文档首先删除包含指定词条的所有文档,然后添加新文档（Updates a document by first deleting the document(s) containing term and then adding the new document.）
			// 2）、批量修改文档
//			trackingIndexWriter.updateDocuments(term, docs);
			
			
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
	
	@Test
	public void search() {
		
		try {
			Term term1 = new Term("content", "中国");
			Term term2 = new Term("content", "美国");
			Term term2_1 = new Term("content", "华盛顿");
			
			IndexSearcher searcher = createIndexSearcher();
			
			// 查询1
			Query query = new TermQuery(term1);
			TopDocs topDocs = searcher.search(query, 1000);
			System.out.println("查询到记录条数："+topDocs.totalHits);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			for (ScoreDoc scoreDoc : scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				String fieldName = doc.getFields().get(0).name();
				System.out.println(fieldName+"==搜索到的内容："+doc.get("content"));
				
				// 搜索结果中搜索关键字高亮显示处理
				String content = doc.get("content");
				Analyzer analyzer = new SmartChineseAnalyzer();
				String highlighterResult = highlightFormat("content", content, query, analyzer);
				System.out.println("结果高亮显示处理："+highlighterResult+"\n");
			}
			
			System.out.println("\n=================\n");
			
			// 查询2（多关键字查询）
			MultiPhraseQuery query2 = new MultiPhraseQuery();
			query2.add(term2);
			query2.add(term2_1);
			query2.setSlop(10);	//setSlop的参数是设置两个关键字term2与term2_1之间允许间隔的最大值
			
			TopDocs topDocs2 = searcher.search(query2, 1000);
			System.out.println("查询到记录条数："+topDocs2.totalHits);
			ScoreDoc[] scoreDocs2 = topDocs2.scoreDocs;
			for (ScoreDoc scoreDoc : scoreDocs2) {
				Document doc = searcher.doc(scoreDoc.doc);
				String fieldName = doc.getFields().get(0).name();
				System.out.println(fieldName+"==搜索到的内容："+doc.get("content"));
				
				
				// 搜索结果中搜索关键字高亮显示处理
				String content = doc.get("content");
				Analyzer analyzer = new SmartChineseAnalyzer();
				String highlighterResult = highlightFormat("content", content, query2, analyzer);
				System.out.println("结果高亮显示处理："+highlighterResult+"\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取索引文件存放路径（用于测试）
	 * @return
	 */
	public static String getIndexDirPath() {
		
		try {
			String classesPath = LuceneIndexManageTest.class.getResource("/").getPath();
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
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		return searcher;
	}
	
	/**
	 * 创建索引文件操作对象
	 * @return
	 * @throws IOException
	 */
	public static IndexWriter createIndexWriter() throws IOException {
		Directory directory = createDirectory();
		//Analyzer analyzer = new StandardAnalyzer();
		Analyzer analyzer = new SmartChineseAnalyzer();
		IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
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
	

	/**
	 * 高亮处理<br/>
	 * 搜索结果中的搜索关键字进行高亮显示处理
	 * @param field		取值时的属性名称
	 * @param content	根据field取出的值内容
	 * @param query		搜索时使用的查询对象
	 * @param analyzer	分词器
	 * @return
	 * @throws Exception
	 */
	public static String highlightFormat(String field, String content, Query query, Analyzer analyzer) throws Exception {
		QueryScorer queryScorer = new QueryScorer(query, field);
		Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span color='red'>", "</span>");
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter, queryScorer);
		highlighter.setTextFragmenter(fragmenter);
		TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(content));
		String highlighterResult = highlighter.getBestFragment(tokenStream, content);
		return highlighterResult;
	}
	
}
