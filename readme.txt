注意：本lucene demo 基于lucene_5.4 版本

lucene版本与jdk版本对照：
	Java 1.4, Unicode 3.0
	Java 5, Unicode 4.0
	Java 6, Unicode 4.0
	Java 7, Unicode 6.0
	Java 8, Unicode 6.2


lucene 索引查看工具luke下载地址：
	https://yunpan.cn/cq7QybnmCsk6a  访问密码 0452


示例目录说明：
	https://github.com/lixin901230/lucene_demo.git

	com.lx.lucene		演示纯lucene 的索引管理（增删改查）、分词器（含第三方IK分词器）、各种查询器 ，及搜索结果高亮显示示例
	com.lx.complete		演示lucene在实际项目中结合数据库混合使用的完整示例（根据数据库创建索引，对数据库的增删改时同时维护lucene索引，搜索时使用lucene的索引进行检索）