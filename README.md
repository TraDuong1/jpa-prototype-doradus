“JPA Light” for Doradus Project
======================================

The purpose of this project is to demonstrate how to utilize JPA for Doradus.  
### Features

The features provided by this project include:

- CRUD operations
  	* Save entity (with Collections).
	* Delete entity.     
  	* Update entity.      
	* Get entity by unique ID
	* Get entity by query

- Schema Auto Creation
  	* Automatically create schema (application, table and fields) from Entity. 
  

### You can take a look at the sample test 

- [MappingSessionTest](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/test/java/com/dell/jpa/mapping/MappingSessionTest.java) to see how to persist a JPA entity into Doradus and retrieve it using Object Mapper APIs [MappingSession](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/main/java/com/dell/jpa/mapping/MappingSession.java)
- With support of custom annotations that allows auto-create Doradus schema if it doesn’t exist, Application and Table can be created 1st time saving the entity. See attributes defined in [Application annotation](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/main/java/com/dell/jpa/entity/NewEntity.java)

### Getting started
You will need:
- [Doradus](https://github.com/dell-oss/Doradus) instance running on your localhost 
- Maven Dependency.  
```xml
    <dependency>
      <groupId>com.dell.doradus.jpa</groupId>
      <artifactId>jpa-prototype-doradus</artifactId>
      <version>1.0.0</version>
    </dependency>
```
