package com.lx.lucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Lucene 搜索
 * @author lx
 */
public class LuceneSearch {
	
	public static void main(String[] args) {
		
		String classesPath = CreateIndexTable.class.getResource("/").getPath();
		classesPath = classesPath.startsWith("/") ? classesPath.substring(1) : classesPath;
		String webPath = classesPath.substring(0, classesPath.indexOf("WEB-INF"));
		
		String indexDir = webPath + "luceneData/luceneIndex";	//索引存放路径
		System.out.println("indexDir=="+ indexDir);
		
		String key = "优化";
		search(indexDir, key);
	}
	
	/**
	 * 根据创建的索引表 和 搜索关键字 进行搜索
	 * @param indexDir	索引表的位置
	 * @param key		查询关键字
	 */
	public static void search(String indexDir, String key) {
		
		DirectoryReader reader = null;
		try {
			
			// 1、创建索引表存储对象（索引存储在磁盘中）
			Directory directory = FSDirectory.open(Paths.get(indexDir));
			//Directory dir = new RAMDirectory();	// 创建索引表存储对象（索引存储在内存中）
			
			// 2、建立读入索引文件对象
			reader = DirectoryReader.open(directory);
			
			// 3、生成搜索器，将用它来操作索引
			IndexSearcher searcher = new IndexSearcher(reader);
			
			// 4、生成分词搜索规则（用什么规则生成的索引表，就用什么规则去搜索索引表）
			//Analyzer analyzer = new StandardAnalyzer();//通用标准分词规则
			Analyzer analyzer = new SmartChineseAnalyzer();	//中文分词规则（建立索引就用的这个）
			
			// 5、根据对索引表搜索的字段和分词器创建搜索转换器
			QueryParser parser = new QueryParser("content", analyzer);
			
			// 6、根据搜索关键字，获取搜索规则（底层实现算法，到此处还只是一些设置，还不是真正耗时的搜索工作）
			Query query = parser.parse(key);
			
			// 7、开始搜索工作，根据以上设置的搜索规则，搜索出前30条数据
			TopDocs results = searcher.search(query, 30);
			System.out.println("符合条件的文档总数："+results.totalHits);
			
			// 8、显示搜索结果
			ScoreDoc[] docs = results.scoreDocs;
			for (ScoreDoc score : docs) {
				//根据文件标识从搜索器里取得搜索到的文件
				Document doc = searcher.doc(score.doc);
				//显示搜索到的文件的路径和名字，通过索引表的字段获取
				System.out.println("搜索到的文件："+doc.get("fullPath")+"/"+doc.get("fileName"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					// 9、关闭读入文件流
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
