package com.lx.lucene.index.manager;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lx.lucene.util.LuceneManager;
import com.lx.util.CommonUtils;
import com.lx.util.FileUtils;

/**
 * 索引管理<br/><br/>
 * 注：此索引管理<b> 支持近实时搜索 </b>，
 * 即索引更新但未提交写入硬盘只是flush到缓冲区中，也能进行搜索，这样减少commit次数节省资源消耗，并由一定的提交策略提交缓冲区的索引写入硬盘<br/>
 * 比{@link LuceneManager}性能更好，因为{@link LuceneManager}类中每次索引操作都会commit提交索引写入硬盘，很耗费资源<br/><br/>
 * 
 * 使用{@link SearcherManager}实现原理：<br/>
 * 	只有IndexWriter上的commit操作才会导致{@link RAMDirectory}上的数据完全同步到文件。IndexWriter提供了实时获得reader的API，
 * 这个调用将导致flush操作，生成新的segment，但不会commit（fsync），从而减少 了IO。新的segment被加入到新生成的reader里。
 * 从返回的reader里，可以看到更新。所以，只要每次新的搜索都从IndexWriter获得一个新的reader，就可以搜索到最新的内容。
 * 这一操作的开销仅仅是flush，相对commit来说，开销很小。Lucene的index组织方式为一个index目录下的多个segment。
 * 新的doc会加入新的segment里，这些新的小segment每隔一段时间就合并起来。因为合并，总的segment数量保持的较小，总体search速度仍然很快。
 * 为了防止读写冲突，lucene只创建新的segment，并在任何active的reader不在使用后删除掉老的segment。
 * flush是把数据写入到操作系统的缓冲区，只要缓冲区不满，就不会有硬盘操作。
 * commit是把所有内存缓冲区的数据写入到硬盘，是完全的硬盘操作。
 * 重量级操作。这是因为，Lucene索引中最主要的结构posting通过VINT和delta的格式存储并紧密排列。
 * 合并时要对同一个term的posting进行归并排序，是一个读出，合并再生成的过程
 * 
 * @author lx
 */
public class IndexManager {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private IndexWriter writer;
	private SearcherManager manager;
	
	/* 
	 * 用TrackingIndexWriter类来包装IndexWriter，这样IndexWriter的索引操作委派给TrackingIndexWriter提供的索引添删改API来操作索引，
	 * 这样可以在IndexWriter未commit提交的情况下也能在ControlledRealTimeReopenThread中及时反应出来索引的变更，
	 * 这样在索引变更但未提交的情况下， 就能实现近实时搜索（之所以这样处理，因为IndexWriter的commit提交操作非常消耗资源，
	 * 所以在生产环境中应该想一个比较好的索引更新提交策略；
	 * 比如：1、定时提交，通过定时任务每隔一段时间后commit一下内存中变更的索引文档；2、定时合并内存索引与硬盘索引）
	 */
	private TrackingIndexWriter tkWriter;
	private ControlledRealTimeReopenThread<IndexSearcher> crtThread;
	
	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		
		IndexManager indexManager = new IndexManager();
		String fieldName = "content";
		String fieldValue = "橘子";
		List<Map<String, Object>> searchResult = indexManager.search(fieldName, fieldValue, true);
		System.out.println(searchResult);
	}
	
	public IndexManager() {
		try {
			String indexPath = getIndexDirPath();
			Directory directory = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new SmartChineseAnalyzer();
			IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
			writer = new IndexWriter(directory, writerConfig);
			manager = new SearcherManager(directory, new SearcherFactory());	//true 表示在内存中删除，false可能删可能不删，设为false性能会更好一些  
			
			tkWriter = new TrackingIndexWriter(writer);	//为writer 包装了一层
			
			//ControlledRealTimeReopenThread，主要将writer装，每个方法都没有commit 操作。
			//内存索引重读线程
			crtThread = new ControlledRealTimeReopenThread<IndexSearcher>(tkWriter, manager, 5.0, 0.025);
			crtThread.setDaemon(true);	//设置indexSearcher的守护线程
			crtThread.setName("Controlled Real Time Reopen Thread");
			crtThread.start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 对单条记录数据创建索引<br/>
	 * <b>注意：该map中的元素只能是简单类型</b>
	 * @param dbRowMap	需要创建索引的数据（该map相当于数据库中查出来的一条记录或一个数据bean）
	 * @param noAnalyzerFields	不需要进行分词的属性名称（如ID属性不需要进行分词，否则修改、删除时无法根据ID查找需要修改或删除的索引文档）
	 */
	public void addIndex(Map<String, Object> dbRowMap, String...noAnalyzerFields) {
		
		try {
			// 通过TrackingIndexWriter提供的api操作索引
			Document doc = getDocument(dbRowMap, noAnalyzerFields);
			tkWriter.addDocument(doc);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 对单条记录数据创建索引<br/>
	 * 依赖{@link CommonUtils#beanToMap(Object)}
	 * @param object	需要创建索引的数据对象
	 * @param noAnalyzerFields	不需要进行分词的属性名称（如ID属性不需要进行分词，否则修改、删除时无法根据ID查找需要修改或删除的索引文档）
	 */
	public void addIndex(Object object, String...noAnalyzerFields) {
		
		Map<String, Object> dbRowMap = CommonUtils.beanToMap(object);
		addIndex(dbRowMap, noAnalyzerFields);
	}
	
	/**
	 * 搜索
	 * @param fieldName		需要修改的索引文档的词条属性名称
	 * @param fieldValue	需要修改的索引文档的词条属性名称
	 * @param isHighlight	是否对搜索结果关键字进行高亮显示处理；true:高亮显示；false:不高亮显示
	 * @return
	 */
	public List<Map<String, Object>> search(String fieldName, String fieldValue, boolean isHighlight) {
		
		List<Map<String, Object>> resultEntrys = new ArrayList<Map<String, Object>>();
		IndexSearcher searcher = null;
		try {
			manager.maybeRefresh();	//更新看看内存中索引是否有变化如果，有一个更新了，其他线程也会更新
			searcher = manager.acquire();	//利用acquire 方法获取search，执行此方法前须执行maybeRefresh
			
			Term term = new Term(fieldName, fieldValue);
			Query query = new TermQuery(term);
			TopDocs topDocs = searcher.search(query, 10);
			
			logger.info(">>>>>>搜索到"+ topDocs.totalHits +"条记录");
			
			List<Document> documents = new ArrayList<Document>();
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			for (ScoreDoc scoreDoc : scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				documents.add(doc);
			}
			if(isHighlight) {
				Analyzer analyzer = new SmartChineseAnalyzer();
				resultEntrys = handleSearchResultHighlight(documents, query, analyzer);
			} else {
				resultEntrys = handleSearchResult(documents);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(searcher != null) {
				try {
					manager.release(searcher);	//释放searcher
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return resultEntrys;
	}
	
	/**
	 * 处理搜索到的文档
	 * @param documents	文档集合
	 * @return
	 */
	public List<Map<String, Object>> handleSearchResult(List<Document> documents) throws Exception {
		
		List<Map<String, Object>> resultEntrys = new ArrayList<Map<String, Object>>();
		for (Document doc : documents) {
			Map<String, Object> resultEntryMap = new HashMap<String, Object>();
			List<IndexableField> fields = doc.getFields();
			for (IndexableField field : fields) {
				String _fieldName = field.name();
				String _fieldValue = doc.get(_fieldName);
				
				resultEntryMap.put(_fieldName, _fieldValue);
			}
			resultEntrys.add(resultEntryMap);
		}
		return resultEntrys;
	}
	
	/**
	 * 处理搜索到的文档，并对搜索结果进行高亮显示处理
	 * @param documents	文档集合
	 * @param query
	 * @param analyzer	若未传分词器，则默认使用lucene自带的中文分词器{@link SmartChineseAnalyzer}
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> handleSearchResultHighlight(List<Document> documents, Query query, Analyzer analyzer) throws Exception {
		
		if(analyzer == null) {
			analyzer = new SmartChineseAnalyzer();
		}
		
		List<Map<String, Object>> resultEntrys = new ArrayList<Map<String, Object>>();
		for (Document doc : documents) {
			Map<String, Object> resultEntryMap = new HashMap<String, Object>();
			List<IndexableField> fields = doc.getFields();
			for (IndexableField field : fields) {
				String _fieldName = field.name();
				String _fieldValue = doc.get(_fieldName);
				
				String highlighterResult = highlightFormat(_fieldName, _fieldValue, query, analyzer);
				if(highlighterResult != null) {
					resultEntryMap.put(_fieldName, highlighterResult);
				} else {
					resultEntryMap.put(_fieldName, _fieldValue);
				}
				
			}
			resultEntrys.add(resultEntryMap);
		}
		return resultEntrys;
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
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span style=\"color:red;\">", "</span>");
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter, queryScorer);
		highlighter.setTextFragmenter(fragmenter);
		TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(content));
		String highlighterResult = highlighter.getBestFragment(tokenStream, content);
		return highlighterResult;
	}
	
	/**
	 * 根据数据库记录集合创建索引文档对象
	 * @param dbRow	一个
	 * @param noAnalyzerFields 不需要进行分词的属性名称
	 * @return
	 */
	public Document getDocument(Map<String, Object> dbRow, String...noAnalyzerFields) {
		List<Map<String, Object>> dbRows = new ArrayList<Map<String, Object>>();
		dbRows.add(dbRow);
		List<Document> documents = getDocuments(dbRows, noAnalyzerFields);
		Document doc = documents.get(0);
		return doc;
	}
	
	/**
	 * 根据数据库记录集合创建索引文档对象
	 * @param dbRows	一个含Map类型元素的List集合，每个Map对应数据库一条记录，map中的买个元素对应数据库中的一个字段（或对象的一个属性）
	 * @param noAnalyzerFields	不需要进行分词的属性名称
	 * @return
	 */
	public List<Document> getDocuments(List<Map<String, Object>> dbRows, String...noAnalyzerFields) {
		
		List<Document> docs = new ArrayList<Document>();
		for (Map<String, Object> rowMap : dbRows) {
			
			Document doc = new Document();
			Set<String> keys = rowMap.keySet();
			for (String key : keys) {
				
				Field field = null;
				Object value = rowMap.get(key);
				if (value != null) {
					field = getField(key, value, noAnalyzerFields);
		        }
				if(field != null) {
					doc.add(field);
				}
			}
			docs.add(doc);
		}
		return docs;
	}
	
	/**
	 * 创建域
	 * @param key	域名称
	 * @param value	域值
	 * @param noAnalyzerFields	不需要进行分词的属性名称
	 * @return
	 */
	public Field getField(String key, Object value, String...noAnalyzerFields) {
		Field field = null;

		FieldType type = new FieldType();
		if(noAnalyzerFields != null && noAnalyzerFields.length > 0) {
			for (String _field : noAnalyzerFields) {
				if(_field.equalsIgnoreCase(key)) {
					type.setTokenized(false);
				}
			}
		}
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		type.setStored(true);
		field = new Field(key, value.toString(), type);
		
		/*if (value instanceof java.lang.Character) {
        	
        	field = new TextField(key, value.toString(), Field.Store.YES);
        } else if (value instanceof java.lang.Boolean) {
        	
        	field = new StringField(key, value.toString(), Field.Store.YES);
        } else if (value instanceof java.lang.Integer) {
        	
            field = new IntField(key, (Integer) value, Field.Store.YES);
        } else if (value instanceof java.lang.Long) {
        	
        	field = new LongField(key, (Long) value, Field.Store.YES);
        } else if (value instanceof java.util.Date) {
        	
        	field = new LongField(key, ((java.util.Date) value).getTime(), Field.Store.YES);
        } else if (value instanceof java.sql.Date) {
        	
        	field = new LongField(key, ((java.sql.Date) value).getTime(), Field.Store.YES);
        } else if (value instanceof java.lang.String) {
        	
        	field = new TextField(key, value.toString(), Field.Store.YES);
        } else if (value instanceof java.lang.Double) {
        	
        	field = new DoubleField(key, (Double) value, Field.Store.YES);
        } else if (value instanceof java.lang.Byte) {
        	
        	//field = new DoubleField(key, (Byte) value, Field.Store.YES);
        } else if (value instanceof java.lang.Float) {
        	
        	field = new FloatField(key, (Float) value, Field.Store.YES);
        } else if (value instanceof java.lang.Short) {
        	
        	field = new IntField(key, (Short) value, Field.Store.YES);
        } else {
        	field = new StringField(key, value.toString(), Field.Store.YES);
        }*/
		return field;
	}
	
	/**
	 * 获取索引存储路径
	 * @return
	 */
	public static String getIndexDirPath() {
		String webappPath = FileUtils.getWebappPath();
		webappPath = webappPath.endsWith("/") ? webappPath : webappPath + "/";
		String indexDirPath = webappPath + "luceneData/luceneIndex";	//索引存放路径
		return indexDirPath;	//索引存放路径;
	}
	
}
