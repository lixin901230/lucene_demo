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

	<div>
		<input type="text" id="name" value=""/><span style="color: red; display: none;"> * 不能为空</span>
		<input type="text" id="content" value=""/><span style="color: red; display: none;"> * 不能为空</span>
		<input type="text" id="price" value=""/><span style="color: red; display: none;"> * 不能为空</span>
	</div>
	<div>
		<input type="button" value="保存" id="saveProductBtn"/>
	</div>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/resource/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript">
		
		function isEmpty(val) {
			if(val != null && $.trim(val) != "" && val !="undefined") {
				return true;
			} else {
				return false;
			}
		}
	
		// 添加
		$("addProduct").on("click", function(){
			
			var name = $("#name").val();
			var content = $("#content").val();
			var price = $("#price").val();
			
			if(!isEmpty(name)) {
				$("#name").next("span").show();
				return false;
			}
			if(!isEmpty(content)) {
				$("#content").next("span").show();
				return false;
			}
			if(!isEmpty(price)) {
				$("#price").next("span").show();
				return false;
			}
			
			$.ajax({
				url: '${pageContext.request.contextPath}/productSearch/addProduct.do',
				type: 'post',
				data: {
					"name": name,
					"content": content,
					"price": price
				},
				dataType: 'json',
				cache: false,
				async: true,
				success: function(data) {
					if(data) {
						if(data.success) {
								
						} else {
							
						}
					}
				},
				error: function(){
					alert("请求出错");
				}
			});
		});
	</script>
</body>
</html>
