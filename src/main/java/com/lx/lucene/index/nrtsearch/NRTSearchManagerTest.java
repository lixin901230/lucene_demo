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
	
	@Test
	public void testInitNRTSearchManager() {
		
		HashSet<ConfigBean> set = new HashSet<ConfigBean>();
//		for (int i = 0; i < 4; i++) {
			ConfigBean bean = new ConfigBean();
			bean.setIndexPath(getIndexDirPath());
//			bean.setIndexName("test" + i);
			bean.setIndexName("luceneIndex");
			set.add(bean);
//		}
		IndexConfig.setConfig(set);
		NRTSearchManager nrtSearchManager = NRTSearchManager.getIndexManager("luceneIndex");
		System.out.println(nrtSearchManager);
		
		List<Map<String,Object>> list = nrtSearchManager.search("content", "大米", true);
		System.out.println(list);
	}
	
	@Test
	public void testSearch() {

		HashSet<ConfigBean> set = new HashSet<ConfigBean>();
//		for (int i = 0; i < 4; i++) {
			ConfigBean bean = new ConfigBean();
			bean.setIndexPath(getIndexDirPath());
//			bean.setIndexName("test" + i);
			bean.setIndexName("luceneIndex");
			set.add(bean);
//		}
		IndexConfig.setConfig(set);
		NRTSearchManager nrtSearchManager = NRTSearchManager.getIndexManager("luceneIndex");
		System.out.println(nrtSearchManager);
		
		List<Map<String,Object>> list = nrtSearchManager.search("content", "美国", true);
		System.out.println(list);
		
		String[] noAnalyzerFields = new String[]{"id", "number", "price"};	//指定不需要分词的属性字段
		
		ProductInfo product = new ProductInfo();
		product.setId(UUIDTools.getUUID());
		product.setName("地球");
		product.setContent("美国在地球七大洲中的北美洲");
		product.setPrice(2.0);
		product.setNumber(200);
		nrtSearchManager.addIndex(product, noAnalyzerFields);
		
		while(true) {
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			list = nrtSearchManager.search("content", "地图", true);
			System.out.println(list);
			
			if(list != null && list.size() > 0) {
				break;
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
		String indexDirPath = webappPath + "luceneData/luceneIndex/";	//索引存放路径
		return indexDirPath;	//索引存放路径;
	}
}
