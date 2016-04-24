package com.lx.lucene.util;

import java.util.UUID;

public class UUIDTools {
	
	public static void main(String[] args) {
		for (int i = 0; i < 1000; i++) {
			String uuid = getUUID();
			System.out.println(uuid+"\t"+uuid.length());
		}
	}
	
	/**
	 * 获取一个32位字符串的唯一ID
	 * @return
	 */
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		String id = uuid.toString().replaceAll("-", "");
		return id;
	}
}
