package com.lx.lucene.index.nrtsearch;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.lx.complete.bean.ProductInfo;
import com.lx.lucene.index.manager.IndexManager;
import com.lx.util.FileUtils;
import com.lx.util.UUIDTools;

public class NRTSearchManagerTest {
	
	/**
	 * 多索引管理类实例化，根据传入的多个索引类配置对象分别初始化多个对应的近实时搜索索引管理对象
	 */
	@Test
	public void testInitMultiNRTSearchManager() {
		
		HashSet<ConfigBean> set = new HashSet<ConfigBean>();
		// 配置多个索引目录，初始化近实时索引管理类时，将对不同的索引文件创建各自的索引管理类
		for (int i = 0; i < 4; i++) {
			ConfigBean bean = new ConfigBean();
			bean.setIndexPath(getIndexDirPath());
			bean.setIndexName("test" + i);
			set.add(bean);
		}
		IndexConfig.setConfig(set);
		NRTSearchManager nrtSearchManager = NRTSearchManager.getNRTSearchManager("test0");
		System.out.println("索引管理类对象===="+nrtSearchManager);
	}
	
	/**
	 * 获取luceneIndex所有目录下索引文件的近实时搜索索引管理类对象，并进行搜索
	 */
	@Test
	public void testSearch() {
		
		HashSet<ConfigBean> set = new HashSet<ConfigBean>();
		ConfigBean bean = new ConfigBean();
		bean.setIndexPath(getIndexDirPath());
		bean.setIndexName("luceneIndex");
		set.add(bean);
		IndexConfig.setConfig(set);
		NRTSearchManager nrtSearchManager = NRTSearchManager.getNRTSearchManager("luceneIndex");
		System.out.println(nrtSearchManager);
		
		List<Map<String,Object>> list = nrtSearchManager.search("content", "美国", true);
		System.out.println(list);
	}
	
	/**
	 * 添加索引——近实时搜索测试
	 */
	@Test
	public void testAddIndexNRTSearch() {

		HashSet<ConfigBean> set = new HashSet<ConfigBean>();
		ConfigBean bean = new ConfigBean();
		bean.setIndexPath(getIndexDirPath());
		bean.setIndexName("luceneIndex");
		set.add(bean);
		
		IndexConfig.setConfig(set);
		NRTSearchManager nrtSearchManager = NRTSearchManager.getNRTSearchManager("luceneIndex");
		System.out.println(nrtSearchManager);
		
		// 第一次查询
		List<Map<String,Object>> list = nrtSearchManager.search("content", "美国", true);
		System.out.println(list);
		
		// 新增索引
		String[] noAnalyzerFields = new String[]{"id", "number", "price"};	//指定不需要分词的属性字段
		ProductInfo product = new ProductInfo(UUIDTools.getUUID(), "地球", "美国在地球七大洲中的北美洲", 200, 2.0);
		nrtSearchManager.addIndex(product, noAnalyzerFields);
		
		// 再次查询
		for(int i=0; i<15; i++) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			list = nrtSearchManager.search("content", "美国", true);
			System.out.println(list);
		}
	}

	/**
	 * 修改索引——近实时搜索测试
	 */
	@Test
	public void testUpdateIndexNRTSearch() {

		HashSet<ConfigBean> set = new HashSet<ConfigBean>();
		ConfigBean bean = new ConfigBean();
		bean.setIndexPath(getIndexDirPath());
		bean.setIndexName("luceneIndex");
		set.add(bean);
		
		IndexConfig.setConfig(set);
		NRTSearchManager nrtSearchManager = NRTSearchManager.getNRTSearchManager("luceneIndex");
		System.out.println(nrtSearchManager);
		
		// 第一次查询
		List<Map<String,Object>> list = nrtSearchManager.search("content", "美国", true);
		System.out.println(list);
		
		// 修改索引
		String[] noAnalyzerFields = new String[]{"id", "number", "price"};	//指定不需要分词的属性字段
		ProductInfo product = new ProductInfo("de9bab7fd99d4750a209a12bbb263c52", "地球", "美国是发达国家，美国在北美洲", 400, 4.0);
		nrtSearchManager.updateIndex("id", "de9bab7fd99d4750a209a12bbb263c52", product, noAnalyzerFields);	// 根据id属性去修改
		
		// 再次查询
		for(int i=0; i<15; i++) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			list = nrtSearchManager.search("content", "美国", true);
			System.out.println(list);
		}
	}
	
	/**
	 * 删除索引——近实时搜索测试
	 */
	@Test
	public void testDeleteIndexNRTSearch() {

		HashSet<ConfigBean> set = new HashSet<ConfigBean>();
		ConfigBean bean = new ConfigBean();
		bean.setIndexPath(getIndexDirPath());
		bean.setIndexName("luceneIndex");
		set.add(bean);
		
		IndexConfig.setConfig(set);
		NRTSearchManager nrtSearchManager = NRTSearchManager.getNRTSearchManager("luceneIndex");
		System.out.println(nrtSearchManager);
		
		// 第一次查询
		List<Map<String,Object>> list = nrtSearchManager.search("content", "美国", true);
		System.out.println(list);
		
		// 删除索引
		nrtSearchManager.deleteIndex("id", "27e95908cfe84456a066328e09e877d3");	//注意：根据id删除索引时，该id值来源：1）：自己查出要删除的索引词条文档，并找出要删除的文档的id属性值，然后再测试删除；2）：使用luke工具找到要删除的词条文档值的id，拷贝出来测试删除
		
		// 再次查询
		for(int i=0; i<15; i++) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			list = nrtSearchManager.search("content", "美国", true);
			System.out.println(list);
		}
	}
	
	/**
	 * 获取索引存储路径
	 * @return
	 */
	public static String getIndexDirPath() {
		String webappPath = FileUtils.getWebappPath();
		webappPath = webappPath.endsWith("/") ? webappPath : webappPath + "/";
		String indexDirPath = webappPath + "luceneData/luceneIndex/";	//索引存放路径
		return indexDirPath;	//索引存放路径;
	}
}
