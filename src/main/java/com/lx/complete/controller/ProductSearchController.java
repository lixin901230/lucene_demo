package com.lx.complete.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lx.complete.bean.ProductInfo;
import com.lx.complete.service.IProductSearchService;
import com.lx.lucene.util.LuceneManager;
import com.lx.util.CommonUtils;
import com.lx.util.HandleResultUtils;
import com.lx.util.JsonUtils;
import com.lx.util.UUIDTools;

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
	 * @param searchKeyWord 搜索关键词
	 * @param searchField	接收页面参数，搜索字段
	 * @param model	返回前端页面的数据
	 * @return
	 */
	@RequestMapping("/searchProducts.do")
	public String searchProducts(String searchField, String searchKeyWord, Model model) {
		
		List<ProductInfo> products = new ArrayList<ProductInfo>();
		List<Map<String, Object>> productInfos = new ArrayList<Map<String, Object>>();
		try {
			if(StringUtils.isNotEmpty(searchKeyWord)) {
				if(StringUtils.isEmpty(searchField)) {
					searchField = "content";
				}
				
				// 搜索时，去lucene中搜索，没有则再从数据库中搜索（lucene中没搜到，就不用） 
				productInfos = new LuceneManager().search(searchField, searchKeyWord, true);
				//productInfos = productSearchService.searchProducts(searchField, searchKeyWord);
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
			new LuceneManager().addIndexBatch(data, "id");
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
			product.setId(UUIDTools.getUUID());
			boolean success = productSearchService.addProductInfo(product);
			if(success) {
				new LuceneManager().addIndex(product, "id");
				result = HandleResultUtils.getResultMap(true, "添加产品信息成功");
			}
		} catch (Exception e) {
			result = HandleResultUtils.getResultMap(false, "添加产品信息失败，原因："+e);
			e.printStackTrace();
		}
		String jsonStr = JsonUtils.objToJsonStr(result);
		HandleResultUtils.writeJsonStr(response, jsonStr);
	}
	
	@RequestMapping("/toUpdateProduct")
	public String toUpdateProduct(String productId, Model model) {
		try {
			ProductInfo productInfo = productSearchService.queryProductInfoByIdForDB(productId);
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
//	@ResponseBody
	@RequestMapping("/updateProductById.do")
	public void updateProduct(ProductInfo product) {
		Map<String, Object> result = null;
		try {
			boolean success = productSearchService.updateProductInfoById(product);
			if(success) {
				String fieldName = "id";
				Map<String, Object> map = CommonUtils.beanToMap(product);
				new LuceneManager().updateIndex(fieldName, product.getId(), map, "id");
				
				result = HandleResultUtils.getResultMap(true, "修改产品信息成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = HandleResultUtils.getResultMap(false, "修改产品信息失败，原因："+e);
		}
		HttpServletResponse response = getResponse();
		String jsonStr = JsonUtils.objToJsonStr(result);
		HandleResultUtils.writeJsonStr(response, jsonStr);
		//return result;
	}
	
	/**
	 * 删除数据库产品信息时，同时删除lucene索引库中对于的索引文档数据
	 * @param request
	 * @param response
	 */
	@RequestMapping("/deleteProductById.do")
	public void deleteProduct(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = null;
		try {
			String productId = request.getParameter("productId");
			boolean success = productSearchService.deleteProductInfoById(productId);
			if(success) {
				String fieldName = "id";
				new LuceneManager().deleteIndex(fieldName, productId);
				result = HandleResultUtils.getResultMap(true, "删除产品信息成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = HandleResultUtils.getResultMap(false, "删除产品信息失败，原因："+e);
		}
		String jsonStr = JsonUtils.objToJsonStr(result);
		HandleResultUtils.writeJsonStr(response, jsonStr);
	}
}
