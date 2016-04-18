package com.lx.lucene;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import org.apache.lucene.store.Directory;
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
		
		String classesPath = CreateIndexTable.class.getResource("/").getPath();
		classesPath = classesPath.startsWith("/") ? classesPath.substring(1) : classesPath;
		String webPath = classesPath.substring(0, classesPath.indexOf("WEB-INF"));
		
		String dataDir = webPath + "luceneData";				//用于生产索引的文本内容文件路径
		String indexDir = webPath + "luceneData/luceneIndex";	//索引存放路径
		System.out.println("dataDir==="+ dataDir + "\nindexDir=="+ indexDir);
		
		createLuceneIndex(dataDir, indexDir);
	}
	
	/**
	 * 创建索引
	 * @param contentDir  等待创建索引的内容路径
	 * @param indexTableDir 索引表保存的路径
	 * @throws Exception 
	 */
	public static void createLuceneIndex(String dataDir, String indexDir) {
		
		IndexWriter writer = null;
		try {
			// 1、读取内容，打开索引文件目录
			Directory dir = FSDirectory.open(Paths.get(indexDir));

			// 2、创建一个标准分词器，所谓分词器，就是在全内容中选择出关键字（不适用于中文，所以我们选择建议一个中文的分词器）
			//StandardAnalyzer analyzer = new StandardAnalyzer();
			SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
			
			// 3、创建索引分析器配置实例
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setOpenMode(OpenMode.CREATE_OR_APPEND);	//索引的打开方式，没有就新建，有就打开追加
			
			// 4、索引表生成对象，可以看成是一个有规则的输出流对象
			writer = new IndexWriter(dir,config);
			
			// 5、读入等待创建索引的内容（因为是多个文件所以用listFiles，如果是只有一个文件 也不会错，注意：不能是文件夹）
			Document document = null;
			File[] files = new File(dataDir).listFiles();
			for (File file : files) {
				
				if(file.isFile()) {	//排除文件夹
					
					// 6、循环创建等待分词的文档，并在后面将索引文档通过IndexWriter写入索引文件中，生成索引文件
					document = new Document();
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
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(writer != null && writer.isOpen()) {
				try {
					// 8、写完索引表，关闭写对象实例。
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

