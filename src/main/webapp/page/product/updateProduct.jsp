<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>修改产品信息</title>
</head>
<body>
	<div>
		<input type="text" id="id" value="${product.id}"/><span style="color: red; display: none;"> * 不能为空</span>
		<input type="text" id="name" value="${product.name}"/><span style="color: red; display: none;"> * 不能为空</span>
		<input type="text" id="content" value="${product.content}"/><span style="color: red; display: none;"> * 不能为空</span>
		<input type="text" id="price" value="${product.price}"/><span style="color: red; display: none;"> * 不能为空</span>
	</div>
	<div>
		<input type="button" value="保存" id="editSaveProductBtn"/>
	</div>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/resource/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript">

		$(function(){
			
			// 保存修改信息
			$("#updateProductContainer").on("click", "#editSaveProductBtn", function(){
				editSaveProduct();
			});
		});
		
		// 编辑保存产品信息
		function editSaveProduct() {
			var id = $("#id").val();
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
				url: '${pageContext.request.contextPath}/productSearch/updateProductById.do',
				type: 'post',
				data: {
					"id": id,
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
							$("#addMsgInfo").css({"color": "green"}).text("添加成功！").show();
							$("#name").val("");
							$("#content").val("");
							$("#price").val("");
						} else {
							$("#addMsgInfo").css({"color": "red"}).text("添加失败！").show();
						}
						setTimeout(function(){
							$("#addMsgInfo").text("").hide();
						} , 2000);
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