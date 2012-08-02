package com.heath_bar.tvdb.types;

public class Actor {

	private int id;
	private String name;
	private String role;
	private WebImage image;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public WebImage getImage() {
		if (image == null)
			image = new WebImage();
		return image;
	}
	public void setImage(WebImage image) {
		this.image = image;
	}
	
	
}
