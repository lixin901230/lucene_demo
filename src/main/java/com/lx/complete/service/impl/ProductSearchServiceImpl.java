package com.lx.complete.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.lx.complete.bean.ProductInfo;
import com.lx.complete.dao.IProductSearchDao;
import com.lx.complete.service.IProductSearchService;

@Service
public class ProductSearchServiceImpl implements IProductSearchService {
	
	public IProductSearchDao productSearchDao;
	
	@Override
	public boolean addProductInfo(ProductInfo productInfo) throws Exception {
		
		// 1、向数据库插入记录
		boolean success = productSearchDao.addProductInfo(productInfo);
		
		// 2、更新lucene索引文件
		
		
		return success;
	}
	
	/**
	 * 全文搜索（使用lucene进行搜索）
	 */
	@Override
	public List<ProductInfo> searchProducts(Map<String, Object> params) throws Exception {

		List<ProductInfo> productInfos = new ArrayList<ProductInfo>();
		
		// 搜索时，去lucene中搜索，没有则再从数据库中搜索（lucene中没搜到，就不用） 
		
		// 将搜索到的记录解析成一个个ProductInfo对象并放入到productInfos集合中返回到控制层
		
		return productInfos;
	}

}
