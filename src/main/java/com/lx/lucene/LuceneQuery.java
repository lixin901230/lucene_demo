package com.lx.lucene;

import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;

/**
 * Lucene 常用 Query 对象使用示例
 * @author lx
 */
public class LuceneQuery {
	
	public static void main(String[] args) {
		
		String key = "我的lucene query 对象示例";
		
		Query query = null;
		
		// 1、QueryParser
		
		// 2、MultiFieldQueryParser
		
		// 3、TermQuery
		
		// 4、PrefixQuery
		
		// 5、PhraseQuery
		
		// 6、WildcardQuery
		
		// 7、TermRangeQuery
		
		// 8、NumericRangeQuery
		
		// 9、BooleanQuery
		
		
	}
}
