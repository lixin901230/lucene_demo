<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>添加产品信息</title>
</head>
<body>
	<!-- <div>添加产品信息</div> -->
	<div>
		<input type="text" id="name" value=""/><span style="color: red; display: none;"> * 不能为空</span>
		<input type="text" id="content" value=""/><span style="color: red; display: none;"> * 不能为空</span>
		<input type="text" id="price" value=""/><span style="color: red; display: none;"> * 不能为空</span>
	</div>
	<div>
		<input type="button" value="保存" id="saveProductBtn"/>
	</div>
</body>
</html>