# Starting and Configuring an Open Message Queue 4.1 HA Cluster

* Install Open Message Queue 4.1 on each of the machine that you want to setup as part of the Message Queue HA Cluster

* Use the `imqbrokerd` command to start a broker. 
The first time a broker instance is run, a config.properties file is automatically created. 
You can edit this instance configuration file to configure the HA cluster configuration properties.

    `$TOP/mq/bin/imqbrokerd -tty`
	
* Shutdown the broker by pressing Ctrl-C in the terminal window in which you ran the `imqbrokerd` command

* Edit the `config.properties` file to configure HA cluster configuration properties:

| :---         | :---      | 
| **Property name**   | **Description** |
| :---         | :---      |
| `imq.cluster.ha` | High Availability mode must be turned on `imq.cluster.ha=true` | 
| `imq.cluster.clusterid` | Cluster name which is the ID associated with all brokers in the HA cluster; this id must be set the same on all brokers in the HA cluster
| `imq.brokerid` | Broker's unique ID; this ID must be unique to this broker in the HA cluster | 
| `imq.persist.store` | Specifies type of  persistence store; only JDBC-based data store is supported
`imq.persist.store=jdbc`|
| `imq.persist.jdbc.dbVendor` | Database vendor (hadb, mysql)
hadb - Sun Java System High Availability Database
mysql - MySQL 4.1 Database
Example: `imq.persist.jdbc.dbVendor=hadb`
Note: Select HADB if you're currently using Sun Java Enterprise System and already have HADB installed. | 

Additional configuration properties for HADB database:

<TABLE WIDTH=766 BORDER=1 CELLPADDING=4 CELLSPACING=3 STYLE="page-break-inside: avoid">
	<COL WIDTH=380>
	<COL WIDTH=359>
	<THEAD>
		<TR VALIGN=TOP>
			<TH WIDTH=380>
				<P>Property Name</P>
			</TH>
			<TH WIDTH=359>
				<P>Description</P>
			</TH>
		</TR>
	</THEAD>
	<TBODY>
		<TR VALIGN=TOP>
			<TD WIDTH=380>
				<P><B>imq.persist.jdbc.hadb.user</B></P>
			</TD>
			<TD WIDTH=359>
				<P>Specifies user's account name</P>
			</TD>
		</TR>
		<TR VALIGN=TOP>
			<TD WIDTH=380>
				<P><B>imq.persist.jdbc.hadb.password</B></P>
			</TD>
			<TD WIDTH=359>
				<P><FONT SIZE=3>Specifies user's password</FONT></P>
			</TD>
		</TR>
		<TR VALIGN=TOP>
			<TD WIDTH=380>
				<P><B>imq.persist.jdbc.hadb.property.serverList</B></P>
			</TD>
			<TD WIDTH=359>
				<P><FONT SIZE=3>Specifies the JDBC URL of the HADB. Use the
				command &quot;hadbm get JdbcUrl&quot;; remove the <I>jdbc:sun:hadb
				</I>prefix and use the <I>host:port,host:port...</I> as the
				value for the serverList property.</FONT></P>
			</TD>
		</TR>
	</TBODY>
</TABLE>
	
## Additional configuration properties for MySQL database:

<TABLE WIDTH=766 BORDER=1 CELLPADDING=4 CELLSPACING=3 STYLE="page-break-inside: avoid">
	<COL WIDTH=380>
	<COL WIDTH=359>
	<THEAD>
		<TR VALIGN=TOP>
			<TH WIDTH=380>
				<P>Property Name</P>
			</TH>
			<TH WIDTH=359>
				<P>Description</P>
			</TH>
		</TR>
	</THEAD>
	<TBODY>
		<TR VALIGN=TOP>
			<TD WIDTH=380>
				<P><B>imq.persist.jdbc.mysql.user</B></P>
			</TD>
			<TD WIDTH=359>
				<P>Specifies user's account name</P>
			</TD>
		</TR>
		<TR VALIGN=TOP>
			<TD WIDTH=380>
				<P><B>imq.persist.jdbc.mysql.password</B></P>
			</TD>
			<TD WIDTH=359>
				<P><FONT SIZE=3>Specifies user's password</FONT></P>
			</TD>
		</TR>
		<TR VALIGN=TOP>
			<TD WIDTH=380>
				<P><B>imq.persist.jdbc.mysql.property.url</B></P>
			</TD>
			<TD WIDTH=359>
				<P><FONT SIZE=3>Specifies the JDBC URL to open the database</FONT></P>
			</TD>
		</TR>
	</TBODY>
</TABLE>

* Copy your JDBC driver jar file to the following path:
` $TOP/mq/lib/ext/`

* Repeat steps 2-5 for each broker instance in the HA cluster.
Ensure that the cluster ID is the same for all brokers in the HA
cluster and the broker ID is unique for each broker in the HA
cluster.

* Use the `imqdbmgr` command to create the database schema.
`$TOP/mq/bin/imqdbmgr create tbl

* Note: You'll
	only need to run this command once with any of the machine that is
	part of the HA cluster because all brokers in an HA cluster shared
	the same persistent store.

* Starting the brokers in the HA cluster<BR><BR>For each broker
in the HA cluster, use the imqbrokerd command to start a broker (see
step 1). When brokers are started they will automatically register
themselves into the HA cluster.


