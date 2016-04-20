package com.lx.lucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;


/**
 * Lucene搜索方式大合集
 *
 *	• TermQuery 
	• BooleanQuery 
	• WildcardQuery 
	• PhraseQuery 
	• MultiPhraseQuery 
	• PrefixQuery 
	• FuzzyQuery 
	• RegexpQuery 
	• TermRangeQuery 
	• NumericRangeQuery 
	• ConstantScoreQuery 
	• DisjunctionMaxQuery 
	• MatchAllDocsQuery 
	
 * @author lx
 */
public class LuceneQueryTest {
	
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
	 * 初始化搜索器（工具方法）
	 * @return
	 * @throws IOException 
	 */
	public static IndexSearcher getIndexSearcher() throws IOException {
		
		String indexDir = getIndexDirPath();
		Directory directory = FSDirectory.open(Paths.get(indexDir));
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		return searcher;
	}
	
	/**
	 * 1、词条搜索（单个关键词查找）<br/><br/>
	 * 
	 * 主要对象是TermQuery，调用方式如下：<br/>
     * Term term=new Term(字段名, 搜索关键字);<br/>
     * Query query=new TermQuery(term);<br/>
     * TopDocs topDocs = searcher.search(query, 1000);<br/>
     * 
	 * @throws IOException 
	 */
	@Test
	public void termQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
		String searchKey = "优化";
		Term term = new Term("content", searchKey);
		Query query = new TermQuery(term);
		
		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
	/**
	 * 组合搜索(允许多个关键字组合搜索)<br/><br/>
     * 
     * 主要对象是BooleanQuery，调用方式如下：<br/>
     * Term term1=new Term(字段名, 搜索关键字);<br/>
     * TermQuery query1=new TermQuery(term1);<br/><br/>
     * 
     * Term term2=new Term(字段名, 搜索关键字);<br/>
     * TermQuery query2=new TermQuery(term2);<br/><br/>
     * 
     * Builder builder = new BooleanQuery.Builder();<br/>
     * builder.add(query1, 参数);<br/>
     * builder.add(query2, 参数);<br/>
     * BooleanQuery query = builder.build();<br/>
     * TopDocs topDocs = searcher.search(query, 1000);<br/><br/>
     * 
     * 此方法中的核心在BooleanQuery.Builder()的add方法上，其第二个参数有三个可选值，对应着逻辑上的与或非关系。<br/>
     * 参数如下：<br/>
     * 	BooleanClause.Occur.MUST：必须包含，类似于逻辑运算的与<br/>
     * 	BooleanClause.Occur.MUST_NOT：必须不包含，类似于逻辑运算的非<br/>
     * 	BooleanClause.Occur.SHOULD：可以包含，类似于逻辑运算的或<br/>
     * 这三者组合，妙用无穷。<br/>
     * 
	 * @throws Exception
	 */
	@Test
	public void booleanQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
		//组合条件：
		TermQuery termQuery01 = new TermQuery(new Term("content", "mysql"));
		TermQuery termQuery02 = new TermQuery(new Term("content", "优化"));
		
		Builder builder = new BooleanQuery.Builder();
		builder.add(termQuery01, BooleanClause.Occur.SHOULD);
		builder.add(termQuery02, BooleanClause.Occur.SHOULD);
		BooleanQuery query = builder.build();
		
		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
	
	/**
	 * 通配符搜索（顾名思义）<br/><br/>
	 * 
	 * 主要对象是：WildcardQuery，调用方式如下：<br/><br/>
     * 
     * Term term=new Term(字段名,搜索关键字+通配符);<br/>
     * WildcardQuery query=new WildcardQuery(term);<br/>
     * TopDocs topDocs = searcher.search(query, 1000);<br/><br/>
     * 
     * 其中的通配符分两种，即 * 和 ? <br/>
     * * 表示匹配多个任意字符<br/>
     * ? 表示匹配一个任意字符
	 * @throws Exception
	 */
	@Test
	public void wildcardQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
		Term term = new Term("content", "优?");
		//Term term = new Term("content", "李?");
		WildcardQuery query = new WildcardQuery(term);
		
		
		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
	/**
	 * 短语搜索（根据零碎的短语组合成新的词组进行搜索）<br/><br/>
	 * 
	 * 主要对象是PhraseQuery，调用方式如下：<br/>
     * Term term1=new Term(字段名, 搜索关键字);<br/>
     * Term term2=new Term(字段名, 搜索关键字);<br/><br/>
     * 
     * PhraseQuery query=new PhraseQuery();<br/>
     * query.setSlop(参数);<br/>
     * query.add(term1);<br/>
     * query.add(term2);<br/>
     * TopDocs topDocs = searcher.search(query, 1000);<br/><br/>
     * 其中setSlop的参数是设置两个关键字之间允许间隔的最大值。<br/>
	 * 
	 * @throws Exception
	 */
	@Test
	public void phraseQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
		PhraseQuery.Builder builder = new PhraseQuery.Builder();
		builder.add(new Term("content", "优化"));
		builder.add(new Term("content", "思路"));
		
//		builder.add(new Term("descContent", "lucene"));
//		builder.add(new Term("descContent", "学习"));
		builder.setSlop(10);		//设置两个搜索关键字之间允许间隔的最大值
		PhraseQuery query = builder.build();
		
		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
	/**
	 * 多短语搜索(先指定一个前缀关键字，然后其他的关键字加在此关键字之后，组成词语进行搜索)<br/><br/>
	 * 
	 * 主要对象是MultiPhraseQuery，调用方式如下：<br/>
     * 
     * Term term=new Term(字段名,前置关键字);<br/>
     * Term term1=new Term(字段名,搜索关键字);<br/>
     * Term term2=new Term(字段名,搜索关键字);<br/><br/>
     * 
     * MultiPhraseQuery query=new MultiPhraseQuery();<br/><br/>
     * 
     * query.add(term);<br/>
     * query.add(new Term[]{term1, term2});<br/><br/>
     * 
     * TopDocs topDocs = searcher.search(query, 1000);<br/>
     * 
	 * @throws Exception
	 */
	@Test
	public void multiPhraseQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		MultiPhraseQuery query = new MultiPhraseQuery();
//		Term term = new Term("content", "mysql");
		Term term1 = new Term("content", "优化");
		Term term2 = new Term("content", "思路");
//		Term term3 = new Term("descContent", "lucene");
//		Term term4 = new Term("descContent", "学习");
		
//		query.add(term);
		query.add(new Term[]{term1, term2} );
//		query.add(new Term[]{term3, term4} );
		
		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
	/**
	 * 前缀搜索(搜索起始位置符合要求的结果)<br/><br/>
	 * 
	 * 主要对象是PrefixQuery，调用方式如下：<br/>
     * Term term=new Term(字段名, 搜索关键字);<br/>
     * PrefixQuery query=new PrefixQuery(term);<br/>
     * TopDocs topDocs = searcher.search(query, 1000);<br/>
     * 
	 * @throws Exception
	 */
	@Test
	public void prefixQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
		Term prefixTerm = new Term("content", "优化");
		PrefixQuery query = new PrefixQuery(prefixTerm);
		
		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
	/**
	 * 模糊搜索(顾名思义)<br/><br/>
	 * 
	 * 主要对象是FuzzyQuery，调用方式如下：<br/><br/>
     *
     * Term term=new Term(字段名, 搜索关键字);<br/>
     * FuzzyQuery query=new FuzzyQuery(term, 参数);<br/>
     * TopDocs topDocs = searcher.search(query, 1000);<br/>
     * 此中的参数是表示模糊度
	 * 
	 * @throws Exception
	 */
	@Test
	public void fuzzyQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
		FuzzyQuery query = new FuzzyQuery(new Term("content", "思路"), 2);
		
		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
	/**
	 * 正则表达式搜索<br/><br/>
	 * 
	 * 主要对象是：RegexQuery，调用方式如下 <br/>
     * String regex = ".*"; <br/>
     * Term term = new Term (search_field_name, regex); <br/>
     * RegexQuery query = new RegexQuery (term); <br/>
     * TopDocs hits = searcher.search (query, 100); <br/>
	 * 
	 * @throws Exception
	 */
	@Test
	public void regexpQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
		String regex = "优";
		RegexpQuery query = new RegexpQuery(new Term("content", regex));
		
		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
	/**
	 * 词条范围搜索(允许搜索指定范围内的关键字结果)<br/><br/>
	 * 
	 * 主要对象是TermRangeQuery，调用方式如下：<br/>
     * TermRangeQuery query=new TermRangeQuery(字段名, 起始值, 终止值, 起始值是否包含边界, 终止值是否包含边界); <br/><br/>
     * TopDocs topDocs = searcher.search(query, 1000);<br/>
     * TermRangeQuery构造函数最后两个参数是Boolean类型的，表示是否包含边界 。<br/>
     * true 包含边界<br/>
     * false 不包含边界<br/>
	 * 
	 * @throws Exception
	 */
	@Test
	public void termRangeQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
		// 不能搜索到的term
		BytesRef lowerTerm2 = new BytesRef("游".getBytes());
		BytesRef upperTerm2 = new BytesRef("泳".getBytes());
		
		// 能搜索到的term
		BytesRef lowerTerm = new BytesRef("优".getBytes());
		BytesRef upperTerm = new BytesRef("思".getBytes());
		TermRangeQuery query = new TermRangeQuery("content", lowerTerm, upperTerm, true, true);
		
		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
	/**
	 * 数值范围搜索(允许搜索指定范围内的数字结果)
	 * 
	 * 主要对象是TermRangeQuery，调用方式如下：<br/>
     * TermRangeQuery query=new TermRangeQuery(字段名, 起始值, 终止值, 起始值是否包含边界, 终止值是否包含边界); <br/><br/>
     * TopDocs topDocs = searcher.search(query, 1000);<br/>
     * TermRangeQuery构造函数最后两个参数是Boolean类型的，表示是否包含边界 。<br/>
     * true 包含边界<br/>
     * false 不包含边界<br/>
	 * 
	 * @throws Exception
	 */
	@Test
	public void numericRangeQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
		NumericRangeQuery<Double> query = NumericRangeQuery.newDoubleRange("doubleContent", 26.5, 27.5, true, false);//从doubleContent中查找[26.5~27.5)之间的数组
		//NumericRangeQuery<Integer> query = NumericRangeQuery.newIntRange("doubleContent", 30, 31, true, true);//从intContent中查找[30~31]之间的数组
		
		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
	@Test
	public void constantScoreQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
		
//		ConstantScoreQuery query = new ConstantScoreQuery(query);
	}
	
	@Test
	public void disjunctionMaxQuery() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		
//		DisjunctionMaxQuery query = new DisjunctionMaxQuery(disjuncts, tieBreakerMultiplier);
	}
	
	@Test
	public void matchAllDocsQuery () throws Exception {
		IndexSearcher searcher = getIndexSearcher();
		
		MatchAllDocsQuery query = new MatchAllDocsQuery();

		TopDocs topDocs = searcher.search(query, 1000);
		System.out.println("共检索出 " + topDocs.totalHits + " 条记录");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			float score = scoreDoc.score; //相似度
			System.out.println("相似度："+score+"\n搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
		}
	}
	
}
