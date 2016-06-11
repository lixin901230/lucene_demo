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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lx.complete.bean.ProductInfo;
import com.lx.complete.service.IProductSearchService;
import com.lx.lucene.index.manager.IndexManager;
import com.lx.lucene.index.manager.LuceneManager;
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
	
	private static IndexManager indexManager;
	static {
		indexManager = new IndexManager();
	}
	
	/**
	 * 全文搜索符合条件的产品信息
	 * @param searchKeyWord 搜索关键词
	 * @param searchField	接收页面参数，搜索字段
	 * @param model	返回前端页面的数据
	 * @return
	 */
	@RequestMapping("/searchProducts.do")
	public String searchProducts(@RequestParam String searchField, @RequestParam String searchKeyWord, Model model) {
		
		List<ProductInfo> products = new ArrayList<ProductInfo>();
		List<Map<String, Object>> productInfos = new ArrayList<Map<String, Object>>();
		try {
			if(StringUtils.isNotEmpty(searchKeyWord)) {
				if(StringUtils.isEmpty(searchField)) {
					searchField = "content";
				}
				
				// 搜索，去lucene中搜索，没有则再从数据库中搜索（lucene中没搜到，就不用）
				productInfos = new LuceneManager().search(searchField, searchKeyWord, true);
				
				// lucene近实时搜索
				//productInfos = indexManager.search(searchField, searchKeyWord, true);
				
				//数据库搜索
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
	@ResponseBody
	public Map<String, Object> createLuceneIndexForDB(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Object> result = null;
		try {
			
			// 一、新建索引库前，先将原有索引库删除
			new LuceneManager().deleteAllIndex();
			
			// 二、重新获取数据创建索引
			
			// 获取数据库数据
			Map<String, Object> params = new HashMap<String, Object>();
			List<ProductInfo> productList = productSearchService.queryProductInfosByDB(params);
			
			// 对数据库数据创建lucene索引库
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			for (ProductInfo productInfo : productList) {
				Map<String, Object> map = CommonUtils.beanToMap(productInfo);
				data.add(map);
			}
			
			String[] noAnalyzerFields = new String[]{"id", "number", "price"};	//指定不需要分词的属性字段
			new LuceneManager().addIndexBatch(data, noAnalyzerFields);
			result = HandleResultUtils.getResultMap(true, "lucene索引库创建成功");
		} catch (Exception e) {
			result = HandleResultUtils.getResultMap(false, "lucene索引库创建失败，原因："+e);
			e.printStackTrace();
		}
		
		//下面注释的json处理被替换：使用@ResponseBody根据springmvc中配置的jackson进行json转换并写入响应流
		/*String jsonStr = JsonUtils.objToJsonStr(result);
		HandleResultUtils.writeJsonStr(response, jsonStr);*/
		return result;
	}
	
	/**
	 * 添加产品信息到数据库时，更新添加lucene索引库
	 * @param product
	 */
	@RequestMapping("/addProduct.do")
	@ResponseBody
	public Map<String, Object> addProduct(ProductInfo product, 
			HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Object> result = null;
		try {
			product.setId(UUIDTools.getUUID());
			boolean success = productSearchService.addProductInfo(product);
			if(success) {
				String[] noAnalyzerFields = new String[]{"id", "number", "price"};	//指定不需要分词的属性字段
				
				// 方式1：直接使用{@link LuceneManager}中IndexWriter的api操作索引，性能低（因为都是实时commit的，commit很耗费资源）
				new LuceneManager().addIndex(product, noAnalyzerFields);
				
				// 方式2（推荐）：使用优化后的IndexManager的封装进行索引操作（会使用TrackingIndexWriter的api操作索引，操作后暂时不提交，详细见：{@link IndexManager}类说明）
				//indexManager.addIndex(product, noAnalyzerFields);
				
				result = HandleResultUtils.getResultMap(true, "添加产品信息成功");
			}
		} catch (Exception e) {
			result = HandleResultUtils.getResultMap(false, "添加产品信息失败，原因："+e);
			e.printStackTrace();
		}
		//下面注释的json处理被替换：使用@ResponseBody根据springmvc中配置的jackson进行json转换并写入响应流
		/*String jsonStr = JsonUtils.objToJsonStr(result);
		HandleResultUtils.writeJsonStr(response, jsonStr);*/
		return result;
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
	@RequestMapping("/updateProductById.do")
	@ResponseBody
	public Map<String, Object> updateProduct(ProductInfo product) {
		Map<String, Object> result = null;
		try {
			boolean success = productSearchService.updateProductInfoById(product);
			if(success) {
				String fieldName = "id";
				String[] noAnalyzerFields = new String[]{"id", "number", "price"};	//指定不需要分词的属性字段
				Map<String, Object> map = CommonUtils.beanToMap(product);
				new LuceneManager().updateIndex(fieldName, product.getId(), map, noAnalyzerFields);
				
				result = HandleResultUtils.getResultMap(true, "修改产品信息成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = HandleResultUtils.getResultMap(false, "修改产品信息失败，原因："+e);
		}
		
		//下面注释的json处理被替换：使用@ResponseBody根据springmvc中配置的jackson进行json转换并写入响应流
		/*HttpServletResponse response = getResponse();
		String jsonStr = JsonUtils.objToJsonStr(result);
		HandleResultUtils.writeJsonStr(response, jsonStr);*/
		return result;
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
