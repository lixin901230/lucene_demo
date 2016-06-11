package com.lx.lucene.index.nrtsearch;  

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
  
/**
 * 索引管理类配置信息<br/>
 * 生产环境中，可通过采用Spring注入配置进行配置信息的配置 或 使用配置文件配置并初始化 
 */
public class ConfigBean {
	
	private String indexName;//索引名
	private String indexPath;//索引硬盘路径
//	private Analyzer analyzer = new StandardAnalyzer();//索引分词器
	private Analyzer analyzer = new SmartChineseAnalyzer();	//支持中文
	private double indexReopenMaxStaleSec = 5.0;	//在0.025s~5.0s之间重启一次线程，这个是时间的最佳实践
	private double indexReopenMinStaleSec = 0.025;	//在0.025s~5.0s之间重启一次线程，这个是时间的最佳实践
	private int indexCommitSeconds = 15;//索引写入磁盘时间间隔（秒）
	
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public String getIndexPath() {
		return indexPath;
	}
	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}
	public Analyzer getAnalyzer() {
		return analyzer;
	}
	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}
	public double getIndexReopenMaxStaleSec() {
		return indexReopenMaxStaleSec;
	}
	public void setIndexReopenMaxStaleSec(double indexReopenMaxStaleSec) {
		this.indexReopenMaxStaleSec = indexReopenMaxStaleSec;
	}
	public double getIndexReopenMinStaleSec() {
		return indexReopenMinStaleSec;
	}
	public void setIndexReopenMinStaleSec(double indexReopenMinStaleSec) {
		this.indexReopenMinStaleSec = indexReopenMinStaleSec;
	}
	public int getIndexCommitSeconds() {
		return indexCommitSeconds;
	}
	public void setIndexCommitSeconds(int indexCommitSeconds) {
		this.indexCommitSeconds = indexCommitSeconds;
	}
}
