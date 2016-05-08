package com.lx.lucene.index.manager;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.lx.util.FileUtils;

/**
 * 索引管理<br/>
 * 近实时搜索<br/>
 * 
 * 使用{@link SearcherManager}实现原理：<br/>
 * 	只有Index Writer上的commit操作才会导致ram directory上的数据完全同步到文件。Index Writer提供了实时获得reader的API，
 * 这个调用将导致flush操作，生成新的segment，但不会commit（fsync），从而减少 了IO。新的segment被加入到新生成的reader里。
 * 从返回的reader里，可以看到更新。所以，只要每次新的搜索都从IndexWriter获得一个新的reader，就可以搜索到最新的内容。
 * 这一操作的开销仅仅是flush，相对commit来说，开销很小。Lucene的index组织方式为一个index目录下的多个segment。
 * 新的doc会加入新的segment里，这些新的小segment每隔一段时间就合并起来。因为合并，总的segment数量保持的较小，总体search速度仍然很快。
 * 为了防止读写冲突，lucene只创建新的segment，并在任何active的reader不在使用后删除掉老的segment。
 * flush是把数据写入到操作系统的缓冲区，只要缓冲区不满，就不会有硬盘操作。
 * commit是把所有内存缓冲区的数据写入到硬盘，是完全的硬盘操作。
 * 重量级操作。这是因为，Lucene索引中最主要的结构posting通过VINT和delta的格式存储并紧密排列。
 * 合并时要对同一个term的posting进行归并排序，是一个读出，合并再生成的过程
 * 
 * @author lx
 */
public class IndexManager {
	
	private SearcherManager manager;
	private IndexWriter writer;
	private TrackingIndexWriter tkWriter;
	private ControlledRealTimeReopenThread<IndexSearcher> crtThread;
	
	public IndexManager() {
		try {
			String indexPath = getIndexDirPath();
			Directory directory = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new SmartChineseAnalyzer();
			IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
			writer = new IndexWriter(directory, writerConfig);
			manager = new SearcherManager(directory, new SearcherFactory());	//true 表示在内存中删除，false可能删可能不删，设为false性能会更好一些  
			
			tkWriter = new TrackingIndexWriter(writer);	//为writer 包装了一层
			
			//ControlledRealTimeReopenThread，主要将writer装，每个方法都没有commit 操作。
			//内存索引重读线程
			crtThread = new ControlledRealTimeReopenThread<IndexSearcher>(tkWriter, manager, 5.0, 0.025);
			crtThread.setDaemon(true);	//设置indexSearcher的守护线程
			crtThread.setName("Controlled Real Time Reopen Thread");
			crtThread.start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 搜索
	 */
	public void search() {
		IndexSearcher searcher = null;
		try {
			manager.maybeRefresh();	//更新看看内存中索引是否有变化如果，有一个更新了，其他线程也会更新
			searcher = manager.acquire();	//利用acquire 方法获取search，执行此方法前须执行maybeRefresh
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(searcher != null) {
				try {
					manager.release(searcher);	//释放searcher
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 获取索引存储路径
	 * @return
	 */
	public static String getIndexDirPath() {
		String webappPath = FileUtils.getWebappPath();
		webappPath = webappPath.endsWith("/") ? webappPath : webappPath + "/";
		String indexDirPath = webappPath + "luceneData/luceneIndex";	//索引存放路径
		return indexDirPath;	//索引存放路径;
	}
}
