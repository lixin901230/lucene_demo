package com.lx.complete.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lx.complete.bean.ProductInfo;
import com.lx.complete.dao.IProductSearchDao;
import com.lx.complete.service.IProductSearchService;
import com.lx.lucene.util.LuceneManager;

@Service
public class ProductSearchServiceImpl implements IProductSearchService {
	
	@Autowired
	public IProductSearchDao productSearchDao;
	
	@Override
	public boolean addProductInfo(ProductInfo productInfo) throws Exception {
		boolean success = productSearchDao.addProductInfo(productInfo);
		return success;
	}
	
	@Override
	public boolean updateProductInfoById(ProductInfo productInfo) throws Exception {
		boolean success = productSearchDao.updateProductInfoById(productInfo);
		return success;
	}
	
	@Override
	public boolean deleteProductInfoById(String productId) {
		boolean success = productSearchDao.deleteProductInfoById(productId);
		return success;
	}
	
	@Override
	public ProductInfo queryProductInfoByIdForDB(String id) throws Exception {
		ProductInfo productInfo = productSearchDao.queryProductInfoByIdForDB(id);
		return productInfo;
	}

	@Override
	public List<ProductInfo> queryProductInfosByDB(Map<String, Object> params) throws Exception {
		List<ProductInfo> list = productSearchDao.queryProductInfosByDB(params);
		return list;
	}
	
	/**
	 * 全文搜索（使用lucene进行搜索）
	 */
	@Override
	public List<Map<String, Object>> searchProducts(String searchFieldName, String searchKeyWord) throws Exception {

		// 搜索时，去lucene中搜索，没有则再从数据库中搜索（lucene中没搜到，就不用） 
		List<Map<String, Object>> searchResults = new LuceneManager().search(searchFieldName, searchKeyWord, true);
		
		return searchResults;
	}

}
