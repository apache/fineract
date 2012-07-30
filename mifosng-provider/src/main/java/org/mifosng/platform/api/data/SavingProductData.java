package org.mifosng.platform.api.data;

import java.io.Serializable;

import org.joda.time.DateTime;

public class SavingProductData implements Serializable {

	private Long id;
	private String name;
	private String description;
	
	private DateTime createdOn;
	private DateTime lastModifedOn;
	
	public SavingProductData(){
		//
	}

	public SavingProductData(DateTime createdOn, DateTime lastModifedOn, Long id,String name, String description) {
		this.createdOn=createdOn;
		this.lastModifedOn=lastModifedOn;
		this.id=id;
		this.name=name;
		this.description=description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public DateTime getLastModifedOn() {
		return lastModifedOn;
	}

	public void setLastModifedOn(DateTime lastModifedOn) {
		this.lastModifedOn = lastModifedOn;
	}
	
	
	
}
