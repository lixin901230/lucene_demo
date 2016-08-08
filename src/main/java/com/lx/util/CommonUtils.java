package com.lx.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
		product.setId(UUIDTools.getUUID());
		product.setName("西瓜");
		product.setContent("西瓜是一种水果");
		product.setPrice(2.5);
		product.setFlag(true);
		
		Map<String, Object> map = beanToMap(product);
		System.out.println(map);
		
		Map<String, Object> map2 = beanToMap2(product);
		System.out.println(map2);
		
		
		Object object = convertMapToBean(ProductInfo.class, map);
		if(object instanceof ProductInfo) {
			ProductInfo productInfo = (ProductInfo) object;
			System.out.println(productInfo);
		}
		
		Object object2 = mapToBean(map, ProductInfo.class);
		if(object2 instanceof ProductInfo) {
			ProductInfo productInfo = (ProductInfo) object2;
			System.out.println(productInfo);
		}
	}

    /**
	 * 将javabean实体类转为map类型，然后返回一个map类型的值<br/>
	 * 
	 * @param object
	 * @return
	 * @throws Exception
	 */
	@Deprecated
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
     * 将一个 JavaBean 对象转化为一个  Map
     * @param bean 要转化的JavaBean 对象
     * @return 转化出来的  Map 对象
     * @throws IntrospectionException 如果分析类属性失败
	 * @throws IllegalArgumentException 
     * @throws IllegalAccessException 如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */ 
    public static Map<String, Object> convertBeanToMap(Object bean) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	Map<String, Object> returnMap = new HashMap<String, Object>();
        Class<?> type = bean.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        
        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
        for (int i = 0; i< propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if (!propertyName.equals("class")) {
                Method readMethod = descriptor.getReadMethod();
                Object result = readMethod.invoke(bean, new Object[0]);
                if (result != null) {
                    returnMap.put(propertyName, result);
                } else {
                    returnMap.put(propertyName, "");
                }
            }
        }
        return returnMap;
    }

	/**
     * 将一个 Map 对象转化为一个 JavaBean
     * @param type 要转化的类型
     * @param map 包含属性值的 map
     * @return 转化出来的 JavaBean 对象
     * @throws IntrospectionException 如果分析类属性失败
     * @throws IllegalAccessException 如果实例化 JavaBean 失败
     * @throws InstantiationException 如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */ 
    public static Object convertMapToBean(Class<?> type, Map<String, Object> map) throws InstantiationException, IllegalAccessException, IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(type); // 获取类属性
        Object obj = type.newInstance(); // 创建 JavaBean 对象
        
        // 给 JavaBean 对象的属性赋值
        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
        for (int i = 0; i< propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            
            if (map.containsKey(propertyName)) {
                // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
            	Object value = map.get(propertyName);
                Object[] args = new Object[1];
                if(value != null) {
	                args[0] = value;
	                try {
                		descriptor.getWriteMethod().invoke(obj, args);
	                } catch (Exception e) {
						e.printStackTrace();
					}
                }
            }
        }
        return obj;
    }
	
	/**
	 * 将map集合转为一个指定类型的对象
	 * @param map
	 * @param object
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
    @Deprecated
	public static Object mapToBean(Map<String, Object> map, Class<?> clazz) throws InstantiationException, IllegalAccessException {
		
		Object object = clazz.newInstance();
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields) {
			field.setAccessible(true);//设置允许访问
			String fieldName = field.getName();
			Class<?> type = field.getType();
			Object value = map.get(fieldName);
			
			String methodName = ""; 
			String nextMethodName = "get" + fieldName;
			if(type == boolean.class || type == null) {	//先检查是否为boolean类型
				methodName = "is" + fieldName;
			} else {
				methodName = nextMethodName;
			}
			
			Method readMethodName = findMethod(clazz, methodName, 0);
			if(readMethodName == null && !methodName.equals(nextMethodName)) {
				methodName = nextMethodName;
				readMethodName = findMethod(clazz, methodName, 0);
			}
			
			Method writeMethod = null;
			try {       
				Class<?> _type = findPropertyType(clazz, readMethodName, null);
			
				String writeMethodName = "set" + fieldName;
				Class<?>[] args = (_type == null) ? null : new Class[] { _type };
	            writeMethod = findMethod(clazz, writeMethodName, 1);
	            if (writeMethod != null) {
	                if (!writeMethod.getReturnType().equals(void.class)) {
	                    writeMethod = null;
	                }
	            }
			} catch (IntrospectionException e) {
				e.printStackTrace();
			}
			
            if(writeMethod != null) {
            	try {
            		if(value != null) {
            			writeMethod.invoke(object, value);
            		}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
            }
		}
		return object;
	}
	
	private static Method findMethod(Class<?> cls, String methodName, int argCount) {
		
		Method method = null;
		Method[] methods = cls.getMethods();
		for (int i=0; i<methods.length; i++) {
			
			method = methods[i];
			
			if(method.getName().toLowerCase().equals(methodName.toLowerCase())) {
				Type[] types = method.getGenericParameterTypes();
				if(types.length == argCount) {
					return method;
				}
			}
		}
		return null;
	}
	
	private static Class<?>[] getParameterTypes(Class<?> base, Method method) {
        if (base == null) {
            base = method.getDeclaringClass();
        }
        return method.getParameterTypes();
        //return TypeResolver.erase(TypeResolver.resolveInClass(base, method.getGenericParameterTypes()));
    }
	
	private static Class<?> getReturnType(Class<?> base, Method method) {
        if (base == null) {
            base = method.getDeclaringClass();
        }
        return method.getReturnType();
        //return TypeResolver.erase(TypeResolver.resolveInClass(base, method.getGenericReturnType()));
    }
	
	/**
     * Returns the property type that corresponds to the read and write method.
     * The type precedence is given to the readMethod.
     *
     * @return the type of the property descriptor or null if both
     *         read and write methods are null.
     * @throws IntrospectionException if the read or write method is invalid
     */
    private static Class<?> findPropertyType(Class<?> cls, Method readMethod, Method writeMethod)
        throws IntrospectionException {
        Class<?> propertyType = null;
        try {
            if (readMethod != null) {
                Class<?>[] params = getParameterTypes(cls, readMethod);
                if (params.length != 0) {
                    throw new IntrospectionException("bad read method arg count: " + readMethod);
                }
                propertyType = getReturnType(cls, readMethod);
                if (propertyType == Void.TYPE) {
                    throw new IntrospectionException("read method " + readMethod.getName() + " returns void");
                }
            }
            if (writeMethod != null) {
                Class<?> params[] = getParameterTypes(cls, writeMethod);
                if (params.length != 1) {
                    throw new IntrospectionException("bad write method arg count: " + writeMethod);
                }
                if (propertyType != null && !params[0].isAssignableFrom(propertyType)) {
                    throw new IntrospectionException("type mismatch between read and write methods");
                }
                propertyType = params[0];
            }
        } catch (IntrospectionException ex) {
            throw ex;
        }
        return propertyType;
    }
	
	
}
