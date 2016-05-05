<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>添加产品信息</title>
</head>
<body>

	<div>添加产品信息</div>
	<div style="padding: 10px 10px">在数据库中添加一条产品信息数据并将该数据添加到lucene索引库中</div>
	<div>
		name：<input type="text" id="name" value=""/><span style="color: red; display: none;"> * 不能为空</span><br/><br/>
		content：<input type="text" id="content" value=""/><span style="color: red; display: none;"> * 不能为空</span><br/><br/>
		number：<input type="text" id="number" value=""/><span style="color: red; display: none;"> * 不能为空</span><br/><br/>
		price：<input type="text" id="price" value=""/><span style="color: red; display: none;"> * 不能为空</span><br/><br/>
	</div>
	<div>
		<input type="button" value="保存" id="saveProductBtn"/>
		<input type="button" value="返回" id="goBack"/>
	</div>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/resource/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript">
	
		$(function(){
			// 提交保存添加信息
			$("#saveProductBtn").on("click", function(){
				saveProduct();
			});
			
			// 返回
			$("#goBack").click(function(){
				window.location.href="${pageContext.request.contextPath}/index.jsp";
			});
		});
		
		// 提交保存添加信息
		function saveProduct(){
			
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
				url: '${pageContext.request.contextPath}/productSearch/addProduct.do',
				type: 'post',
				data: {
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
							var r = confirm("添加成功，是否继续添加？");
							if(r) {
								window.location.reload();			
							} else {
								window.location.href="${pageContext.request.contextPath}/index.jsp";
							}
						} else {
							alert("添加失败");
						}
					}
				},
				error: function(){
					alert("请求出错");
				}
			});
		};
		
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
