package com.heath_bar.tvdb.types;

public class Rating {

	private String id;
	private String userRating;
	private String communityRating;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserRating() {
		return userRating;
	}
	public void setUserRating(String userRating) {
		this.userRating = userRating;
	}
	public String getCommunityRating() {
		return communityRating;
	}
	public void setCommunityRating(String communityRating) {
		this.communityRating = communityRating;
	}
	
}
