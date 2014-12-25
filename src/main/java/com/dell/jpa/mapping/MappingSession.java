package com.dell.jpa.mapping;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EmbeddedId;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.DataType;
import com.datastax.driver.mapping.EntityTypeParser;
import com.datastax.driver.mapping.meta.EntityFieldMetaData;
import com.datastax.driver.mapping.meta.EntityTypeMetadata;
import com.dell.doradus.client.ApplicationSession;
import com.dell.doradus.client.Client;
import com.dell.doradus.client.QueryResult;
import com.dell.doradus.client.SpiderSession;
import com.dell.doradus.common.ApplicationDefinition;
import com.dell.doradus.common.BatchResult;
import com.dell.doradus.common.CommonDefs;
import com.dell.doradus.common.DBObject;
import com.dell.doradus.common.DBObjectBatch;
import com.dell.doradus.common.FieldDefinition;
import com.dell.doradus.common.FieldType;
import com.dell.doradus.common.TableDefinition;
import com.dell.doradus.common.Utils;
import com.dell.jpa.entity.annotation.Application;
import com.dell.jpa.entity.annotation.Link;
import com.dell.jpa.mapping.query.QueryBuilder;

/**
 * Object Mapper APIs to work with entities to be persisted in Doradus. 
 * This is lightweight wrapper for the Doradus Client. 
 */
@Repository
public class MappingSession implements IMappingSession{
	
	@Autowired
	Client client;
	
    /**
     * Persist Entity
     * @param entity
     * @return persisted entity
     */
	public <E> E save(E entity) {
		
		//retrieve Application from annotation
		Application applicationAnnotation = (Application) entity.getClass().getAnnotation(Application.class);	
		String applicationName = applicationAnnotation.name();
	
		boolean ddlAutoCreate = applicationAnnotation.ddlAutoCreate();
		String storageService = applicationAnnotation.storageService();
		String key = applicationAnnotation.key();
		
		Table tableAnnotation = (Table)entity.getClass().getAnnotation(Table.class);
		String tableName = tableAnnotation.name();
		Set<Link> linkFields = getLinkFields(entity.getClass());
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

        if (client.getAppDef(applicationName) == null) {			
        	if (ddlAutoCreate) {
        		//create new application
        		ApplicationDefinition appDef = new ApplicationDefinition();
        		appDef.setAppName(applicationName);
        		appDef.setKey(key);
        		createTable(storageService, tableName, linkFields, fields, appDef, clazz);
        		client.createApplication(appDef);
        	}
        }		
        
        ApplicationSession session = (ApplicationSession)client.openApplication(applicationName);
        TableDefinition tableDef = session.getAppDef().getTableDef(tableName);
        if (tableDef == null) {
     		createTable(storageService, tableName, linkFields, fields, session.getAppDef(), clazz);  
     		client.createApplication(session.getAppDef());
        }
        
	    DBObjectBatch objectBatch = new DBObjectBatch();
	    
	    DBObject dbObject = new DBObject();
	    dbObject.setTableName(tableName);
	    
	    for (int i = 0; i < fields.size(); i++) {
	    	if (columns[i] != null && values[i] != null) {
	    		if (fields.get(i).getDataType().equals(DataType.Name.SET)) {
	    			dbObject.addFieldValues(columns[i], (Set)values[i]);
	    		}
	    		else if (fields.get(i).getDataType().equals(DataType.Name.TIMESTAMP)) {
	    			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    				String value = format.format(new Date(values[i].toString()));
    				dbObject.addFieldValue(columns[i], value);    			
	    		}
	    		else {
	    			dbObject.addFieldValue(columns[i], values[i].toString());
	    		}
    		}
    	}    
		//Link linkAnnotation = (Link)entity.getClass().getAnnotation(Link.class);
		//String linkTableName = linkAnnotation.tableName();
		//dbObject.addFieldValue(linkAnnotation.name(), value);
		
	    objectBatch.addObject(dbObject);

		
        storageService = session.getAppDef().getStorageService();
        //persist
    	BatchResult result = ((SpiderSession)session).addBatch(tableName, objectBatch);
        if (result.isFailed()) {
        	throw new RuntimeException(result.getErrorMessage());
        }
        
        EntityFieldMetaData idField = entityMetadata.getFieldMetadata("id");
        idField.setValue(entity, result.getResultObjectIDs().iterator().next());	        
   
        return entity;
	}

	private void createTable(String storageService, String tableName,
			Set<Link> linkFields, List<EntityFieldMetaData> fields,
			ApplicationDefinition appDef, Class entityClass) {
		TableDefinition tableDef = new TableDefinition(appDef);
		tableDef.setTableName(tableName);
		tableDef.setApplication(appDef);
		appDef.addTable(tableDef);
		appDef.setOption(CommonDefs.OPT_STORAGE_SERVICE, storageService);
		for (int i = 0; i < fields.size(); i++) {    
			FieldDefinition fieldDef = new FieldDefinition(tableDef);	 
			if (FieldDefinition.isValidFieldName(fields.get(i).getColumnName())) {
				switch (fields.get(i).getDataType()) {
		    		case BOOLEAN:
		    			fieldDef.setName(fields.get(i).getColumnName());
		    			fieldDef.setType(FieldType.BOOLEAN);
		    			break;
		    		case INT:
		    			fieldDef.setName(fields.get(i).getColumnName());
		    			fieldDef.setType(FieldType.INTEGER);
		    			break;
		    		case TEXT:
		    			if (!setLinkField(fields.get(i).getName(), fieldDef, entityClass)) {
			    			fieldDef.setName(fields.get(i).getColumnName());
		    				fieldDef.setType(FieldType.TEXT);
		    			}
		    			break;
		    		case TIMESTAMP:
		    			fieldDef.setName(fields.get(i).getColumnName());
		    			fieldDef.setType(FieldType.TIMESTAMP);
		    			break;
		    		default:
		    			break;
		    		}
				
				tableDef.addFieldDefinition(fieldDef);		
		    }
		}
	}

	private boolean setLinkField(String name, FieldDefinition fieldDef, Class entityClass) {
		Field[] fields = entityClass.getDeclaredFields(); 
		for (Field f: fields) {
			Link linkAnnotation = f.getAnnotation(Link.class) ;
			if (linkAnnotation!= null) {
				if (name.equals(linkAnnotation.fieldName())) {
					fieldDef.setName(linkAnnotation.name());
					fieldDef.setType(FieldType.LINK);
					fieldDef.setCollection(true);
					fieldDef.setLinkInverse(linkAnnotation.inverseName());
					fieldDef.setLinkExtent(linkAnnotation.tableName());				
					return true;
				}
				
			}
		}
		return false;

	}

	private Set<Link> getLinkFields(Class entityClass) {
		Set<Link>linkFields = new HashSet<Link>();		
		Field[] fields = entityClass.getDeclaredFields(); 
		for (Field f: fields) {
			if (f.getAnnotation(Link.class) != null) {
				linkFields.add(f.getAnnotation(Link.class));
			}
		}
		return linkFields;
	}
	
    /**
     * Delete Entity
     * @param entity
     */
	public <E> void delete(E entity) {
		//retrieve Application from annotation
		Application applicationAnnotation = (Application) entity.getClass().getAnnotation(Application.class);	
		String applicationName = applicationAnnotation.name();
	
		Table tableAnnotation = (Table)entity.getClass().getAnnotation(Table.class);
		String tableName = tableAnnotation.name();
		
	   	Class<?> clazz = entity.getClass();
        EntityTypeMetadata entityMetadata = EntityTypeParser.getEntityMetadata(clazz);
        
        EntityFieldMetaData idField = entityMetadata.getFieldMetadata("id");
        idField.getValue(entity);
        
        DBObjectBatch objectBatch = new DBObjectBatch();
        DBObject dbObj1 = new DBObject();
        dbObj1.setObjectID((String)idField.getValue(entity));
        dbObj1.setTableName(tableName);
        dbObj1.setDeleted(true);
        objectBatch.addObject(dbObj1);
        ApplicationSession session = (ApplicationSession)client.openApplication(applicationName);
        BatchResult batchResult = session.deleteBatch(tableName, objectBatch);
    	
        if (batchResult.isFailed()) {
        	throw new RuntimeException(batchResult.getErrorMessage());
        }
	}
	/**
     * Get Entity by Id(Primary Key)
     * 
     * @param class Entity.class
     * @param id primary key
     * @return Entity instance or null
    */
    public <T> T get(Class<T> entityClass, String id) {
    	
    	Application applicationAnnotation = (Application) entityClass.getAnnotation(Application.class);	
    	//retrieve Application from annotation
		String applicationName = applicationAnnotation.name();

		Table tableAnnotation = (Table)entityClass.getAnnotation(Table.class);
		String tableName = tableAnnotation.name();
		
        ApplicationSession session = (ApplicationSession)client.openApplication(applicationName);
        String storageService = session.getAppDef().getStorageService();
        
        if (storageService.startsWith("Spider")) {
        	DBObject dbObject = ((SpiderSession)session).getObject(tableName, (String)id);
            return createEntityObjectFromDBObject(entityClass, dbObject);    
         
        }
        return null;	         
    }
   	
    
    public <T> List<T> getByQuery(Class<T> entityClass, QueryBuilder query) {
      	Application applicationAnnotation = (Application) entityClass.getAnnotation(Application.class);	
    	//retrieve Application from annotation
		String applicationName = applicationAnnotation.name();

		Table tableAnnotation = (Table)entityClass.getAnnotation(Table.class);
		String tableName = tableAnnotation.name();
		
        ApplicationSession session = (ApplicationSession)client.openApplication(applicationName);
        
        
		QueryResult queryResult = session.objectQuery(tableName, query.toMap());
        Collection<DBObject> dbObjectList =  queryResult.getResultObjects();  
        
        List<T> entityList = new ArrayList<T>();
        for (DBObject dbObject: dbObjectList) {
        	entityList.add(createEntityObjectFromDBObject(entityClass, dbObject));
        }
    	return entityList;
    }
    
    
	private <T> T createEntityObjectFromDBObject(Class<T> entityClass,
			DBObject dbObject) {
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
	private Object getValueObjectFromDBObject(DBObject dbObject, EntityFieldMetaData field) {
		Object value = null;
		List<String> fieldValues = null;
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
						value = Utils.parseDate(dateField).getTime();
					}
				} catch (Exception e) {
					value = null;
				}
                break;
            case INT:
    			try {
					String intField = dbObject.getFieldValue(field.getColumnName());
					if (intField != null) {
						value = Integer.valueOf(dbObject.getFieldValue(field.getColumnName()));
					}
				} catch (Exception e) {
					value = null;
				}
                break;
            case BIGINT:
      			try {
    				String longField = dbObject.getFieldValue(field.getColumnName());
    				if (longField != null) {
    					value = Long.valueOf(dbObject.getFieldValue(field.getColumnName()));
    				}
				} catch (Exception e) {
					value = null;
				}
                break;    
            case DOUBLE:
      			try {
    				String doubleField = dbObject.getFieldValue(field.getColumnName());
    				if (doubleField != null) {
    					value = Double.valueOf(dbObject.getFieldValue(field.getColumnName()));
    				}
				} catch (Exception e) {
					value = null;
				}            	
               break;
            case FLOAT:
      			try {
    				String floatField = dbObject.getFieldValue(field.getColumnName());
    				if (floatField != null) {
    					value = Float.valueOf(dbObject.getFieldValue(field.getColumnName()));
    				}
				} catch (Exception e) {
					value = null;
				}                 	
               break;
            case LIST:
                if (value == null) {
                    value = new ArrayList<Object>();
                }
            	fieldValues = dbObject.getFieldValues(field.getColumnName());
             	if (fieldValues != null && !fieldValues.isEmpty()) {
                    ((List<Object>) value).addAll(fieldValues);
                }
                break;
            case SET:
                if (value == null) {
                    value = new HashSet<Object>();
                }
            	fieldValues = dbObject.getFieldValues(field.getColumnName());
            	if (fieldValues != null && !fieldValues.isEmpty()) {
	                ((Set<Object>) value).addAll(new HashSet<String>(fieldValues));
                }
                break;
            default:
            	dbObject.getFieldValue(field.getColumnName());
                break;
        }
		return value;
	}	
    


  
}
