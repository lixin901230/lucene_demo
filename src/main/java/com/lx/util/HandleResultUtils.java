package com.lx.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结果处理工具类
 * @author lx
 *
 */
public class HandleResultUtils {
	
	public static Map<String, Object> getResultMap(boolean success, String msg) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", success);
		result.put("msg", msg);
		
		return result;
	}
	
	public static Map<String, Object> getResultMap(boolean success, List<Object> data, String msg) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", success);
		result.put("data", data);
		result.put("msg", msg);
		
		return result;
	}
	
}
