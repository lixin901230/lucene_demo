package com.lx.complete.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lx.complete.bean.ProductInfo;
import com.lx.complete.service.IProductSearchService;


/**
 * 产品信息搜Controller
 * 
 * @author lx
 *
 */
@Controller
@RequestMapping("/productSearch")
public class ProductSearchController {
	
	@Autowired
	private IProductSearchService productSearchService;
	
	/**
	 * 全文搜索符合条件的产品信息
	 * @param model
	 * @return
	 */
	@RequestMapping("/queryProductList.do")
	public String searchProducts(Map<String, List<ProductInfo>> model) {
		
		List<ProductInfo> productInfos = new ArrayList<ProductInfo>();
		try {
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("content", "橘子");
			productInfos = productSearchService.searchProducts(params);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.put("products", productInfos);
		return "/product/productList";
	}
}
