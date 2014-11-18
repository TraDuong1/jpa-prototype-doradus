Sample “JPA Light” for Doradus Project
======================================

The purpose of this project is to demonstrate how to utilize JPA for Doradus, to some extent.  
### You can take a look at the sample test 

- [MappingSessionTest](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/test/java/com/dell/jpa/mapping/MappingSessionTest.java) to see how to persist JPA entity into Doradus and retrieve it using Object Mapper APIs [MappingSession](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/main/java/com/dell/jpa/mapping/MappingSession.java)
- With support of custom annotations that allows auto-create schema if it doesn’t exist, Application and Table can be created 1st time saving the entity. See attributes defined in [Application annotation](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/main/java/com/dell/jpa/entity/NewEntity.java)

### To get the tests run.
You will need:
- [Apache Maven](http://maven.apache.org/download.cgi)
- [Doradus](https://github.com/dell-oss/Doradus) instance running on your localhost (or change [doradus.properties](https://github.com/TraDuong1/jpa-prototype-doradus/blob/master/src/test/resources/doradus.properties)).
- Java 7 and up.



