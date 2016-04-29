<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>全文搜索</title>
</head>
<body>
	<div>
		<div>对数据库中的数据创建lucene索引库</div>
		<div>
			<input type="button" id="createLuceneIndex" value="重新创建索引" title="对数据库数据创建lucene索引"/>
			<span id="errorMsg" style="color: red; font-size: 12px; display: none;"> * 查询数据库创建lucene索引库失败，原因：<span id="errorInfo"></span></span>
		</div>
		<div style="margin: 25px 25px;"></div>
		<div>全文搜索产品信息</div>
		<div>
			<input type="text" id="searchKey" value=""/>
			<input type="button" id="searchBtn" value="搜  索"/>
		</div>
		<div style="border: 1px solid #ddd; width: 60%; margin-top: 20px; padding: 50px 50px;">
			<div>搜索结果</div>
			<div id="searchResultContainer"></div>
		</div>
	</div>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/resource/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript">
		
		$(function(){
			
			// 使用lucene进行全文搜索
			$("#searchBtn").on("click", function(){
				search();
			});
			
			// 创建lucene索引（对数据库的数据创建索引
			$("#createLuceneIndex").on("click", function(){
				createLuceneIndex();
			});
		});
		
		// 搜索
		function search() {
			
			var searchKey = $("#searchKey").val();
			debugger
// 			if(searchKey == null || $.trim(searchKey) == "") {
// 				return false;
// 			}
			
			$.ajax({
				url: '${pageContext.request.contextPath}/productSearch/searchProducts.do',
				type: 'post',
				data: {
					"product.content": searchKey
				},
				dataType: 'html',
				cache: false,
				async: true,
				success: function(data) {
					
					if(data) {
						$("#searchResultContainer").html(data);
						console.info(data);
					}
				},
				error: function(){
					alert("请求出错");
				}
			});
		}
		
		// 对数据库数据创建lucene索引
		function createLuceneIndex(){
			$.ajax({
				url: '${pageContext.request.contextPath}/productSearch/createLuceneIndex.do',
				type: 'post',
				dataType: 'json',
				cache: false,
				async: true,
				success: function(data) {
					if(data) {
						if(data.success) {
							$("#errorMsg").hide();
						} else {
							$("#errorMsg").show().find("#errorInfo").text(data.msg);
						}
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