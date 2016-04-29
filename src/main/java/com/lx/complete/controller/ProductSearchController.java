package com.lx.complete.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lx.complete.bean.ProductInfo;
import com.lx.complete.service.IProductSearchService;
import com.lx.lucene.util.LuceneManager;
import com.lx.util.CommonUtils;


/**
 * 产品信息搜Controller
 * 
 * @author lx
 *
 */
@Controller
@RequestMapping("/productSearch")
public class ProductSearchController extends BaseController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private IProductSearchService productSearchService;
	
	/**
	 * 全文搜索符合条件的产品信息
	 * @param model
	 * @return
	 */
	@RequestMapping("/queryProductList.do")
	public String searchProducts(Model model) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> productInfos = new ArrayList<Map<String, Object>>();
		try {
			
			String searchField = "content";
			String searchKey = "橘子";
			
			// 搜索时，去lucene中搜索，没有则再从数据库中搜索（lucene中没搜到，就不用） 
			productInfos = new LuceneManager().search(searchField, searchKey, true);
			//productInfos = productSearchService.searchProducts(searchField, searchKey);
			
			result.put("success", true);
		} catch (Exception e) {
			logger.error(this.getClass().getName()+">>>searchProducts 执行异常，原因："+e);
			e.printStackTrace();
		}
		model.addAttribute("products", productInfos);
		return "/product/productList";
	}
	
	@RequestMapping("/addProduct.do")
	public void addProduct(ProductInfo product) {
		
		try {
			boolean success = productSearchService.addProductInfo(product);
			if(success) {
				new LuceneManager().addIndex(product);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/updateProductById.do")
	public void updateProduct(ProductInfo product) {
		try {
			boolean success = productSearchService.updateProductInfoById(product);
			if(success) {
				String fieldName = "id";
				String id = product.getId().toString();
				Map<String, Object> map = CommonUtils.beanToMap(product);
				new LuceneManager().updateIndex(fieldName, id, map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/deleteProductById.do")
	public void deleteProduct(HttpServletRequest request, HttpServletResponse response) {
		try {
			String productId = request.getParameter("productId");
			boolean success = productSearchService.deleteProductInfoById(productId);
			if(success) {
				String fieldName = "id";
				new LuceneManager().deleteIndex(fieldName, productId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
