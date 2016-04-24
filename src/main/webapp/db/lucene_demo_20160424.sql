/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50173
Source Host           : localhost:3306
Source Database       : lucene_demo

Target Server Type    : MYSQL
Target Server Version : 50173
File Encoding         : 65001

Date: 2016-04-25 00:01:36
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `country_info`
-- ----------------------------
DROP TABLE IF EXISTS `country_info`;
CREATE TABLE `country_info` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(200) DEFAULT NULL COMMENT '国家名称',
  `capital` varchar(100) DEFAULT NULL COMMENT '首都',
  `description` text COMMENT '国家简介',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='国家信息表';

-- ----------------------------
-- Records of country_info
-- ----------------------------
INSERT INTO `country_info` VALUES ('0af5455fe4544d72b9dfbd8be97e8c2a', '泰国', '曼谷', '曼谷是泰国的首都');
INSERT INTO `country_info` VALUES ('0dc9ec53a4dd4133b20a171345c87db7', '韩国', '首尔', '首尔是韩国的首都');
INSERT INTO `country_info` VALUES ('1d3c21850034464fb6cb9e6afa544a9f', '俄罗斯', '莫斯科', '莫斯科是俄罗斯的首都');
INSERT INTO `country_info` VALUES ('59be00c93e5146868c92a8e0b6491844', '中国', '北京', '北京是中国的首都');
INSERT INTO `country_info` VALUES ('5a0f796bf03b41e3befc6d289f133358', '瑞典', '斯德哥尔摩', '斯德哥尔摩是瑞典的首都');
INSERT INTO `country_info` VALUES ('7069189169b849d88bba1ad629ab97bf', '马来西亚', '吉隆坡', '吉隆坡是马来西亚的首都');
INSERT INTO `country_info` VALUES ('82d7735b7ddf4c1b847a15ec0cf5e936', '德国', '柏林', '柏林是德国的首都');
INSERT INTO `country_info` VALUES ('8fdf804800544739b601b8fa115d091e', '朝鲜', '平壤', '平壤是朝鲜的首都');
INSERT INTO `country_info` VALUES ('98d361e81ecf49c698233ec7237f5e82', '美国', '华盛顿', '华盛顿是美国的首都');
INSERT INTO `country_info` VALUES ('991404c87fb8464b92b918eda54e4129', '法国', '巴黎', '巴黎是发过的首都');
INSERT INTO `country_info` VALUES ('abfb98eeb38846949c3b2025473504ad', '意大利', '罗马', '罗马是意大利的首都');
INSERT INTO `country_info` VALUES ('b5ee2c2a03c64f34af1ad9b6b774337f', '越南', '河内', '河内是越南的首都');
INSERT INTO `country_info` VALUES ('bd413696ae354079ac4e311ce792237a', '英国', '伦敦', '伦敦是英国的首都');
INSERT INTO `country_info` VALUES ('c40549b789b14376988ecf60d5d29f50', '印度', '新德里', '新德里是印度的首都');
INSERT INTO `country_info` VALUES ('f0fafe2d6c2c4016a8ec705ddcce2253', '瑞士', '伯尔尼', '伯尔尼是瑞士的首都');
INSERT INTO `country_info` VALUES ('fc69a2ec27864896822c4e167b69f956', '日本', '东京', '东京是日本的首都');

-- ----------------------------
-- Table structure for `product_info`
-- ----------------------------
DROP TABLE IF EXISTS `product_info`;
CREATE TABLE `product_info` (
  `id` int(10) NOT NULL,
  `name` varchar(200) DEFAULT NULL,
  `title` text,
  `price` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品信息表';

-- ----------------------------
-- Records of product_info
-- ----------------------------
INSERT INTO `product_info` VALUES ('1', '草鱼', '湖南张家界清水鱼', '12');
INSERT INTO `product_info` VALUES ('2', '大米', '东北优质大米', '90');
INSERT INTO `product_info` VALUES ('3', '橘子', '湖南张家界石门橘子', '15');
INSERT INTO `product_info` VALUES ('4', '葡萄', '新疆高原又大又甜的葡萄', '20');
