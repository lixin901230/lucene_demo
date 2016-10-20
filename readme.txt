注意：本lucene demo 基于lucene_5.4 版本
http://archive.apache.org/dist/lucene/java/

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
	6、近实时搜索SearcherManager、TrackingIndexWriter和ControlledRealTimeReopenThread的使用
	
示例目录说明：
	com.lx.lucene					演示纯lucene 的索引管理（增删改查）、分词器（含第三方IK分词器）、各种查询器 ，及搜索结果高亮显示示例
	com.lx.lucene.filesearch		演示纯lucene 对文件进行创建索引，并根据文件内容对文件进行搜索
	com.lx.lucene.IKAnalyzer5x		第三方IK分词器对lucene5.x新版本的支持扩展
	com.lx.complete					演示lucene在实际项目中结合数据库混合使用的完整示例（根据数据库创建索引，且数据库的增删改记录时同时维护更新lucene索引，搜索时使用lucene的索引进行检索）
	com.lx.lucene.index.manager		手动commit提交更新搜索管理类LuceneManager、近实时搜索索引管理IndexManager（该近实时搜索以被NRTSearchManager优化取代）
	com.lx.lucene.index.nrtsearch	近实时搜索索引管理（完整示例：NRTSearchManager、ConfigBean、IndexConfig、NRTIndex、NRTSearch）
	com.lx.util						工具类
	

Lucene 基本操作：
	1、创建Directory，获取索引目录
　　	2、创建词法分析器Analyzer，创建IndexWriter对象
　　	3、创建Document对象，创建Field对象并设置属性和值，存储数据
　　	4、提交，关闭IndexWriter
注意：
	在使用近实搜索索引管理类时：（关于近实时搜索的具体说明：请参考com.lx.lucene.index.nrtsearch.NRTSearchManager.java类的类说明）
	1）、添删改索引都不用手动去commit提交合并索引文件，	操作索引文件时由TrackingIndexWriter来取代IndexWriter，索引操作都在内存中不用手动频繁的commit（频繁的更新索引文件很耗费性能），
	那么所有文件的更新将由ControlledRealTimeReopenThread类来定时去提交内存中的索引与硬盘中的索引文件进行合并（这样较少了硬盘索引文件的频繁操作，提高了性能）；
	2）、在实现近实时搜索时，使用SearcherManager类来获取IndexSearcher来进行搜索

未演示demo的部分：
	1、排序查询 sort（在NRTSearch类中有封装，但为给出测试示例）
	2、查询分页（在NRTSearch类中有封装，但为给出测试示例）
	3、分组统计 group by