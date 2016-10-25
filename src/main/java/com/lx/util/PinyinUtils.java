package com.lx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtils {
	public static Pattern cnWordPattern = Pattern.compile("[\u4E00-\u9FA5]");
	public static Pattern enWordPattern = Pattern.compile("[a-zA-Z0-9]");
	
	private static HanyuPinyinOutputFormat getDefaultPinyinFormat(){
		HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
		outputFormat.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);//以数字带拼音声调，如考试：kao3shi4
		outputFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
		outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);//小写字母（与小写英文一起排序，数字、大写英文排在前面）
		
		return outputFormat;
	}
	
	public static String getPinyinWord(char word){
		HanyuPinyinOutputFormat outputFormat = getDefaultPinyinFormat();
		
		String wordStr = String.valueOf(word);
		Matcher cnWordMatcher = cnWordPattern.matcher(wordStr);
		
		if(cnWordMatcher.find()){
			String[] pinyin;
			try {
				pinyin = PinyinHelper.toHanyuPinyinStringArray(word, outputFormat);
			} catch (BadHanyuPinyinOutputFormatCombination e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}
			if(pinyin != null && pinyin.length > 0) {
				return pinyin[0];//多音字取默认第一个读音
			} else {
				return "";
			}
		} else {
			Matcher enWordMatcher = enWordPattern.matcher(wordStr);
			if(enWordMatcher.find()){
				return wordStr;
			} else {
				return "";
			}
		}
	}
	
	public static String getPinyinSentence(String sentence){
		if(sentence == null || sentence.length() == 0){
			return "";
		}
		StringBuilder pinyinSent = new StringBuilder();
		for (int i = 0; i < sentence.length(); i++) {
			char word = sentence.charAt(i);
			String pinyinWordStr = getPinyinWord(word);
			pinyinSent.append(pinyinWordStr);
		}
		return pinyinSent.toString();
	}
	
	public static List<String> getPurePinyinList(String sentence){
		if(sentence == null || sentence.length() == 0){
			return null;
		}
		List<String> pinyinSentList = new ArrayList<String>();
		for (int i = 0; i < sentence.length(); i++) {
			char word = sentence.charAt(i);
			String pinyinWordStr = getPinyinWord(word);
			if(pinyinWordStr != null && StringUtils.isNotEmpty(pinyinWordStr)){
				pinyinSentList.add(pinyinWordStr);
			}
		}
		return pinyinSentList;
	}
	
	/**
	 * 按拼音比较两句话，第一个小，返回-1；第一个大，返回1；相同返回0
	 */
	public static int comparePinyinSentence(String sentStr1, String sentStr2){
		List<String> pinyinList1 = getPurePinyinList(sentStr1);
		List<String> pinyinList2 = getPurePinyinList(sentStr2);
		
		if(pinyinList1 == null || pinyinList1.size() == 0){
			if(pinyinList2 == null || pinyinList2.size() == 0){
				return 0;
			} else {
				return -1;
			}
		} else if (pinyinList2 == null || pinyinList2.size() == 0){
			return 1;
		}

		int length1 = pinyinList1.size();
		int length2 = pinyinList2.size();

		int compareLength = length1;
		
		if(length2 < compareLength){
			compareLength = length2;
		}

		for(int i = 0; i < compareLength; i++){
			int compare = compareWordString(pinyinList1.get(i), pinyinList2.get(i));
			if(compare == 0){
				continue;
			} else {
				return compare;
			}
		}
		
		//最小长度内内容相同，文字多的为大
		if(length1 > length2){
			return 1;
		} else if (length1 < length2){
			return -1;
		} else {
			return 0;
		}
	}
	
	/**
	 * 按字符比较两个字，第一个字小，返回-1；第一个字大，返回1；相等返回0
	 * @param wordStr1
	 * @param wordStr2
	 * @return
	 */
	private static int compareWordString(String wordStr1, String wordStr2){
		char[] wordArray1 = wordStr1.toCharArray();
		char[] wordArray2 = wordStr2.toCharArray();
		
		int length1 = wordArray1.length;
		int length2 = wordArray2.length;
		
		//empty str
		if(length1 == 0){
			if(length2 == 0){
				return 0;
			} else {
				return -1;
			}
		} else if (length2 == 0) {
			return 1;
		}
		
		int compareLength = length1;
		
		if(length2 < compareLength){
			compareLength = length2;
		}

		for(int i = 0; i < compareLength; i++){
			if(wordArray1[i] < wordArray2[i]){
				return -1;
			} else if (wordArray1[i] > wordArray2[i]){
				return 1;
			} else {
				continue;
			}
		}
		if(length1 > length2){
			return 1;
		} else if (length1 < length2){
			return -1;
		} else {
			return 0;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(comparePinyinSentence("考试A", "考试B"));
		System.out.println(comparePinyinSentence("大学英语（1）", "大学英语2 "));
	}
}
