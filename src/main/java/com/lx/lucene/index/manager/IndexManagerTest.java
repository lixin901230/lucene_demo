package com.lx.lucene.index.manager;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.lx.complete.bean.ProductInfo;
import com.lx.util.UUIDTools;

public class IndexManagerTest {
	
	@Test
	public void testSearch() {

		IndexManager indexManager = new IndexManager();
		
		List<Map<String,Object>> list = indexManager.search("content", "地图", true);
		System.out.println(list);
		
		String[] noAnalyzerFields = new String[]{"id", "number", "price"};	//指定不需要分词的属性字段
		
		ProductInfo product = new ProductInfo();
		product.setId(UUIDTools.getUUID());
		product.setName("地图");
		product.setContent("世界地图");
		product.setPrice(2.0);
		product.setNumber(200);
		indexManager.addIndex(product, noAnalyzerFields);
		
		while(true) {
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			list = indexManager.search("content", "地图", true);
			System.out.println(list);
			
			if(list != null && list.size() > 0) {
				break;
			}
		}
	}
}
