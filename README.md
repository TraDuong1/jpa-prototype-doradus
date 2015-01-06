“JPA Light” for Doradus Project
======================================

The purpose of this project is to demonstrate how to utilize JPA for Doradus.  
### Features

The features provided by this project include:

- CRUD operations
  	* Storing JPA entity objects (with primitive and collection fields)
 	* Storing JPA entity (with object associations)
	* Retrieving JPA entity by unique ID
	* Retrieving JPA entities by query
	* Updating JPA entity.      
	* Deleting JPA entity.     
  

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
-Schema already exists 

for example; the application “HelloSpider”, table “Movies” and fields (_ID, Name) already exist

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
	
	@Id
	@Column(name="_ID")  
	private String id;
	
	@Column(name="Name")  
	private String name;
	…	
}
```
-Schema does not exist. You can enable schema auto-creation by overriding the attribute ddlAutoCreate to true
```java
@Entity
@Application(name="TestApplication", ddlAutoCreate=true, storageService="SpiderService", key="TestKey")
@Table(name="Person")
public class Person {
	
	@Id
	@Column(name="_ID")  
	private String id;
	
	private String name;
	private int age;
	…	
}
```

#### Obtain a mapping session as a lightweight wrapper for the Doradus Client
-Via Spring context
```xml
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd">
	
	<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
		<property name="location">
			<value>doradus.properties</value>
		</property>
	</bean>

	<bean id="client" class="com.dell.doradus.client.Client">
      	<constructor-arg type="java.lang.String" value="${doradus.host}"/>
		<constructor-arg type="int" value="${doradus.port}"/>
	</bean>
	
	<bean id="mappingSession" class="com.dell.jpa.mapping.MappingSession"/>
	
</beans>
```

#### Save
- Save JPA entity object (with primitive and collection fields)
```java
		Movies entity = new Movies();
		entity.setName("Spririted Away");
		entity.setDirector("Hayao Miyazaki");
		entity.setBudget(100);
		entity.setCancelled(false);
		Set<String> leads = new HashSet<String>(Arrays.asList("Haku,", "Rin"));
		entity.setLeads(leads);
		entity.setReleaseDate(new Date());

		Movies savedEntity = mappingSession.save(entity);
```

- Save JPA entity object with associations
```java
@Entity
@Application(name="TestApplication", ddlAutoCreate=true, storageService="SpiderService", key="TestKey")
@Table(name="Person")
public class Person {	
	…
	@Column(name="Addresses")  	
	@Link(name="Addresses", inverseName="Person", tableName="Address", fieldName="addressIds")
	private Set<String> addressIds;
}

@Entity
@Application(name="TestApplication", ddlAutoCreate=true, storageService="SpiderService", key="TestKey")
@Table(name="Address")
public class Address {
	@Id
	@Column(name="_ID")  
	private String id;
	
	@Column(name="Street")  	
	private String street;
	
	@Column(name="City")  
        …
}		

		Address homeAddress = new Address();
		homeAddress.setStreet("34212 Orcas");
		homeAddress.setCity("Renton");

		Address workAddress = new Address();
		workAddress.setStreet("111 Main St");
		workAddress.setCity("AV");
	
		Person person = new Person();
		person.setAge(40);
		person.setName("John");
		Set<String> addressIds= new HashSet<String>(Arrays.asList(homeAddress.getId(), workAddress.getId()));

		Person savedPerson = mappingSession.save(person);
```
#### Get
-Get JPA entity by unique ID
```java
		Person result = mappingSession.get(Person.class, person.getId());
```
-Get JPA entity by query
```java
		QueryBuilder queryBuilder = new QueryBuilder().query("*").fields("Addresses.*");
		List<Person> persons = mappingSession.getByQuery(Person.class, queryBuilder); 
```
#### Delete
```java
		mappingSession.delete(person);
```

For complete examples, see [MappingSessionTest.java](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/test/java/com/dell/jpa/mapping/MappingSessionTest.java)
