package com.dell.jpa.mapping; 

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dell.jpa.entity.Movies;
import com.dell.jpa.mapping.MappingSession;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")

public class MappingSessionTest {
	
	@Autowired
	MappingSession mappingSession = null;
	

	@Test
	public void testPersitEntityWithSchemaExisted() throws ParseException {	
		
		Movies entity = new Movies();
		entity.setName("My Totoro");
		entity.setDirector("Hayao Miyazaki");
		entity.setBudget(100);
		entity.setCancelled(false);
		Set<String> leads = new HashSet<String>(Arrays.asList("Haku,", "Rin"));
		entity.setLeads(leads);
		
		//test persist
		assertNull(entity.getId());
		
		Movies savedObject = mappingSession.save(entity);
		assertNotNull(entity.getId());		
		assertNotNull(savedObject.getId());
		
		//test retrieval
		Movies result = mappingSession.get(Movies.class, entity.getId());
		assertNotNull(result.getId());		
		assertEquals(entity.getName(), result.getName());
		assertEquals(entity.getDirector(), result.getDirector());
		assertEquals(entity.getBudget(), result.getBudget());
		assertEquals(entity.isCancelled(), result.isCancelled());
		assertEquals(entity.getLeads().size(), result.getLeads().size());		
	}
}
