package com.lx.lucene.index.nrtsearch;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

/**
 * 近实时搜索
 */
public class NRTSearch {
	
	private NRTSearchManager nrtSearchManager;
	
	/**
	 * @param indexName 索引名
	 */
	public NRTSearch(String indexName) {
		nrtSearchManager = NRTSearchManager.getNRTSearchManager(indexName);
	}
	
	/**
	 * 索引中的记录数量
	 * @return
	 */
	public int getIndexNum() {
		return nrtSearchManager.getIndexNum();
	}
	
	/**
	 * @param query 查询字符串
	 * @param start 起始位置
	 * @param end 结束位置
	 * @return 查询结果
	 */
	public SearchResultBean search(Query query, int start, int end) {
		start = start < 0 ? 0 : start;
		end = end < 0 ? 0 : end;
		if (nrtSearchManager == null || query == null || start >= end) {
			return null;
		}
		SearchResultBean result = new SearchResultBean();
		List<Document> datas = new ArrayList<Document>();
		result.setDatas(datas);
		IndexSearcher searcher = nrtSearchManager.getIndexSearcher();
		try {
			TopDocs docs = searcher.search(query, end);
			result.setCount(docs.totalHits);
			end = end > docs.totalHits ? docs.totalHits : end;
			for (int i = start; i < end; i++) {
				datas.add(searcher.doc(docs.scoreDocs[i].doc));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			nrtSearchManager.release(searcher);
		}
		return result;
	}
	
	/**
	 * @param query 查询字符串
	 * @param start 起始位置
	 * @param end 结束位置
	 * @param sort 排序条件
	 * @return 查询结果
	 */
	public SearchResultBean search(Query query, int start, int end, Sort sort) {
		start = start < 0 ? 0 : start;
		end = end < 0 ? 0 : end;
		if (nrtSearchManager == null || query == null || start >= end) {
			return null;
		}
		SearchResultBean result = new SearchResultBean();
		List<Document> datas = new ArrayList<Document>();
		result.setDatas(datas);
		IndexSearcher searcher = nrtSearchManager.getIndexSearcher();
		try {
			TopDocs docs = searcher.search(query, end, sort);
			result.setCount(docs.totalHits);
			end = end > docs.totalHits ? docs.totalHits : end;
			for (int i = start; i < end; i++) {
				datas.add(searcher.doc(docs.scoreDocs[i].doc));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			nrtSearchManager.release(searcher);
		}
		return result;
	}
	
	/**
	 * 按序号检索
	 * @param start
	 * @param count
	 * @return
	 */
	public SearchResultBean search(int start, int count) {
		start = start < 0 ? 0 : start;
		count = count < 0 ? 0 : count;
		if (nrtSearchManager == null) {
			return null;
		}
		SearchResultBean result = new SearchResultBean();
		List<Document> datas = new ArrayList<Document>();
		result.setDatas(datas);
		IndexSearcher searcher = nrtSearchManager.getIndexSearcher();
		result.setCount(count);
		try {
			for (int i = 0; i < count; i++) {
				datas.add(searcher.doc((start + i) % getIndexNum()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			nrtSearchManager.release(searcher);
		}
		return result;
	}
}
