package com.lx.lucene.index.manager;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
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
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
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
import com.lx.util.FileUtils;

/**
 * 索引管理类，使用 IndexWriter 和 IndexReader操作索引<br/><br/>
 * 
 * lucene 工具类，含索引的添、删、改、查 及其他索引文件操作
 * 
 * @author lx
 */
public class LuceneManager {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private volatile IndexWriter indexWriter = null;
	private volatile IndexReader indexReader = null;
	
	private Object lock_w = new Object();	// lock IndexWriter
	private Object lock_r = new Object();	// logck IndexReader
	
	/**
	 * 对多条记录批量创建索引<br/>
	 * 	注意：现在是将所有的数据库都存到一个索引文件中，若数据库数据量较大，可能造成索引文件较大，
	 * 		因此后期可以一个业务模块的相关表建一个单独的索引文件 或 对每个表建一个单独的索引文件 存放到不同的索引目录中，
	 * 		搜索时同时建立多个搜索任务从多个索引目录下的索引文件中搜索，有助于搜索性能优化
	 * 
	 * @param dbRows	需要创建索引的数据集合（从数据库中查出来的数据记录的map集合，每个map相当于一个实体对象）
	 * @param noAnalyzerFields	不需要进行分词的属性名称（如ID属性不需要进行分词，否则修改、删除时无法根据ID查找需要修改或删除的索引文档）
	 */
	public void addIndexBatch(List<Map<String, Object>> dbRows, String...noAnalyzerFields) {
		
		IndexWriter indexWriter = null;
		try {
			indexWriter = getIndexWriter();

			List<Document> docs = getDocuments(dbRows, noAnalyzerFields);
			
			indexWriter.addDocuments(docs);
			indexWriter.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeIndexWriter(indexWriter);
		}
	}
	
	/**
	 * 对单条记录数据创建索引<br/>
	 * <b>注意：该map中的元素只能是简单类型</b>
	 * @param dbRowMap	需要创建索引的数据（该map相当于数据库中查出来的一条记录或一个数据bean）
	 * @param noAnalyzerFields	不需要进行分词的属性名称（如ID属性不需要进行分词，否则修改、删除时无法根据ID查找需要修改或删除的索引文档）
	 */
	public void addIndex(Map<String, Object> dbRowMap, String...noAnalyzerFields) {
		
		IndexWriter indexWriter = null;
		try {
			indexWriter = getIndexWriter();

			Document doc = getDocument(dbRowMap, noAnalyzerFields);
			
			indexWriter.addDocument(doc);
			indexWriter.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeIndexWriter(indexWriter);
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
	 * 修改索引
	 * @param fieldName		需要修改的索引文档的词条属性名称，通常修改时根据记录的 id 去修改
	 * @param fieldValue	需要修改的索引文档的词条属性名称
	 * @param newData		需要更新的新数据map；<b>注意：该map中的元素只能是简单类型</b>
	 * @param noAnalyzerFields	不需要进行分词的属性名称（修改时，根据ID到索引库中查找需要修改的索引文档）
	 */
	public void updateIndex(String fieldName, String fieldValue, Map<String, Object> newData, String...noAnalyzerFields) {
		
		IndexWriter indexWriter = null;
		try {
			indexWriter = getIndexWriter();
			
			Term term = new Term(fieldName, fieldValue);
			
			List<Map<String, Object>> newDatas = new ArrayList<Map<String, Object>>();
			newDatas.add(newData);
			List<Document> docs = getDocuments(newDatas, noAnalyzerFields);
			indexWriter.updateDocuments(term, docs);	//Atomically deletes documents matching the provided delTerm and adds a block of documents with sequentially assigned document IDs, such that an external reader will see all or none of the documents.
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeIndexWriter(indexWriter);
		}
	}
	
	/**
	 * 修改索引<br/>
	 * 依赖{@link CommonUtils#beanToMap(Object)}
	 * @param fieldName		需要修改的索引文档的词条属性名称，通常修改时根据记录的 id 去修改
	 * @param fieldValue	需要修改的索引文档的词条属性名称
	 * @param newObject		需要更新的新对象
	 * @param noAnalyzerFields	不需要进行分词的属性名称（删除时，根据ID到索引库中查找需要删除的索引文档去删除）
	 */
	public void updateIndex(String fieldName, String fieldValue, Object newObject, String...noAnalyzerFields) {
		
		Map<String, Object> dbRowMap = CommonUtils.beanToMap(newObject);
		updateIndex(fieldName, fieldValue, dbRowMap, noAnalyzerFields);
	}
	
	/**
	 * 删除索引
	 * @param fieldName		需要修改的索引文档的词条属性名称
	 * @param fieldValue	需要修改的索引文档的词条属性名称
	 * 
	 */
	public void deleteIndex(String fieldName, String fieldValue) {
		IndexWriter indexWriter = null;
		try {
			indexWriter = getIndexWriter();
			
			Term term = new Term(fieldName, fieldValue);
			indexWriter.deleteDocuments(term);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeIndexWriter(indexWriter);
		}
	}
	
	/**
	 * 批量删除索引
	 * @param fieldAndValueMap	需要匹配删除的索引文档的词条属性名称及值，如：{"id1":"value1", "id2":"value2", "id3":"value3", ....}
	 */
	public void deleteIndexBatch(Map<String, String> fieldAndValueMap) {
		IndexWriter indexWriter = null;
		try {
			List<Term> terms = new ArrayList<Term>();
			
			indexWriter = getIndexWriter();
			for (String field : fieldAndValueMap.keySet()) {
				String value = fieldAndValueMap.get(field);
				Term term = new Term(field, value);
				terms.add(term);
			}
			Term[] termArr = terms.toArray(new Term[]{});
			indexWriter.deleteDocuments(termArr);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeIndexWriter(indexWriter);
		}
	}
	
	/**
	 * 删除全部
	 */
	public void deleteAllIndex() {
		IndexWriter indexWriter = null;
		try {
			
			indexWriter = getIndexWriter();
			indexWriter.deleteAll();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeIndexWriter(indexWriter);
		}
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
		try {
			
			IndexSearcher searcher = getIndexSearcher();
			
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
			closeIndexReader(indexReader);
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
	 
	/**
	 * 创建索引存储目录对象Directory<br/>
	 * 	若要对索引文件进行分开存储，则可根据业务数据将不同类型数据的索引分别存放在多个不同的目录中
	 * @return
	 * @throws URISyntaxException 
	 * @throws IOException
	 */
	public Directory createDirectory() throws Exception {
		String indexPath = getIndexDirPath();
		Directory directory = FSDirectory.open(Paths.get(indexPath));
		return directory;
	}
	
	/**
	 * 获取分词器
	 * @return
	 */
	public Analyzer getAnalyzer() {
		//Analyzer analyzer = new StandardAnalyzer();		// 1、标准分词器
		//IKAnalyzer analyzer = new IKAnalyzer();			// 2、IK分词器（第三方）
		Analyzer analyzer = new SmartChineseAnalyzer();		// 3、中文分词器
		return analyzer;
	}

	/**
	 * 索引文件操作对象<br/>
	 * 	注意：使用同步锁，获取IndexWriter，确保IndexWriter唯一（因为lucene只允许同时一个IndexWriter操作索引存储目录）
	 * @return
	 * @throws IOException
	 */
	public IndexWriter getIndexWriter() throws Exception {
		synchronized (lock_w) {
			if(indexWriter == null) {
				Directory directory = createDirectory();
				Analyzer analyzer = getAnalyzer();
				IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
				writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);	// Add new documents to an existing index
				indexWriter = new IndexWriter(directory, writerConfig);
			}
		}
		return indexWriter;
	}
	
	/**
	 * 索引文件读取对象<br/>
	 * 	注意：使用同步锁，获取IndexWriter，确保IndexWriter唯一（因为lucene只允许同时一个IndexWriter操作索引存储目录）
	 * @return
	 * @throws IOException
	 */
	public IndexReader getIndexReader() throws Exception {
		synchronized (lock_r) {
			if(indexReader == null) {
				Directory directory = createDirectory();
				indexReader = DirectoryReader.open(directory);
			}
		}
		return indexReader;
	}
	
	/**
	 * 初始化搜索器（工具方法）
	 * @return
	 * @throws Exception 
	 */
	public IndexSearcher getIndexSearcher() throws Exception {
		
		indexReader = getIndexReader();
		IndexSearcher searcher = new IndexSearcher(indexReader);
		return searcher;
	}
	
	/**
	 * 关闭IndexWriter资源
	 * @param indexWriter
	 */
	public void closeIndexWriter(IndexWriter indexWriter) {
		if(indexWriter != null) {
			try {
				indexWriter.close();
				indexWriter = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 关闭IndexReader资源
	 * @param indexReader
	 */
	public void closeIndexReader(IndexReader indexReader) {
		if(indexReader != null) {
			try {
				indexReader.close();
				indexReader = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 检查是否存在索引文件
	 * @return boolean true：已存在索引文件；false：不存在索引文件
	 */
	public boolean checkExistsIndex() {
		
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
