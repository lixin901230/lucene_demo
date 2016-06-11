package com.lx.lucene.index.nrtsearch;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lx.util.CommonUtils;

/**
 * 近实时搜索管理类
 * 
 * @author lx
 */
public class NRTSearchManager {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private IndexWriter indexWriter;
	/* 
	 * 更新索引文件的IndexWriter:
	 * 用TrackingIndexWriter类来包装IndexWriter，这样IndexWriter的索引操作委派给TrackingIndexWriter提供的索引添删改API来操作索引，
	 * 这样可以在IndexWriter未commit提交的情况下也能在ControlledRealTimeReopenThread中及时反应出来索引的变更，
	 * 这样在索引变更但未提交的情况下， 就能实现近实时搜索（之所以这样处理，因为IndexWriter的commit提交操作非常消耗资源，
	 * 所以在生产环境中应该想一个比较好的索引更新提交策略；
	 * 比如：1、定时提交，通过定时任务每隔一段时间后commit一下内存中变更的索引文档；2、定时合并内存索引与硬盘索引）
	 */
	private TrackingIndexWriter trackingIndexWriter;
	//索引重读线程
	private ControlledRealTimeReopenThread<IndexSearcher> controlledRealTimeReopenThread;
	//索引写入磁盘线程
	private SearcherManager searcherManager;
	
	private ConfigBean configBean;
	private IndexCommitThread indexCommitThread;
	
	private Analyzer analyzer;
	
	private static class LazyIndexManager {
		//保存系统中的IndexManager对象
		private static HashMap<String, NRTSearchManager> indexManagerMap = new HashMap<String, NRTSearchManager>();
		static {
			for (ConfigBean bean : IndexConfig.getConfig()) {
				indexManagerMap.put(bean.getIndexName(), new NRTSearchManager(bean));
			}
		}
	}
	
	/**
	 * 获取索引的IndexManager对象
	 * @param indexName	索引文件名称
	 * @return NRTSearchManager
	 */
	public static NRTSearchManager getIndexManager(String indexName) {
		return LazyIndexManager.indexManagerMap.get(indexName);
	}
	
	/**
	 * 使用单例模式：<br/>
	 * 加载索引是一个相当消耗资源的事情，所以我们不可能每一次索引操作都加载一次索引，所以我们就必须使用单例模式来实现IndexManager类。
	 * 这里的单例模式又和我们常见的单例模式有所区别，普通的单例模式该类只有一个对象，这里的单例模式是该类有多个对象，下面就简单的介绍下此处另类的单例模式。
	 * 系统中关于索引的配置信息是存在HashSet对象中，这也就是说这里IndexManager类会实例化多少次取决于HashSet对象，
	 * 也就是你配置文件让他实例化多少次就会实例化多少次。既然这样，怎么还能叫单例模式呢？这里的单例是索引的单例，
	 * 也就是说一个索引只有一个IndexManager对象，不会存在两个IndexManager对象去操作同一个索引的情况
	 */
	private NRTSearchManager(ConfigBean configBean) {
		try {
			this.configBean = configBean;
			this.analyzer = configBean.getAnalyzer();
			
			String indexPath = configBean.getIndexPath();//  + "/" + configBean.getIndexName();
			
			IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
			writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
			
			Directory directory = FSDirectory.open(Paths.get(indexPath));
			indexWriter = new IndexWriter(directory, writerConfig);
			trackingIndexWriter = new TrackingIndexWriter(indexWriter);	//将indexWriter委托给trackingIndexWriter
			searcherManager = new SearcherManager(directory, new SearcherFactory());	//true 表示在内存中删除，false可能删可能不删，设为false性能会更好一些  
			
			//开启守护线程
			startDaemonThread();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置indexSearcher的守护线程
	 */
	private void startDaemonThread() {
		//内存索引重读线程（即启动SearcherManager的maybeRefresh()线程，继续索引重读，索引重新打开线程在0.025s~5.0s之间重启一次线程，这个是时间的最佳实践）
		this.controlledRealTimeReopenThread = new ControlledRealTimeReopenThread<IndexSearcher>(trackingIndexWriter, searcherManager, configBean.getIndexReopenMaxStaleSec(), configBean.getIndexReopenMinStaleSec());
		this.controlledRealTimeReopenThread.setDaemon(true);	//设置indexSearcher的守护线程
		this.controlledRealTimeReopenThread.setName("Controlled Real Time Reopen Thread");
		this.controlledRealTimeReopenThread.start();
		
		//内存索引提交线程
		this.indexCommitThread = new IndexCommitThread(configBean.getIndexName() + " index commmit thread");
		this.indexCommitThread.setDaemon(true);
		this.indexCommitThread.start();
	}
	
	/**
	 * 内存索引数据提交线程
	 */
	private class IndexCommitThread extends Thread {
		
		private boolean flag = false;
		public IndexCommitThread (String name) {
			super(name);
		}
		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			flag = true;
			while (flag){
				try {
					//内存索引提交至硬盘
					indexWriter.commit();
					System.out.println(new Date().toLocaleString() + "\t" + configBean.getIndexName() + "\tcommit");
					TimeUnit.SECONDS.sleep(configBean.getIndexCommitSeconds());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			super.run();
		}
	}
	
	/**
	 * 获取最新可用的indexSearcher
	 * @return IndexSearcher
	 */
	public IndexSearcher getIndexSearcher() {
		try {
			return this.searcherManager.acquire();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取索引中的记录条数 
	 */
	public int getIndexNum(){
		return indexWriter.numDocs();
	}
	
	/**
	 * 释放indexSearcher
	 * @param indexSearcher
	 */
	public void release(IndexSearcher indexSearcher) {
		try {
			this.searcherManager.release(indexSearcher);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public TrackingIndexWriter getTrackingIndexWriter() {
		return trackingIndexWriter;
	}
	public IndexWriter getIndexWriter() {
		return indexWriter;
	}
	public Analyzer getAnalyzer() {
		return analyzer;
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
			trackingIndexWriter.addDocument(doc);
			
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
			searcherManager.maybeRefresh();	//更新看看内存中索引是否有变化如果，有一个更新了，其他线程也会更新
			searcher = searcherManager.acquire();	//利用acquire 方法获取search，执行此方法前须执行maybeRefresh
			
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
					searcherManager.release(searcher);	//释放searcher
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
}
