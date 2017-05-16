# Starting and Configuring an Open Message Queue 4.1 HA Cluster

1\. Install Open Message Queue 4.1 on each of the machine that you want to setup as part of the Message Queue HA Cluster

2\. Use the `imqbrokerd` command to start a broker. 

`$TOP/mq/bin/imqbrokerd -tty`

The first time a broker instance is run, an instance configuration file `config.properties` is automatically created. 
You can then edit this file to configure the HA cluster configuration properties.
	
3\. Shutdown the broker by pressing Ctrl-C in the terminal window in which you ran the `imqbrokerd` command

4\. Edit the file `config.properties` file to configure HA cluster configuration properties:

| :---         | :---      | 
| **Property name**   | **Description** |
| :---         | :---      |
| `imq.cluster.ha` | High Availability mode must be turned on `imq.cluster.ha=true` | 
| `imq.cluster.clusterid` | Cluster name which is the ID associated with all brokers in the HA cluster; this id must be set the same on all brokers in the HA cluster
| `imq.brokerid` | Broker's unique ID; this ID must be unique to this broker in the HA cluster | 
| `imq.persist.store` | Specifies type of  persistence store; only JDBC-based data store is supported `imq.persist.store=jdbc`|
| `imq.persist.jdbc.dbVendor` | Database vendor. Set to `hadb` (Sun Java System High Availability Database) or `mysql` (MySQL 4.1 Database). Select `hadb` if you're currently using Sun Java Enterprise System and already have HADB installed. | 

| :---         | :---      | 
| **Additional configuration properties for HADB** |
| **Property name**   | **Description** |
| :---         | :---      |
| `imq.persist.jdbc.hadb.user` | Specifies user's account name |
| `imq.persist.jdbc.hadb.password` | Specifies user's password |
| `imq.persist.jdbc.hadb.property.serverList` | Specifies the JDBC URL of the HADB. Use the command `hadbm get JdbcUrl` |
| `imq.persist.jdbc.hadb.property.serverList` | Specifies the JDBC URL of the HADB. Use the command `hadbm get JdbcUrl`, remove the `jdbc:sun:hadb` prefix and use the `host:port,host:port...` as the	value for the serverList property. |
	
| :---         | :---      | 
| **Additional configuration properties for MySQL** |
| **Property name**   | **Description** |
| :---         | :---      |
| `imq.persist.jdbc.mysql.user` | Specifies user's account name |
| `imq.persist.jdbc.mysql.password` | Specifies user's password | 
| `imq.persist.jdbc.mysql.property.url` | Specifies the JDBC URL to open the database | 

5\. Copy your JDBC driver jar file to the following path:
    `$TOP/mq/lib/ext/`

6\. Repeat steps 2-5 for each broker instance in the HA cluster. Ensure that the cluster ID is the same for all brokers in the HA cluster and the broker ID is unique for each broker in the HA cluster.

7\. Use the `imqdbmgr` command to create the database schema.
    `$TOP/mq/bin/imqdbmgr create tbl

8\. Note: You'll only need to run this command once with any of the machine that is part of the HA cluster because all brokers in an HA cluster shared the same persistent store.

9\. Starting the brokers in the HA cluster. For each broker in the HA cluster, use the `imqbrokerd` command to start a broker (see step 1). When brokers are started they will automatically register themselves into the HA cluster.


