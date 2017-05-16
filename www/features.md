<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Open MQ: Feature Matrix</title>
<link rel="stylesheet" media="all" type="text/css" href="style.css" />
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>About Open MQ</title>
<!-- hide the project info -->
<style type="text/css">
#projecthome .axial { display: none; }
#apphead h1 { display: none; }
#longdescription { border: none; }
#longdescription h2 { display: none; }
#customcontent h2 { display: block; }
#bodycol .tasknav { display: none; }
</style>

</head>

<body>

<table border="0" cellspacing="0" cellpadding="0" id="main2" width="100%">
<tr valign="bottom" class="nav1">
<td nowrap colspan="3"><!-- tab 4--></td>
</tr>
<form><tr class="sub1">
<td nowrap>&nbsp;Open Message Queue Feature Matrix</td>
<td align="right" nowrap>&nbsp;</td>
</tr></form>
<tr><td colspan="3" class="white"><div class="min1">&nbsp;</div></td></tr>
</table>

<p>&nbsp;</p>
<table width="95%" border="0" cellpadding="0" cellspacing="0" class="generic1">
  <thead>
    <tr valign="middle">
      <td width="43%"><div>
        <div align="center">Feature</div>
      </div></td>
      <td width="2%">&nbsp;</td>
      <td width="55%"><div>
        <div align="center">Description</div>
      </div></td>
    </tr>
  </thead>
  <tbody>
    <tr valign="middle">
      <td><div>JMS 1.1 Specification </div></td>
      <td>&nbsp;</td>
      <td><div>Covers basic messaging requirements. Industry supported Java API. </div></td>
    </tr>
    <tr valign="middle">
      <td><div class="generic1" id="body">Java EE 1.4 support (JCA 1.5 Resource Adapter) </div></td>
      <td>&nbsp;</td>
      <td><div>Full support for all Java EE required interfaces. Allows integration with Java EE application servers that conform to the 1.4 or higher specification. </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Integrated File store</div></td>
      <td>&nbsp;</td>
      <td><div>For highest performance, efficient, embedded file store </div></td>
    </tr>
    <tr valign="middle">
      <td><div>JDBC File store </div></td>
      <td>&nbsp;</td>
      <td><div>Tested databases include Oracle, MySQL, Postres-SQL, Java DB (Derby) </div></td>
    </tr>
    <tr valign="middle">
      <td><div>High Availability (Automatic Takeover) </div></td>
      <td>&nbsp;</td>
      <td><div>HA via JDBC data store configuration. For best resilience and full availability. Tested with Oracle, GlassFish HADB (Enterprise Profile). MySQL coming soon! </div></td>
    </tr>
    <tr valign="middle">
      <td><div>High Availability (Active / Standby)  </div></td>
      <td>&nbsp;</td>
      <td><div>With Sun Cluster you can deploy for maximum system performance and availability with availability even in the event of a  server failure </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Distributed cluster support (Service availability)</div></td>
      <td>&nbsp;</td>
      <td><div>Multiple broker node support with no client connection restrictions. Provides service availability, high performance, low administration overhead. </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Publish and Subscribe Messaging </div></td>
      <td>&nbsp;</td>
      <td><div>Shared topic subscriptions, flexible distribution options </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Wild card Topics </div></td>
      <td>&nbsp;</td>
      <td><div>Allows for publish or subscribe with wild-card syntax. <em>New feature in version 4.2 </em></div></td>
    </tr>
    <tr valign="middle">
      <td><div>Range of message delivery modes </div></td>
      <td>&nbsp;</td>
      <td><div>Once and only once, At most once, at least once, non-acknowledged, duplicates okay </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Full range of Transaction support </div></td>
      <td>&nbsp;</td>
      <td><div>XA support for extended transaction context</div></td>
    </tr>
    <tr valign="middle">
      <td><div>Dead Message Queue</div></td>
      <td>&nbsp;</td>
      <td><div>If messages expire, are undeliverable, they are moved to a destination for administrative processing </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Message Compression </div></td>
      <td>&nbsp;</td>
      <td><div>Allows messages to be compressed for transmission across the client - server interface as well as storing it in compressed form. </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Port optimization </div></td>
      <td>&nbsp;</td>
      <td><div>Portmapper allows multiple protocols through single port. Reduces fire-wall complexity </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Flow Control </div></td>
      <td>&nbsp;</td>
      <td><div>When destinations reach configured thresholds, production is throttled back. A range of configuration options is provided </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Access Control </div></td>
      <td>&nbsp;</td>
      <td><div>Administer can control which user IDs have permissions for various operations </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Message Security </div></td>
      <td>&nbsp;</td>
      <td><div>HTTPS, SSL, TLS support for message security </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Authentication</div></td>
      <td>&nbsp;</td>
      <td><div>LDAP or file based credential support. JAAS support for custom authorization integration </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Message Selectors</div></td>
      <td>&nbsp;</td>
      <td><div>Allows consumer to query messages based on criteria </div></td>
    </tr>
    <tr valign="middle">
      <td><div>XML Schema Validation </div></td>
      <td>&nbsp;</td>
      <td><div>Prevents invalid XML messages from being produced into a destination. <em>New feature in Version 4.2 </em></div></td>
    </tr>
    <tr valign="middle">
      <td><div>C-API</div></td>
      <td>&nbsp;</td>
      <td><div>Supports c-integration, Solaris (SPARC/x86), Linux, Windows. </div></td>
    </tr>
    <tr valign="middle">
      <td><div>XA support via C-API </div></td>
      <td>&nbsp;</td>
      <td><div>XA support for C-API, tested with Tuxedo Transaction Manager. <em>New feature  in version 4.2 </em></div></td>
    </tr>
    <tr valign="middle">
      <td><div>JMS over HTTP/SOAP </div></td>
      <td>&nbsp;</td>
      <td><div>Allows JMS messaging through firewall tunneling </div></td>
    </tr>
    <tr valign="middle">
      <td><div>GUI based administration utility  </div></td>
      <td>&nbsp;</td>
      <td><div>Basic administration command support </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Complete control via command line </div></td>
      <td>&nbsp;</td>
      <td><div>All administration commands available through scriptable commands </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Complete JMX interface</div></td>
      <td>&nbsp;</td>
      <td><div>Allows integration with existing administration and monitoring tools or custom administration </div></td>
    </tr>
    <tr valign="middle">
      <td><div>JCA 1.5 Resource Adapter support</div></td>
      <td>&nbsp;</td>
      <td><div>Embedded Resource Adapter for GlassFish; JMSJCA support for extended integration support (WebLogic, WebSphere, JBOSS, Etc.); GenericResourceAdapter support for GlassFish integration. </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Distributed destinations </div></td>
      <td>&nbsp;</td>
      <td><div>Message destinations are shared between broker cluster nodes for better performance and load balancing </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Solaris Service Management Facility Integration </div></td>
      <td>&nbsp;</td>
      <td><div>Allows common configuration management integration</div></td>
    </tr>
    <tr valign="middle">
      <td><div>Java Enterprise System Management Framework</div></td>
      <td>&nbsp;</td>
      <td><div>Integrated management support for Java Enterprise family products </div></td>
    </tr>
    <tr valign="middle">
      <td><div>Internationalization</div></td>
      <td>&nbsp;</td>
      <td><div>All message strings can be localized</div></td>
    </tr>
    <tr valign="middle">
      <td><div>Native packages or archive distributions </div></td>
      <td>&nbsp;</td>
      <td><div>Can use native install for Solaris, Linux RPM. File archive for OEM or isolated install</div></td>
    </tr>
    <tr valign="middle">
      <td><div>Same product, community or commercial support. No technical barrier to move from community to production support. </div></td>
      <td>&nbsp;</td>
      <td><div>Commercial support provides patches, telephone technical support; service response commitments. Develop with confidence that your deployment will be fully supportable without additional developer or deployment expense. </div></td>
    </tr>
  </tbody>
</table>
<p></p>
<p>&nbsp;</p>
</body>
</html>
