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
import com.lx.util.HandleResultUtils;
import com.lx.util.JsonUtils;

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
	@RequestMapping("/searchProducts.do")
	public String searchProducts(ProductInfo product, Model model) {
		
		List<ProductInfo> products = new ArrayList<ProductInfo>();
		List<Map<String, Object>> productInfos = new ArrayList<Map<String, Object>>();
		try {
			if(product != null) {
				String searchField = "content";
				String searchKey = product != null ? product.getContent() : "";
				
				// 搜索时，去lucene中搜索，没有则再从数据库中搜索（lucene中没搜到，就不用） 
				productInfos = new LuceneManager().search(searchField, searchKey, true);
				//productInfos = productSearchService.searchProducts(searchField, searchKey);
			}
			
			// （此步可省略，直接返回List<Map<String, Object>>集合到前端取值显示）将List<Map<String, Object>>转换为List<ProductInfo>
			/*for (Map<String, Object> map : productInfos) {
				ProductInfo productTemp = (ProductInfo) CommonUtils.convertMapToBean(ProductInfo.class, map);
				//ProductInfo productTemp = (ProductInfo) CommonUtils.mapToBean(map, ProductInfo.class);
				products.add(productTemp);
			}*/
			
		} catch (Exception e) {
			logger.error(this.getClass().getName()+">>>searchProducts 执行异常，原因："+e);
			e.printStackTrace();
		}
		//model.addAttribute("products", products);
		model.addAttribute("products", productInfos);
		return "/page/product/productList";
	}
	
	/**
	 * 对整个数据库创建lucene索引库
	 */
	@RequestMapping("/createLuceneIndex.do")
	public void createLuceneIndexForDB(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Object> resultMap = null;
		try {
			// 获取数据库数据
			Map<String, Object> params = new HashMap<String, Object>();
			List<ProductInfo> productList = productSearchService.queryProductInfosByDB(params);
			
			// 对数据库数据创建lucene索引库
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			for (ProductInfo productInfo : productList) {
				Map<String, Object> map = CommonUtils.beanToMap(productInfo);
				data.add(map);
			}
			new LuceneManager().addIndexBatch(data);
			resultMap = HandleResultUtils.getResultMap(true, "lucene索引库创建成功");
		} catch (Exception e) {
			resultMap = HandleResultUtils.getResultMap(false, "lucene索引库创建失败，原因："+e);
			e.printStackTrace();
		}
		
		String jsonStr = JsonUtils.objToJsonStr(resultMap);
		HandleResultUtils.writeJsonStr(response, jsonStr);
	}
	
	/**
	 * 添加产品信息到数据库时，更新添加lucene索引库
	 * @param product
	 */
	@RequestMapping("/addProduct.do")
	public void addProduct(ProductInfo product, 
			HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Object> result = null;
		try {
			boolean success = productSearchService.addProductInfo(product);
			if(success) {
				new LuceneManager().addIndex(product);
				result = HandleResultUtils.getResultMap(true, "添加产品信息成功");
			}
		} catch (Exception e) {
			result = HandleResultUtils.getResultMap(true, "添加产品信息失败，原因："+e);
			e.printStackTrace();
		}
		String jsonStr = JsonUtils.objToJsonStr(result);
		HandleResultUtils.writeJsonStr(response, jsonStr);
	}
	
	@RequestMapping("/getProductById")
	public String getProductById(String id, Model model) {
		try {
			ProductInfo productInfo = productSearchService.queryProductInfoByIdForDB(id);
			model.addAttribute("product", productInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "/page/product/updateProduct";
	}
	
	/**
	 * 修改数据库产品信息时，同时更新lucene索引库
	 * @param product
	 */
	@RequestMapping("/updateProductById.do")
	public void updateProduct(ProductInfo product) {
		Map<String, Object> result = null;
		try {
			boolean success = productSearchService.updateProductInfoById(product);
			if(success) {
				String fieldName = "id";
				String id = product.getId().toString();
				Map<String, Object> map = CommonUtils.beanToMap(product);
				new LuceneManager().updateIndex(fieldName, id, map);
				
				result = HandleResultUtils.getResultMap(true, "修改产品信息成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = HandleResultUtils.getResultMap(true, "修改产品信息失败，原因："+e);
		}
		
		HttpServletResponse response = getResponse();
		String jsonStr = JsonUtils.objToJsonStr(result);
		HandleResultUtils.writeJsonStr(response, jsonStr);
	}
	
	/**
	 * 删除数据库产品信息时，同时删除lucene索引库中对于的索引文档数据
	 * @param request
	 * @param response
	 */
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
