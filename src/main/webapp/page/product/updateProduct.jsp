<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>修改产品信息</title>
</head>
<body>
	<div>修改产品信息</div><br/>
	<div id="editForm">
		<input type="hidden" id="id" value="${product.id}"/>
		name：<input type="text" id="name" value="${product.name}"/><span style="color: red; display: none;"> * 不能为空</span><br/><br/>
		content：<input type="text" id="content" value="${product.content}"/><span style="color: red; display: none;"> * 不能为空</span><br/><br/>
		number：<input type="text" id="number" value="${product.number}"/><span style="color: red; display: none;"> * 不能为空</span><br/><br/>
		price：<input type="text" id="price" value="${product.price}"/><span style="color: red; display: none;"> * 不能为空</span><br/><br/>
	</div>
	<div>
		<input type="button" value="保存" id="editSaveProductBtn"/>
		<input type="button" value="取消" id="goBack"/>
	</div>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/resource/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript">

		$(function(){
			
			// 保存修改信息
			$("#editSaveProductBtn").on("click", function(){
				editSaveProduct();
			});
			
			// 返回
			$("#goBack").click(function(){
				window.location.href="${pageContext.request.contextPath}/index.jsp";
			});
		});
		
		// 编辑保存产品信息
		function editSaveProduct() {
			var id = $("#id").val();
			var name = $("#name").val();
			var content = $("#content").val();
			var number = $("#number").val();
			var price = $("#price").val();
			
			if(!isEmpty(name)) {
				$("#name").next("span").show();
				return false;
			}
			if(!isEmpty(content)) {
				$("#content").next("span").show();
				return false;
			}
			if(!isEmpty(number)) {
				$("#number").next("span").show();
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
					"number": number,
					"price": price
				},
				dataType: 'json',
				cache: false,
				async: false,
				success: function(data) {
					if(data) {
						if(data.success) {
							var r = confirm("修改成功，是否回到搜索首页？");
							if(r) {
								window.location.href="${pageContext.request.contextPath}/index.jsp";
							}
							//window.location.href="${pageContext.request.contextPath}/index.jsp";
						} else {
							alert("修改失败");
						}
					}
				},
				error: function(){
					alert("请求出错");
				}
			});
		}
		
		function isEmpty(val) {
			if(val != null && $.trim(val) != "" && val !="undefined") {
				return true;
			} else {
				return false;
			}
		}
	</script>	
</body>
</html>