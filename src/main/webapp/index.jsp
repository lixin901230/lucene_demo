<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>全文搜索</title>
</head>
<body>
	<div>
		<div>全文搜索产品信息</div>
		<div>
			<input type="text" id="searchKey" value=""/>
			<input type="button" id="searchBtn" value="搜  索"/>
		</div>
	</div>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resource/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript">
		
		$(function(){
			
			$("#searchBtn").on("click", function(){
				search();
			});
		});
		
		// 搜索
		function search() {
			
			var searchKey = $("#searchKey").val();
			debugger
			if(searchKey == null || $.trim(searchKey) == "") {
				return false;
			}
			
			$.ajax({
				url: '${pageContext.request.contextPath}/productSearch/searchProducts.do',
				type: 'post',
				data: {
					"searchKey": searchKey
				},
				dataType: 'json',
				cache: false,
				async: true,
				success: function(data) {
					
					if(data) {
						console.info(data);
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