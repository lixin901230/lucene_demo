package com.lx.complete.service;

import java.util.List;
import java.util.Map;

import com.lx.complete.bean.ProductInfo;

public interface IProductSearchService {
	
	/**
	 * 添加一条产品信息
	 * @param productInfo
	 * @return
	 * @throws Exception
	 */
	public boolean addProductInfo(ProductInfo productInfo) throws Exception;
	
	/**
	 * 修改一条产品信息
	 * @param productInfo
	 * @return
	 * @throws Exception
	 */
	public boolean updateProductInfoById(ProductInfo productInfo) throws Exception;
	
	/**
	 * 删除产品信息
	 * @param productId
	 * @return
	 */
	public boolean deleteProductInfoById(String productId);
	
	/**
	 * 查询产品信息（通过数据库查询）
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List<ProductInfo> queryProductInfosByDB(Map<String, Object> params) throws Exception;
	
	/**
	 * 全文搜索（使用lucene进行搜索）
	 * @param searchFieldName	需要搜索的属性名称
	 * @param searchKeyWord		搜索关键词
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchProducts(String searchFieldName, String searchKeyWord) throws Exception;
	
}
