package com.dell.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.dell.jpa.entity.annotation.Application;

@Entity
@Application(name="NewApplication", ddlAutoCreate=true, storageService="SpiderService", key="NewKey")
@Table(name="NewEntity")
public class NewEntity {
	
	@Id
	@Column(name="_ID")  
	private String id;
	
	private String name;
	private int age;

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
	public int getAge() {
		return age;
	}
	public NewEntity setAge(int age) {
		this.age = age;
		return this;
	}
 
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + age;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NewEntity other = (NewEntity) obj;
        if (age != other.age)
            return false;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
