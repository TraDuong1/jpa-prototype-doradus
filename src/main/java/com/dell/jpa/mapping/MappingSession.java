package com.dell.jpa.mapping;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.DataType;
import com.datastax.driver.mapping.EntityTypeParser;
import com.datastax.driver.mapping.meta.EntityFieldMetaData;
import com.datastax.driver.mapping.meta.EntityTypeMetadata;
import com.dell.doradus.client.ApplicationSession;
import com.dell.doradus.client.Client;
import com.dell.doradus.client.SpiderSession;
import com.dell.doradus.common.DBObject;
import com.dell.doradus.common.DBObjectBatch;
import com.dell.doradus.common.ObjectResult;
import com.dell.jpa.entity.annotation.Application;

/**
 * Object Mapper APIs to work with entities to be persisted in Doradus. 
 * This is lightweight wrapper for the Doradus Client. 
 */
@Repository
public class MappingSession {
	
	@Autowired
	Client client;
	
	
	/**
     * Get Entity by Id(Primary Key)
     * 
     * @param class Entity.class
     * @param id primary key
     * @return Entity instance or null
    */
    public <T> T get(Class<T> entityClass, String id) {
    	
    	Application applicationAnnotation = (Application) entityClass.getAnnotation(Application.class);	
		String application = applicationAnnotation.name();
		
		Table tableAnnotation = (Table)entityClass.getAnnotation(Table.class);
		String tableName = tableAnnotation.name();
		
        ApplicationSession session = (ApplicationSession)client.openApplication(application);
        String storageService = session.getAppDef().getStorageService();
        
        if (storageService.startsWith("Spider")) {
        	DBObject dbObject = ((SpiderSession)session).getObject(tableName, (String)id);
            T entity = null;
			try {
				entity = entityClass.newInstance();
            
	            EntityTypeMetadata entityMetadata = EntityTypeParser.getEntityMetadata(entityClass);
	            
	            // set properties' values
	            for (EntityFieldMetaData field : entityMetadata.getFields()) {
	            	Object value = getValueObjectFromDBObject(dbObject, field);
	            	if (value != null) {
	            		field.setValue(entity, value);
	            	}
	            }
	            return entity;    
			}  
	        catch (Exception e) {
	        	e.printStackTrace();
	            return null;
			}    
         
        }
        return null;
         
    }
    
    /**
     * Persist Entity
     * @param entity
     * @return persisted entity
     */
	public <E> E save(E entity) {
		
		//retrieve Application name from annotation
		Application applicationAnnotation = (Application) entity.getClass().getAnnotation(Application.class);	
		String application = applicationAnnotation.name();
		Table tableAnnotation = (Table)entity.getClass().getAnnotation(Table.class);
		String tableName = tableAnnotation.name();
		
    	Class<?> clazz = entity.getClass();
        EntityTypeMetadata entityMetadata = EntityTypeParser.getEntityMetadata(clazz);
        
        List<EntityFieldMetaData> fields = entityMetadata.getFields();
        String[] columns = new String[fields.size()];
        Object[] values = new Object[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            EntityFieldMetaData f = fields.get(i);
            columns[i] = f.getColumnName();
            values[i] = f.getValue(entity);
        }     


        ApplicationSession session = (ApplicationSession)client.openApplication(application);
	    DBObjectBatch objectBatch = new DBObjectBatch();
	    
	    DBObject dbObject = new DBObject();
	    dbObject.setTableName(tableName);
	    
	    for (int i = 0; i < fields.size(); i++) {
	    	if (columns[i] != null && values[i] != null) {
	    		if (fields.get(i).getDataType().equals(DataType.Name.SET)) {
	    			dbObject.addFieldValues(columns[i], (Set)values[i]);
	    		}
	    		else {
	    			dbObject.addFieldValue(columns[i], values[i].toString());
	    		}
    		}
    	}    
	    objectBatch.addObject(dbObject);
	        
        String storageService = session.getAppDef().getStorageService();
        if (storageService.startsWith("Spider")) {
	        //persist
        	ObjectResult result = ((SpiderSession)session).addObject(tableName, dbObject);
	        if (result.isFailed()) {
	        	throw new RuntimeException(result.getErrorMessage());
	        }
	        EntityFieldMetaData idField = entityMetadata.getFieldMetadata("id");
	        idField.setValue(entity, result.getObjectID());	        
        }  
  
        return entity;
	}
	
	private Object getValueObjectFromDBObject(DBObject dbObject, EntityFieldMetaData field) {
		Object value = null;
        DataType.Name dataType = field.getDataType();
        switch (dataType) {
  
            case BOOLEAN:
                value = Boolean.valueOf(dbObject.getFieldValue(field.getColumnName()));
                break;
            case TEXT:
                value = dbObject.getFieldValue(field.getColumnName());
                break;
            case TIMESTAMP:
				try {
					String dateField = dbObject.getFieldValue(field.getColumnName());
					if (dateField != null) {
						value = (new SimpleDateFormat("dd-MMM-yy")).parse(dateField);
					}
				} catch (ParseException e) {
					value = null;
				}
                break;
            case INT:
                value = Integer.valueOf(dbObject.getFieldValue(field.getColumnName()));
                break;
            case BIGINT:
                value = Long.valueOf(dbObject.getFieldValue(field.getColumnName()));
                break;    
            case DOUBLE:
                value = Double.valueOf(dbObject.getFieldValue(field.getColumnName()));
                break;
            case FLOAT:
                value = Float.valueOf(dbObject.getFieldValue(field.getColumnName()));
                break;
            case LIST:
                if (value == null) {
                    value = new ArrayList<Object>();
                }
                List<String> lst = dbObject.getFieldValues(field.getColumnName());
                if (!lst.isEmpty()) {
                    ((List<Object>) value).addAll(lst);
                }
                break;
            case SET:
                if (value == null) {
                    value = new HashSet<Object>();
                }
                Set<String> set = new HashSet<String>(dbObject.getFieldValues(field.getColumnName()));
                if (!set.isEmpty()) {
                    ((Set<Object>) value).addAll(set);
                }
                break;
            default:
            	dbObject.getFieldValue(field.getColumnName());
                break;
        }
		return value;
	}	
    


  
}
