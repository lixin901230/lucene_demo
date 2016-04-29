package com.lx.complete.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.lx.complete.bean.ProductInfo;
import com.lx.complete.dao.IProductSearchDao;

@Component
public class ProductSearchDaoImpl implements IProductSearchDao {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;	//具名参数查询对象
		
	@Override
	public boolean addProductInfo(ProductInfo productInfo) throws Exception {
		
		boolean success = false;
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(productInfo);
		String sql = "INSERT INTO product_info(name, content, price) VALUES(:name, :content, :price)";
		int flag = namedParameterJdbcTemplate.update(sql, paramSource);
		if(flag > -1) {
			success = true;
		}
		return success;
	}
	
	@Override
	public boolean updateProductInfoById(ProductInfo productInfo) throws Exception {
		
		boolean success = false;
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(productInfo);
		String sql = "UPDATE product_info SET name=:name, content=:content, price=:price WHERE id=:id";
		int flag = namedParameterJdbcTemplate.update(sql, paramSource);
		if(flag > -1) {
			success = true;
		}
		return success;
	}
	
	public boolean deleteProductInfoById(String productId) {
		boolean success = false;
		try {
			String sql = "DELETE FROM product_info WHERE id='"+productId+"'";
			jdbcTemplate.execute(sql);
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}
	
	@Override
	public List<ProductInfo> queryProductInfos(Map<String, Object> params) throws Exception {
		 
		Object id = params.get("id");
		Object name = params.get("name");
		Object content = params.get("content");
		Object price = params.get("price");
		
		List<Object> paramList = new ArrayList<Object>();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM product_info d WHERE and 1=1");
		if(id != null) {
			sql.append(" and d.id=? ");
			paramList.add(id);
		}
		if(name != null) {
			sql.append(" and d.name=? ");
			paramList.add(paramList);
		}
		if(content != null) {
			sql.append(" and d.content=? ");
			paramList.add(content);
		}
		if(price != null) {
			sql.append(" and d.price<=? ");
			paramList.add(price);
		}
		
		RowMapper<ProductInfo> rowMapper = new BeanPropertyRowMapper<ProductInfo>(ProductInfo.class);
		List<ProductInfo> list = jdbcTemplate.query(sql.toString(), rowMapper, paramList.toArray());
		return list;
	}

}
