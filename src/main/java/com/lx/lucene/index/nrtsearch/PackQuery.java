package com.lx.lucene.index.nrtsearch;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;

/**
 * 提供了lucene中的query对象的多种创建方法，如单词单域、单词多域、模糊查询、范围查询、距离查询等
 */
public class PackQuery {
	
	//分词器
	private Analyzer analyzer;
	
	//使用索引中的分词器
	public PackQuery(String indexName) {
		analyzer = NRTSearchManager.getNRTSearchManager(indexName).getAnalyzer();
	}
	
	//使用自定义分词器
	public PackQuery(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	/**
	 * 查询字符串匹配多个查询域
	 * @param key
	 * @param fields
	 * @return Query
	 * @throws ParseException
	 */
	public Query getMultiFieldQuery(String key, String[] fields) throws ParseException{
		MultiFieldQueryParser parse = new MultiFieldQueryParser(fields, analyzer);
		Query query = null;
		query = parse.parse(key);
		return query;
	}
	
	/**
	 * 查询字符串匹配单个查询域
	 * @param key
	 * @param field
	 * @return Query
	 * @throws ParseException
	 */
	public Query getOneFieldQuery(String key, String field) throws ParseException{
		if (key == null || key.length() < 1){
			return null;
		}
		QueryParser parse = new QueryParser(field, analyzer);
		Query query = null;
		query = parse.parse(key);
		return query;
	}
	
	/**
	 * 查询字符串、多个查询域以及查询域在查询语句中的关系
	 * @param key
	 * @param fields
	 * @param occur
	 * @return Query
	 * @throws IOException
	 */
	public Query getBooleanQuery(String key, String[] fields, Occur[] occur) throws IOException{
		if (fields.length != occur.length){
			System.out.println("fields.length isn't equals occur.length, please check params!");
			return null;
		}
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		TokenStream tokenStream = analyzer.tokenStream("", new StringReader(key));
		ArrayList<String> analyzerKeys = new ArrayList<String>();
		while(tokenStream.incrementToken()){
			CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
			analyzerKeys.add(term.toString());
		}
		for(int i = 0; i < fields.length; i++){
			BooleanQuery.Builder queryFieldBuilder = new BooleanQuery.Builder();
			for(String analyzerKey : analyzerKeys){
				TermQuery termQuery = new TermQuery(new Term(fields[i], analyzerKey));
				queryFieldBuilder.add(termQuery, Occur.SHOULD);
			}
			BooleanQuery queryField = queryFieldBuilder.build();
			queryBuilder.add(queryField, occur[i]);
		}
		BooleanQuery booleanQuery = queryBuilder.build();
		return booleanQuery;
	}
	
	/**
	 * 组合多个查询，之间的关系由occur确定
	 * @param querys
	 * @param occur
	 * @return Query
	 */
	public Query getBooleanQuery(List<Query> querys, List<Occur> occurs){
		if (querys.size() != occurs.size()){
			System.out.println("querys.size() isn't equals occurs.size(), please check params!");
			return null;
		}
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		for (int i = 0; i < querys.size(); i++){
			queryBuilder.add(querys.get(i), occurs.get(i));
		}
		BooleanQuery query = queryBuilder.build();
		return query;
	}
	
	/**
	 * StringField属性的搜索
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public Query getStringFieldQuery(String value, String fieldName){
		Query query = null;
		query = new TermQuery(new Term(fieldName, value));
		return query;
	}
	
	/**
	 * 多个StringField属性的搜索
	 * @param fields
	 * @param values
	 * @return
	 */
	public Query getStringFieldQuery(String[] values, String[] fields, Occur occur){
		if (fields == null || values == null || fields.length != values.length){
			return null;
		}
		ArrayList<Query> querys = new ArrayList<Query>();
		ArrayList<Occur> occurs = new ArrayList<Occur>();
		for (int i = 0; i < fields.length; i++){
			querys.add(getStringFieldQuery(values[i], fields[i]));
			occurs.add(occur);
		}
		return getBooleanQuery(querys, occurs);
	}
	
	/**
	 * key开头的查询字符串，和单个域匹配
	 * @param key
	 * @param field
	 */
	public Query getStartQuery(String key, String field) {
		if (key == null || key.length() < 1){
			return null;
		}
		Query query = new PrefixQuery(new Term(field, key));
		return  query;
	}
	
	/**
	 * key开头的查询字符串，和多个域匹配，每个域之间的关系由occur确定
	 * @param key
	 * @param fields
	 * @param occur
	 */
	public Query getStartQuery(String key, String []fields, Occur occur){
		if (key == null || key.length() < 1){
			return null;
		}
		List<Query> querys = new ArrayList<Query>();
		List<Occur> occurs = new ArrayList<Occur>(); 
		for (String field : fields) {
			querys.add(getStartQuery(key, field));
			occurs.add(occur);
		}
		return getBooleanQuery(querys, occurs);
	}
	
	/**
	 * key开头的查询字符串，和多个域匹配，每个域之间的关系Occur.SHOULD
	 * @param key
	 * @param fields
	 */
	public Query getStartQuery(String key, String []fields) {
		return getStartQuery(key, fields, Occur.SHOULD);
	}
	
	/**
	 * 自定每个词元之间的最大距离
	 * @param key
	 * @param field
	 * @param slop
	 * @return
	 */
	public Query getPhraseQuery(String key, String field, int slop) {
		if (key == null || key.length() < 1){
			return null;
		}
		StringReader reader = new StringReader(key);
		PhraseQuery.Builder phraseQueryBuilder = new PhraseQuery.Builder();
		phraseQueryBuilder.setSlop(slop);
		try {
			TokenStream  tokenStream  = this.analyzer.tokenStream(field, reader);
			tokenStream.reset();
			CharTermAttribute  term = tokenStream.getAttribute(CharTermAttribute.class);
			while(tokenStream.incrementToken()){  
				phraseQueryBuilder.add(new Term(field, term.toString()));
	        } 
			reader.close(); 
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		PhraseQuery query = phraseQueryBuilder.build();
		return query;
	}
	
	/**
	 * 自定每个词元之间的最大距离，查询多个域，每个域之间的关系由occur确定
	 * @param key
	 * @param fields
	 * @param slop
	 * @param occur
	 * @return
	 */
	public Query getPhraseQuery(String key, String[] fields, int slop, Occur occur) {
		if (key == null || key.length() < 1){
			return null;
		}
		ArrayList<Query> querys = new ArrayList<Query>();
		ArrayList<Occur> occurs = new ArrayList<Occur>(); 
		for (String field : fields) {
			querys.add(getPhraseQuery(key, field, slop));
			occurs.add(occur);
		}
		return getBooleanQuery(querys, occurs);
	}
	
	/**
	 * 自定每个词元之间的最大距离，查询多个域，每个域之间的关系是Occur.SHOULD
	 * @param key
	 * @param fields
	 * @param slop
	 * @return
	 */
	public Query getPhraseQuery(String key, String[] fields, int slop) {
		return getPhraseQuery(key, fields, slop, Occur.SHOULD);
	}
	
	/**
	 * 通配符检索 eg:getWildcardQuery("a*thor", "field")
	 * @param key
	 * @param field
	 * @return
	 */
	public Query getWildcardQuery(String key, String field) {
		if (key == null || key.length() < 1){
			return null;
		}
		return new WildcardQuery(new Term(field, key));
	}
	
	/**
	 * 通配符检索，域之间的关系为occur
	 * @param key
	 * @param fields
	 * @param occur
	 * @return
	 */
	public Query getWildcardQuery(String key, String[] fields, Occur occur) {
		if (key == null || key.length() < 1){
			return null;
		}
		ArrayList<Query> querys = new ArrayList<Query>();
		ArrayList<Occur> occurs = new ArrayList<Occur>(); 
		for (String field : fields) {
			querys.add(getWildcardQuery(key, field));
			occurs.add(occur);
		}
		return getBooleanQuery(querys, occurs);
	}
	
	/**
	 * 通配符检索，域之间的关系为Occur.SHOULD
	 * @param key
	 * @param fields
	 * @return
	 */
	public Query getWildcardQuery(String key, String[] fields) {
		return getWildcardQuery(key, fields, Occur.SHOULD);
	}
	
	/**
	 * 范围搜索
	 * @param keyStart
	 * @param keyEnd
	 * @param field
	 * @param includeStart
	 * @param includeEnd
	 * @return
	 */
	public Query getRangeQuery (String keyStart, String keyEnd, String field, boolean includeStart, boolean includeEnd) {
		return TermRangeQuery.newStringRange(field, keyStart, keyEnd, includeStart, includeEnd);
	}
	
	/**
	 * 范围搜索
	 * @param min
	 * @param max
	 * @param field
	 * @param includeMin
	 * @param includeMax
	 * @return
	 */
	public Query getRangeQuery (int min, int max, String field, boolean includeMin, boolean includeMax) {
		return NumericRangeQuery.newIntRange(field, min, max, includeMin, includeMax);
	}
	
	/**
	 * 范围搜索
	 * @param min
	 * @param max
	 * @param field
	 * @param includeMin
	 * @param includeMax
	 * @return
	 */
	public Query getRangeQuery (float min, float max, String field, boolean includeMin, boolean includeMax) {
		return NumericRangeQuery.newFloatRange(field, min, max, includeMin, includeMax);
	}
	
	/**
	 * 范围搜索
	 * @param min
	 * @param max
	 * @param field
	 * @param includeMin
	 * @param includeMax
	 * @return
	 */
	public Query getRangeQuery (double min, double max, String field, boolean includeMin, boolean includeMax) {
		return NumericRangeQuery.newDoubleRange(field, min, max, includeMin, includeMax);
	}
	
}
