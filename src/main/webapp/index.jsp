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
			<span id="msgInfo" style="font-size: 12px; display: none;"></span>
		</div>
		
		<div style="margin: 25px 25px;"></div>
		
		<div>在数据库中添加一条产品信息数据并将该数据添加到lucene索引库中</div>
		<div>
			<input type="button" id="loadAddProductPage" value="添加产品信息" title="在数据库中添加一条产品信息数据并将该数据添加到lucene索引库中"/>
			<span id="addMsgInfo" style="font-size: 12px; display: none;"></span>
			<div id="addProductContainer">
				
			</div>
		</div>

		<div style="margin: 25px 25px;"></div>
		
		<div>
			<span id="updateMsgInfo" style="font-size: 12px; display: none;"></span>
			<div id="updateProductContainer">
				
			</div>
		
		<div style="margin: 25px 25px;"></div>
		
		<div>全文搜索产品信息</div>
		<div>
			<input type="text" id="searchKey" value=""/>
			<input type="button" id="searchBtn" value="搜  索"/>
			<span id="searchInfo"></span>
		</div>
		<div style="border: 1px solid #ddd; width: 60%; margin-top: 20px; padding: 50px 50px;">
			<div id="searchResultContainer">搜索结果</div>
		</div>
	</div>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/resource/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript">
		
		$(function(){
			
			// 创建lucene索引（对数据库的数据创建索引
			$("#createLuceneIndex").on("click", function(){
				createLuceneIndex();
			});
			
			// 使用lucene进行全文搜索
			$("#searchBtn").on("click", function(){
				search();
			});
			
			// 添加一条记录
			$("#loadAddProductPage").on("click", function(){
				loadAddProductPage();
				//window.location.href="${pageContext.request.contextPath}/page/product/addProduct.jsp";
			});
			
			// 提交保存添加信息
			$("#addProductContainer").on("click", "#saveProductBtn", function(){
				saveProduct();
			});
			
		});
		
		// 搜索
		function search() {
			
			var searchKey = $("#searchKey").val();
			if(searchKey == null || $.trim(searchKey) == "") {
				$("#searchInfo").css({"color": "red"}).text(" * 请输入搜索关键字").show();
				return false;
			}
			
			$.ajax({
				url: '${pageContext.request.contextPath}/productSearch/searchProducts.do',
				type: 'post',
				data: {
					"content": searchKey
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
							$("#msgInfo").css({"color": "green"}).show().text(" * "+data.msg);
						} else {
							$("#msgInfo").css({"color": "red"}).show().text(" * 查询数据库创建lucene索引库失败，原因："+data.msg);
						}
					}
				},
				error: function(){
					alert("请求出错");
				}
			});
		}
		
		// 加载添加页面表单
		function loadAddProductPage() {
			$.ajax({
				url: '${pageContext.request.contextPath}/page/product/addProduct.jsp',
				type: 'get',
				dataType: 'html',
				cache: false,
				async: true,
				success: function(data) {
					if(data) {
						$("#addProductContainer").show().html(data);
					} else {
						$("#addProductContainer").show().text("加载添加表单失败");
					}
				},
				error: function(){
					alert("请求出错");
				}
			});
		}
	
		// 提交保存添加信息
		function saveProduct(){
			
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
