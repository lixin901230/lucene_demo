package com.lx.util;

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
		return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
	}
	
}
