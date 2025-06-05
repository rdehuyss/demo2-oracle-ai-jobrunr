## Demo Oracle and JobRunr

### Goal of this demo / business case
The purpose of this demo is to leverage Oracle using ALL_MINILM_L12_V2 to find 
support tickets that are similar to newly opened tickets so support engineers 
can quickly diagnose tickets and help customers. 

### Architecture
- we have a table in Oracle called TICKETS with the following columns:
  `ID`, `VERSION`, `SUBJECT`, `DESCRIPTION`, `RESOLUTION`, `STATUS`, `EMBEDDING`
- Users can create tickets using a Spring Boot Rest API called [TicketController](./src/main/java/org/jobrunr/demo/tickets/TicketController.java).
  We simulate creating these tickets using the [Intellij HTTP client](./src/test/demo.http)
- When a ticket is closed, an asynchronous JobRunr job will be launched to update embeddings for the ticket so 
  if a new ticket comes in, the support engineers can quickly find similar tickets
- Each night a recurring JobRunr job will be launched create a summary of all tickets that were opened and resolved


### Current state
Please note that the code is currently in DRAFT / WIP.

- we can:
  - list all tickets (either open or closed)
  - we can create a ticket
  - we can resolve a ticket => error happens here
  - we can view a single ticket and find (hopefully 😀) related tickets

### How to run:
- Download the [`all_MiniLM_L12_v2` model](https://adwc4pm.objectstorage.us-ashburn-1.oci.customer-oci.com/p/VBRD9P8ZFWkKvnfhrWxkpPe8K03-JIoM5h_8EJyJcpE80c108fuUjg7R5L5O7mMZ/n/adwc4pm/b/OML-Resources/o/all_MiniLM_L12_v2_augmented.zip) and save it to [src/main/resources/model](src/main/resources/model)
  as described in found in Oracle Blog post https://blogs.oracle.com/machinelearning/post/use-our-prebuilt-onnx-model-now-available-for-embedding-generation-in-oracle-database-23ai
- Run the Spring Boot main method [DemoOracleAiJobRunrApplication](./src/main/java/org/jobrunr/demo/DemoOracleAiJobRunrApplication.java)
  - this will use `spring-boot-docker-compose` to automatically start Oracle and load the `all_MiniLM_L12_v2` onnx model
  - it will also automatically create a `TICKETS` table
- Then run the steps in [Intellij HTTP client](./src/test/demo.http)
  - List all tickets (will be empty as no tickets created)
  - Create a new ticket 
  - View the new ticket (there will be no similar tickets yet)
  - Resolve the new ticket → this does not work. The embeddings can't be updated even if I try to do it in Oracle itself. See:
    - [TicketService#closeTicket(UUID, String) line 33](./src/main/java/org/jobrunr/demo/tickets/TicketService.java)
    - [TicketRepository#updateEmbedding(UUID, double[]) line 52](./src/main/java/org/jobrunr/demo/tickets/TicketRepository.java)
    - [TicketRepository#updateEmbeddingV2(UUID, String) line 56](./src/main/java/org/jobrunr/demo/tickets/TicketRepository.java)

### Exception
The exception I have is that in the `EMBEDDING` column of the `TICKETS` table, I see the following:
```java
java.sql.SQLException: ORA-17004: Invalid column type: JDBC 4.3 does not specify a default conversion for VECTOR. A default conversion may be configured using the  "oracle.jdbc.vectorDefaultGetObjectType" connection property
https://docs.oracle.com/error-help/db/ora-17004/
	at oracle.jdbc.driver.SQLStateMapping$SqlExceptionType$1.newInstance(SQLStateMapping.java:58)
	at oracle.jdbc.driver.SQLStateMapping.newSQLException(SQLStateMapping.java:169)
	at oracle.jdbc.driver.DatabaseError.newSQLException(DatabaseError.java:175)
	at oracle.jdbc.driver.DatabaseError.createSqlException(DatabaseError.java:225)
	at oracle.jdbc.driver.DatabaseError.createSqlException(DatabaseError.java:298)
	at oracle.jdbc.driver.DatabaseError.createSqlException(DatabaseError.java:319)
	at oracle.jdbc.driver.VectorAccessor.getObject(VectorAccessor.java:180)
	at oracle.jdbc.driver.GeneratedStatement.getObject(GeneratedStatement.java:196)
	at oracle.jdbc.driver.GeneratedScrollableResultSet.getObject(GeneratedScrollableResultSet.java:322)
	in JdbcHelperImpl.getObject(JdbcHelperImpl.java:345)

``` 


### Strange?
When I run the `UPDATE TICKETS` sql statement from [test.sql](./src/test/test.sql), I would have thought that the update would be completely handled in 
Oracle, yet also then I see the same exception. 