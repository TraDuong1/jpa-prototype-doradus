package com.dell.jpa.mapping;

import java.util.List;

import com.dell.jpa.mapping.query.QueryBuilder;

/**
 * MappingSession APIs
 *
 */
public interface IMappingSession {
	
    /**
     * Persist Entity
     * @param entity
     * @return persisted entity
     */	
	public <E> E save(E entity);
	
	
    /**
     * Delete Entity
     * @param entity
     */	
	public <E> void delete(E entity);
	
	/**
     * Get Entity by Id(Primary Key)
     * 
     * @param class Entity.class
     * @param id primary key
     * @return Entity instance or null
    */
    public <T> T get(Class<T> entityClass, String id);
    
    
    public <T> List<T> getByQuery(Class<T> entityClass, QueryBuilder query) ;
}
