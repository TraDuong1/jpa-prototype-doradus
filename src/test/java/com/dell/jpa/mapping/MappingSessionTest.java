package com.dell.jpa.mapping; 


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dell.doradus.client.utils.HelloSpider;
import com.dell.doradus.common.ApplicationDefinition;
import com.dell.jpa.entity.Address;
import com.dell.jpa.entity.Movies;
import com.dell.jpa.entity.Person;
import com.dell.jpa.mapping.query.QueryBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")

public class MappingSessionTest {
	
	@Autowired
	MappingSession mappingSession = null;
	
	@Before
	public void setup() {
		//wipe out and recreate HelloSpider application before test
		ApplicationDefinition appDef = mappingSession.client.getAppDef("HelloSpider");
		if (appDef != null) {
			mappingSession.client.deleteApplication("HelloSpider", "Arachnid");
		}
		String[] args = {"localhost","1123"};		
		HelloSpider.main(args);
		//wipe out TestApplication with Person table before test
		appDef = mappingSession.client.getAppDef("TestApplication");
		if (appDef != null) {
			mappingSession.client.deleteApplication("TestApplication", "TestKey");
		}		
	}
	
	@Test
	public void testPersistAndRetrieveEntityWithSchemaExisted() throws ParseException {	
	
		Movies entity = new Movies();
		entity.setName("Spririted Away");
		entity.setDirector("Hayao Miyazaki");
		entity.setBudget(100);
		entity.setCancelled(false);
		Set<String> leads = new HashSet<String>(Arrays.asList("Haku,", "Rin"));
		entity.setLeads(leads);
		entity.setReleaseDate(new Date());
		
		//test persist
		assertNull(entity.getId());
		
		Movies savedObject = mappingSession.save(entity);
		assertNotNull(entity.getId());		
		assertNotNull(savedObject.getId());
		
		//test get
		Movies result = mappingSession.get(Movies.class, entity.getId());
		assertNotNull(result.getId());		
		assertEquals(entity.getName(), result.getName());
		assertEquals(entity.getDirector(), result.getDirector());
		assertEquals(entity.getBudget(), result.getBudget());
		assertEquals(entity.isCancelled(), result.isCancelled());
		assertEquals(entity.getLeads().size(), result.getLeads().size());	
		assertNotNull(result.getReleaseDate());
		

		//test getByQuery
		QueryBuilder queryBuilder = new QueryBuilder().query("*");
		List<Movies> movies = mappingSession.getByQuery(Movies.class, queryBuilder); 
		assertFalse(movies.isEmpty());
		assertNotNull(movies.get(0).getId());
		
		queryBuilder = new QueryBuilder().query("*").pageSize(10).fields("Director,Leads").sortOrder("Director");
		movies = mappingSession.getByQuery(Movies.class, queryBuilder); 
		assertFalse(movies.isEmpty());
		if (movies.size() >=2) {
			Movies first = movies.get(0);	
			Movies last = movies.get(movies.size()-1);
			if (first.getDirector()!=null && last.getDirector()!=null) {
				assertTrue(first.getDirector().compareTo(last.getDirector()) <=0);
			}
		}

	
	}
	
	@Test
	public void testPersistAndRetrieveEntityWithSchemaNotExisted() throws ParseException {	
//		//persist Rating 
//		Rating rating = new Rating();
//		rating.setRating("B-");
//		mappingSession.save(rating);
		
		//persist Address
		Address address = new Address();
		address.setStreet("111 Main St");
		address.setCity("AV");
		address.setState("CA");
		address.setZip("92656");
		mappingSession.save(address);
		
		Person person = new Person();
		person.setAge(40);
		person.setName("John");
		person.setAddressId(address.getId());
		
		//test persist
		assertNull(person.getId());		
		Person savedObject = mappingSession.save(person);
		assertNotNull(person.getId());		
		assertNotNull(savedObject.getId());
		
		Person person2 = new Person();
		person2.setAge(45);
		person2.setName("Marry");
		
		mappingSession.save(person2);
		assertNotNull(person2.getId());		
		
		//test retrieval
		Person result = mappingSession.get(Person.class, person.getId());
		assertNotNull(result.getId());		
		assertEquals(person.getName(), result.getName());
		assertEquals(person.getAge(), result.getAge());	
		
		//verify Link object result
		Address addressResult = mappingSession.get(Address.class, person.getAddressId());
		assertNotNull(addressResult.getState());
				
		boolean hasLinkObject = false;
		QueryBuilder queryBuilder = new QueryBuilder().query("*").fields("Addresses.*");
		List<Person> persons = mappingSession.getByQuery(Person.class, queryBuilder); 
		for (Person eachPerson: persons) {
			if (eachPerson.getAddressId() != null) {
				hasLinkObject = true;
			}
		}
		assertTrue(hasLinkObject);  
	}	

	@Test
	public void testDelete() {
		Movies entity = new Movies();
		entity.setName("Foo");
		entity.setDirector("Bar");
		entity.setBudget(100);
		
		Movies savedObject = mappingSession.save(entity);	
		Movies result = mappingSession.get(Movies.class, savedObject.getId());
		assertNotNull(result.getId());
		assertNotNull(result.getName());
		
		//test Delete
		mappingSession.delete(savedObject);
		result = mappingSession.get(Movies.class, savedObject.getId());
		assertNull(result.getName());
	}
	
}
