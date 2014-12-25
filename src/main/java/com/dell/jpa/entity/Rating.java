package com.dell.jpa.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.dell.jpa.entity.annotation.Application;

@Entity
@Application(name="HelloSpider", key="Arachnid")
@Table(name="Rating")
public class Rating {
	
	@Id
	@Column(name="_ID")  
	private String id;
	
	private String rating;

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


}
