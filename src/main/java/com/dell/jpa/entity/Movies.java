package com.dell.jpa.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.dell.jpa.entity.annotation.Application;

@Entity

@Application(name="HelloSpider", key="Arachnid")
@Table(name="Movies")
public class Movies implements Serializable {

	private static final long serialVersionUID = 4984054122848129006L;
	
	@Id
	@Column(name="_ID")  
	private String id;
	
	@Column(name="Name")  
	private String name;	
	
	@Column(name="Director")  
	private String director;
	
	@Column(name="Leads")  	
	private Set<String> leads;
	
	@Column(name="Cancelled")  	
	private boolean cancelled;

	@Column(name="ReleaseDate")  
	private Date releaseDate;
	
	@Column(name="Budget") 		
	private long budget;
	
	public Movies() {    
	}
	
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
	
	
	
	public long getBudget() {
		return budget;
	}
	
	
	
	public void setBudget(long budget) {
		this.budget = budget;
	}
	
	
	
	public String getDirector() {
		return director;
	}
	
	
	
	public void setDirector(String director) {
		this.director = director;
	}

	public boolean isCancelled() {
		return cancelled;
	}
	
	
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	
	
	public Date getReleaseDate() {
		return releaseDate;
	}
	
	
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Set<String> getLeads() {
		return leads;
	}

	public void setLeads(Set<String> leads) {
		this.leads = leads;
	}
}
