package com.lx.complete.bean;

public class ProductInfo {
	
	private String id;
	private String name;
	private String content;
	private Integer number;
	private Double price;
	
	public ProductInfo() {
		
	}
	
	public ProductInfo(String id, String name, String content, Integer number,
			Double price) {
		this.id = id;
		this.name = name;
		this.content = content;
		this.number = number;
		this.price = price;
	}
	
	public ProductInfo(String id, String name, String content, Integer number,
			Double price, boolean flag) {
		this.id = id;
		this.name = name;
		this.content = content;
		this.number = number;
		this.price = price;
		this.flag = flag;
	}



	private boolean flag;	//非持久化属性
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
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
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
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
		return "ProductInfo [id=" + id + ", number=" + number + ", name="
				+ name + ", content=" + content + ", price=" + price
				+ ", flag=" + flag + "]";
	}
	
}
