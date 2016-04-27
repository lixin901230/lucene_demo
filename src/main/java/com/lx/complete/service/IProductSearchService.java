package com.lx.complete.service;

import java.util.List;
import java.util.Map;

import com.lx.complete.bean.ProductInfo;

public interface IProductSearchService {
	
	public boolean addProductInfo(ProductInfo productInfo) throws Exception;
	
	/**
	 * 全文搜索（使用lucene进行搜索）
	 * @param params	搜索条件
	 * @return
	 * @throws Exception
	 */
	public List<ProductInfo> searchProducts(Map<String, Object> params) throws Exception;
	
}
