注意：本lucene demo 基于lucene_5.4 版本

lucene版本与jdk版本对照：
	Java 1.4, Unicode 3.0
	Java 5, Unicode 4.0
	Java 6, Unicode 4.0
	Java 7, Unicode 6.0
	Java 8, Unicode 6.2


lucene 索引查看工具luke下载地址：
	https://yunpan.cn/cq7QybnmCsk6a  访问密码 0452
	
示例demo内容：	
	1、lucene索引操作（增删改查）
	2、lucene自带的常用分词器 及 第三方IK中文分词器 演示
	3、lucene各种查询对象演示
	4、lucene搜索结果关键字高亮显示演示
	5、（实战）模拟一个真实项目中对数据库的增、删、改时如何使用lucene创建、更新、删除lucene索引库 以及 如何根据lucene索引进行全文搜索

示例目录说明：
	com.lx.lucene				演示纯lucene 的索引管理（增删改查）、分词器（含第三方IK分词器）、各种查询器 ，及搜索结果高亮显示示例
	com.lx.lucene.filesearch	演示纯lucene 对文件进行创建索引，并根据文件内容对文件进行搜索
	com.lx.lucene.IKAnalyzer5x	第三方IK分词器对lucene5.x新版本的支持扩展
	com.lx.complete				演示lucene在实际项目中结合数据库混合使用的完整示例（根据数据库创建索引，且数据库的增删改记录时同时维护更新lucene索引，搜索时使用lucene的索引进行检索）
	com.lx.util					工具类
	

Lucene 基本操作：
	1、创建Directory，获取索引目录
　　	2、创建词法分析器Analyzer，创建IndexWriter对象
　　	3、创建Document对象，创建Field对象并设置属性和值，存储数据
　　	4、关闭IndexWriter，提交