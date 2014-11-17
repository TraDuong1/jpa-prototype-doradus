package com.dell.jpa.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.dell.jpa.entity.annotation.Application;

@Entity
@Application(name="TestApplication")
@Table(name="SimpleEntity")
public class SimpleEntity {
	
	@Id
	private UUID id;
	private String name;
	private Date timestamp;
	private long version;
	private int age;
	private int random;
	
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	public int getAge() {
		return age;
	}
	public SimpleEntity setAge(int age) {
		this.age = age;
		return this;
	}
    public int getRandom() {
        return random;
    }
    public void setRandom(int random) {
        this.random = random;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + age;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + random;
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + (int) (version ^ (version >>> 32));
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
        SimpleEntity other = (SimpleEntity) obj;
        if (age != other.age)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (random != other.random)
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

}
