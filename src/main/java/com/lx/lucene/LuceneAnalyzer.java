package com.lx.lucene;

import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * lucene 常用分词器
 * @author lx
 */
public class LuceneAnalyzer {
	
	private static String str = "欢迎观看我的lucene学习demo，这是我的第1个lucene 分词器学习示例";
	
	public static void main(String[] args) {
		
		Analyzer analyzer = null;
		
		// 1、标准分词器
		analyzer = new StandardAnalyzer();
		print(analyzer);
		
		// 2、IK分词器
		analyzer = new IKAnalyzer();
		print(analyzer);
		
		// 3、空格分词器
		analyzer = new WhitespaceAnalyzer();
		print(analyzer);
		
		// 4、简单分词器
		analyzer = new SimpleAnalyzer();
		print(analyzer);
		
		// 5、二分法分词器
		analyzer = new CJKAnalyzer();
		print(analyzer);
		
		// 6、关键词分词器
		analyzer = new KeywordAnalyzer();
		print(analyzer);
		
		// 7、被忽略词分词器
		analyzer = new StopAnalyzer();
		print(analyzer);
	}
	
	/**
	 * 
	 */
	public static void print(Analyzer analyzer) {
		try {
			StringReader reader = new StringReader(str);
			TokenStream tokenStream = analyzer.tokenStream("", reader);
			tokenStream.reset();
			
			CharTermAttribute termAttr = tokenStream.getAttribute(CharTermAttribute.class);
			System.out.println("分词技术："+analyzer.getClass());
			while(tokenStream.incrementToken()) {
				System.out.print(termAttr.toString()+" | ");
			}
			System.out.println();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
