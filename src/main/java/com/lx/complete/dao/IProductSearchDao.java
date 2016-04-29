package com.lx.complete.dao;

import java.util.List;
import java.util.Map;

import com.lx.complete.bean.ProductInfo;


public interface IProductSearchDao {
	
	/**
	 * 添加一条产品记录信息ProductInfo
	 * <p><b>使用具名参数jdbc模板对象执行插入操作，且参数以SqlParameterSource的实现类BeanPropertySqlParameterSource作为统一参数</b></p>
	 * 
	 * <p>使用具名参数jdbc模板对象时，可以使用 update(String sql, SqlParameterSource paramSource) 方法进行更新操作</p>
	 * <p>1. 使用SqlParameterSource做统一参数时，sql 语句中的占位符参数名必须 与 实体类的属性名要一致，见下实例sql参数占位符</p>
	 * <p>2. 使用 SqlParameterSource 的 BeanPropertySqlParameterSource 实现类作为统一参数</p>
	 * 
	 * @author lixin
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
	 * 删除一条产品信息
	 * @param productId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteProductInfoById(String productId);

	
	/**
	 * 查询查询信息（可用于定时维护lucene索引，如：每天凌晨查询数据库数据数据，更新lucene索引）
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List<ProductInfo> queryProductInfosByDB(Map<String, Object> params) throws Exception;
}
