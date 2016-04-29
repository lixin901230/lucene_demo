package com.lx.util;

import net.sf.json.JSONObject;

public class JsonUtils {

	public static String objToJsonStr(Object object) throws Exception {
		
		JSONObject jsonObject = JSONObject.fromObject(object);
		String json = jsonObject.toString();
		return json;
	}
}
