package com.lx.complete.bean;

public class ProductInfo {
	
	private Integer id;
	private String name;
	private String content;
	private Double price;
	
	private boolean flag;	//非持久化属性
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	@Override
	public String toString() {
		return "ProductInfo [price=" + price + ", id=" + id + ", name=" + name
				+ ", content=" + content + ", flag=" + flag + "]";
	}
	
}
