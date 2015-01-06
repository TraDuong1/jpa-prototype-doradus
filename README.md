“JPA Light” for Doradus Project
======================================

The purpose of this project is to demonstrate how to utilize JPA for Doradus.  
### Features

The features provided by this project include:

- CRUD operations
  	* Create entity (with primitive and collection fields).
 	* Create entity (with object associations).
	* Delete entity.     
  	* Update entity.      
	* Retrieve entity by unique ID
	* Retrieve entity by query

- Schema Auto Creation
  	* Automatically create schema (application, table and fields) from Entity. 
  

### Getting started
You will need
- [Doradus](https://github.com/dell-oss/Doradus) instance running on your localhost 
- Maven Dependency.  
```xml
    <dependency>
      <groupId>com.dell.doradus.jpa</groupId>
      <artifactId>jpa-prototype-doradus</artifactId>
      <version>1.0.0</version>
    </dependency>
```

### How to use Doradus-JPA Light APIs

#### Entity with annotations
-Schema already exists (application: HelloSpider, table: Movies and fields (_ID, Name)
```java
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

#### Save

- [MappingSessionTest](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/test/java/com/dell/jpa/mapping/MappingSessionTest.java) to see how to persist a JPA entity into Doradus and retrieve it using Object Mapper APIs [MappingSession](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/main/java/com/dell/jpa/mapping/MappingSession.java)
- With support of custom annotations that allows auto-create Doradus schema if it doesn’t exist, Application and Table can be created 1st time saving the entity. See attributes defined in [Application annotation](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/main/java/com/dell/jpa/entity/NewEntity.java)
