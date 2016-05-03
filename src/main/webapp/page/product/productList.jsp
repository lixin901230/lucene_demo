<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>lucene搜索结果</title>
	<style type="text/css">
		<!-- CSS goes in the document HEAD or added to your external stylesheet -->
		table {
			border-collapse: collapse;
		}
		table.gridtable {
			font-family: verdana,arial,sans-serif;
			font-size:11px;
			color:#333333;
			border-width: 1px;
			border-color: #666666;
			border-collapse: collapse;
		}
		table.gridtable th {
			border-width: 1px;
			padding: 8px;
			border-style: solid;
			border-color: #666666;
			background-color: #dedede;
		}
		table.gridtable td {
			border-width: 1px;
			padding: 8px;
			border-style: solid;
			border-color: #666666;
			background-color: #ffffff;
		}
	</style>
</head>
<body>
	<c:choose>
		<c:when test="${(not empty products) && fn:length(products) > 0}">
			搜到的记录信息
			<div>
				<table class="gridtable">
					<tr>
						<th>ID</th>
						<th>Name</th>
						<th>Content</th>
						<th>Price</th>
						<th>操作</th>
					</tr>
					<c:forEach var="product" items="${products}">
						<tr>
							<td>${product.id}</td>
							<td>${product.name}</td>
							<td>${product.content}</td>
							<td>${product.price}</td>
							<td>
								<a href="javascript:void(0);" id="deleteProduct_${product.id}">删除</a>
								<a href="javascript:void(0);" id="updateProduct_${product.id}">修改</a> | 
							</td>
						</tr>
					</c:forEach>
				</table>
			</div>
		</c:when>
		<c:otherwise>没有搜索到您想要的记录</c:otherwise>
	</c:choose>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/resource/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript">

		$(function(){
			
			// 加载修改页面
// 			$(".gridtable").on("click", "a[id^='updateProduct_']", function(){
// 				loadUpdateProductPage(this);
// 			});
			
			// 加载修改页面
			$(".gridtable").on("click", "a[id^='deleteProduct_']", function(){
				deleteProduct(this);
			});
		});
		

		// 加载修改产品信息页面
		function loadUpdateProductPage(_this) {
			var id = $(_this).prop("id");
			id = id.substring("updateProduct_".length, id.length);
			alert("id="+id);
			$.ajax({
				url: '${pageContext.request.contextPath}/productSearch/getProductById.do',
				type: 'post',
				data: {"id": id},
				dataType: 'html',
				cache: false,
				async: true,
				success: function(data) {
					if(data) {
						$("#updateProductContainer").show().html(data);
					} else {
						$("#updateProductContainer").show().text("加载修改表单失败");
					}
				},
				error: function(){
					alert("请求出错");
				}
			});
		}
		
		// 删除
		function deleteProduct(_this) {
			var id = $(_this).prop("id");
			id = id.substring("deleteProduct_".length, id.length);
			alert("id="+id);
			$.ajax({
				url: '${pageContext.request.contextPath}/productSearch/deleteProduct.do',
				type: 'post',
				data: {"productId": id},
				dataType: 'json',
				cache: false,
				async: true,
				success: function(data) {
					if(data) {
						if(data.success) {
							alert("删除成功")
							//$("#searchBtn").click();
						}
					} else {
						alert("删除失败！");
					}
				},
				error: function(){
					alert("请求出错");
				}
			});
		}
		
	</script>
</body>
</html>