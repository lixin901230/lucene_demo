package com.lx.lucene.index.nrtsearch;  

import java.util.HashSet;
  
public class IndexConfig {
	
	//系统中配置多个索引
	private static HashSet<ConfigBean> configBeans;
	
	private static class DefaultIndexConfig {
		private static final HashSet<ConfigBean> configBeansDefault = new HashSet<ConfigBean>();
		static {
			ConfigBean bean = new ConfigBean();
			configBeansDefault.add(bean);
		}
	}
	
	public static HashSet<ConfigBean> getConfig() {
		if (configBeans == null) {
			//如果configBeans为空，返回系统默认值
			return DefaultIndexConfig.configBeansDefault;
		}
		return configBeans;
	}
	
	/**
	 * @param configBeans
	 * @Description: 设置系统索引配置
	 */
	public static void setConfig(HashSet<ConfigBean> configBeans) {
		IndexConfig.configBeans = configBeans;
	}

}
