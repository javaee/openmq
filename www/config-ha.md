# Starting and Configuring Open Message Queue 4.1 HA Cluster

	<LI>
	  <P>Install Open Message Queue 4.1 on each of the machine that you want to setup
	as part of the Message Queue HA Cluster</P>
	<LI><P>Use the imqbrokerd command to start a broker. The first time
	a broker instance is run, a config.properties file is automatically
	created. You can edit this instance configuration file to configure
	the HA cluster configuration properties.<BR><BR>$TOP/mq/bin/imqbrokerd
	-tty</P>
	<LI><P>Shutdown the broker<BR><BR>Ctrl-C in the terminal window in
	which you ran the imqbrokerd command</P>
	<LI><P>Edit the config.properties file to configure HA cluster
	configuration properties:</P>
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
					<P><B>imq.cluster.ha</B></P>
				</TD>
				<TD WIDTH=359>
					<P><FONT SIZE=3>High Availability mode must be turned
					on<BR><BR><B><I>imq.cluster.ha=true</I></B></FONT></P>
				</TD>
			</TR>
			<TR VALIGN=TOP>
				<TD WIDTH=380>
					<P><B>imq.cluster.clusterid</B></P>
				</TD>
				<TD WIDTH=359>
					<P><FONT SIZE=3>Cluster name which is the <SPAN STYLE="font-weight: medium"><I>ID</I></SPAN>
					associated with all brokers in the HA cluster; this id must be
					set the same on all brokers in the HA cluster</FONT></P>
				</TD>
			</TR>
			<TR VALIGN=TOP>
				<TD WIDTH=380>
					<P><B>imq.brokerid</B></P>
				</TD>
				<TD WIDTH=359>
					<P><FONT SIZE=3>Broker's unique ID; this ID must be unique to
					this broker in the HA cluster</FONT></P>
				</TD>
			</TR>
			<TR VALIGN=TOP>
				<TD WIDTH=380>
					<P><B>imq.persist.store</B></P>
				</TD>
				<TD WIDTH=359>
					<P>Specifies type of  persistence store; only JDBC-based data
					store is supported<BR><BR><B><I>imq.persist.store=jdbc</I></B></P>
				</TD>
			</TR>
			<TR VALIGN=TOP>
				<TD WIDTH=380>
					<P><B>imq.persist.jdbc.dbVendor</B></P>
				</TD>
				<TD WIDTH=359>
					<P>Database vendor (hadb, mysql);<BR><BR>hadb &ndash; Sun Java
					System High Availability Database<BR>mysql &ndash; MySQL 4.1
					Database<BR><BR>Example: <B><I>imq.persist.jdbc.dbVendor=hadb</I></B><BR><BR>Note:
					Select HADB if you're currently using Sun Java Enterprise System
					and already have HADB installed.</P>
				</TD>
			</TR>
		</TBODY>
	</TABLE>
	<P><BR><BR>Additional configuration properties for HADB database:</P>
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
	<P><BR><BR>Additional configuration properties for MySQL database:</P>
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
	<P></P>
	<LI><P>Copy your JDBC driver jar file to the following
	path:<BR><BR>$TOP/mq/lib/ext/</P>
	<LI><P>Repeat steps 2-5 for each broker instance in the HA cluster.
	Ensure that the cluster ID is the same for all brokers in the HA
	cluster and the broker ID is unique for each broker in the HA
	cluster.</P>
	<LI><P>Use the imqdbmgr command to create the database
	schema.<BR><BR>$TOP/mq/bin/imqdbmgr create tbl<BR><BR>Note: You'll
	only need to run this command once with any of the machine that is
	part of the HA cluster because all brokers in an HA cluster shared
	the same persistent store.</P>
	<LI><P>Starting the brokers in the HA cluster<BR><BR>For each broker
	in the HA cluster, use the imqbrokerd command to start a broker (see
	step 1). When brokers are started they will automatically register
	themselves into the HA cluster.</P>
</OL>

