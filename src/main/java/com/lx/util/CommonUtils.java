package com.lx.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtilsBean;

import com.lx.complete.bean.ProductInfo;

/**
 * 公共工具类
 * @author lx
 *
 */
public class CommonUtils {
	
	public static void main(String[] args) throws Exception {
		ProductInfo product = new ProductInfo();
		product.setId(1);
		product.setName("西瓜");
		product.setContent("西瓜是一种水果");
		product.setPrice(2.5);
		product.setFlag(false);
		
		Map<String, Object> map = beanToMap(product);
		System.out.println(map);
		
		Map<String, Object> map2 = beanToMap2(product);
		System.out.println(map2);
	}
	
	/**
	 * 将javabean实体类转为map类型，然后返回一个map类型的值<br/>
	 * 	需要引用jar包：commons-beanutils-1.7.0.jar、commons-logging-1.1.1.jar
	 * @param obj
	 * @return
	 */
    public static Map<String, Object> beanToMap(Object obj) {
        Map<String, Object> params = new HashMap<String, Object>(0); 
        try { 
            PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean(); 
            PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(obj); 
            for (int i = 0; i < descriptors.length; i++) { 
                String name = descriptors[i].getName(); 
                if (!"class".equals(name)) { 
                    params.put(name, propertyUtilsBean.getNestedProperty(obj, name)); 
                } 
            } 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
        return params; 
    }
    
    /**
	 * 将javabean实体类转为map类型，然后返回一个map类型的值<br/>
	 * 
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> beanToMap2(Object object) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		Class<? extends Object> clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			
			String methodName = method.getName().toLowerCase();
			if(methodName.startsWith("get") || methodName.startsWith("is")) {
				for (Field field : fields) {
					
					String methodNameSubfix = "";	// 去掉 get或者is前缀后的方法名部分
					if(methodName.startsWith("get")) {
						methodNameSubfix = methodName.substring(3);
					} else if(methodName.startsWith("is")) {	//处理boolean类型的成员变量的方法
						methodNameSubfix = methodName.substring(2);
					}
					
					String fieldName = field.getName();
					if(methodNameSubfix.equalsIgnoreCase(fieldName.toLowerCase())) {
						Object objValue = method.invoke(object);
						map.put(fieldName, objValue);
					}
				}
			}
		}
		return map;
	}
	
	/**
	 * 将map集合转为一个指定类型的对象
	 * @param map
	 * @param object
	 */
	public static void mapToBean(Map<String, Object> map, Object object) {
		
	}
}
