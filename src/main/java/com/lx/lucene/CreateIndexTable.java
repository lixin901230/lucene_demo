package com.lx.lucene;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;

/**
 * 创建索引
 * @author lx
 */
public class CreateIndexTable {
	
	/**
	 * 创建索引文件 测试
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			String contentDir = "C:/luceneData";
			String indexTableDir = "C:/luceneData/luceneIndex";
			createLuceneIndex(contentDir, indexTableDir);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 创建索引
	 * @param contentDir  等待创建索引的内容路径
	 * @param indexTableDir 索引表保存的路径
	 * @throws Exception 
	 */
	public static void createLuceneIndex(String contentDir,String indexTableDir) throws Exception{
		
		// 1、创建一个标准分词器，所谓分词器，就是在全内容中选择出关键字（不适用于中文，所以我们选择建议一个中文的分词器）
		//StandardAnalyzer analyzer = new StandardAnalyzer();
		SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
		
		// 2、创建索引分析器配置实例
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);	//索引的打开方式，没有就新建，有就打开
		
		// 3、读取内容，打开索引文件目录
		FSDirectory dir = FSDirectory.open(Paths.get(indexTableDir));
		
		// 4、索引表生成对象，可以看成是一个有规则的输出流对象
		IndexWriter writer = new IndexWriter(dir,config);
		
		// 5、读入等待创建索引的内容(因为是多个文件所以用listFiles，如果是只有一个文件 也不会错)
		File[] files = new File(contentDir).listFiles();
		for (File file : files) {
			
			// 6、循环创建等待分词的文档，并在后面将索引文档通过IndexWriter写入索引文件中，生成索引文件
			Document document = new Document();
			//可以理解为给索引表添加一个content字段，读内容为从file文件内容里提取的关键字，不保存文档内容进表
			document.add(new TextField("content", new FileReader(file)));
			//为索引表增加fileName字段，内容为文件名，保存文件名进索引表
			document.add(new StringField("fileName", file.getName(),Field.Store.YES));
			//为索引表增加fullPath字段，内容为文件全路径，保存文件路径到索引表
			document.add(new StringField("fullPath", file.getCanonicalPath(),Field.Store.YES));
			
			// 7、把定义好规则的文档写入索引表
			writer.addDocument(document);
			
			System.out.println("加载的文件 ："+file.getCanonicalPath()+"/"+file.getName());
		}
		//写完索引表，关闭写对象实例。
		writer.close();
	}	
}

