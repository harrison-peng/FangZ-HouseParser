package com.server.project.api;

public class House {
	private int id;
	private String title;
	private String description;
	private String address;
	private String location;
	private String price;
	private String type;
	private String url;
	private String addressGeometry;
	private String picture;
	private String information;
	private String community;
	private String life;
	private double[] locationPoint;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public double[] getLocationPoint() {
		return locationPoint;
	}

	public void setLocationPoint(double[] locationPoint) {
		this.locationPoint = locationPoint;
	}

	public String getAddressGeometry() {
		return addressGeometry;
	}

	public void setAddressGeometry(String addressGeometry) {
		this.addressGeometry = addressGeometry;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public String getLife() {
		return life;
	}

	public void setLife(String life) {
		this.life = life;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

}
