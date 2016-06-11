package com.lx.lucene.index.nrtsearch;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.Query;

/**
 * 近实时索引操作类
 */
public class NRTIndex {
	
	private TrackingIndexWriter indexWriter;
	private String indexName;
	
	//直接使用NRTSearchManager中的indexWriter，将索引的修改操作委托给TrackingIndexWriter实现
	public NRTIndex(String indexName) {
		this.indexName = indexName;
		indexWriter = NRTSearchManager.getIndexManager(indexName).getTrackingIndexWriter();
	}
	
	/**
	 * 增加Document至索引
	 * @param doc
	 * @return boolean
	 */
	public boolean addDocument(Document doc){
		try {
			indexWriter.addDocument(doc);
			return true;
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 按照Query条件从索引中删除Document
	 * @param query
	 * @return boolean
	 */
	public boolean deleteDocument(Query query){
		try {
			indexWriter.deleteDocuments(query);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 清空索引
	 * @return
	 */
	public boolean deleteAll(){
		try {
			indexWriter.deleteAll();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 按照Term条件修改索引中Document
	 * @param term
	 * @param doc
	 * @return
	 */
	public boolean updateDocument(Term term, Document doc){
		try {
			indexWriter.updateDocument(term, doc);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 提交合并索引
	 * @throws Exception
	 */
	public void commit() throws Exception {
		NRTSearchManager.getIndexManager(indexName).getIndexWriter().commit();
	}
}
