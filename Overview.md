# Open Message Queue -- Quick Start

**This page gives a brief overview of the main features of Open MQ**

Open MQ implements a reliable messaging mechanism to allow your applications to integrate together without relying on synchronous communications, and providing buffering between the message producers and message consumers. Open MQ provides a unified building block that enables asychronous, loosely coupled integration and provides a common framework for administration, control, and monitoring. 

Open Message Queue is a full featured Message Oriented Middleware (MOM),  messaging server. Open MQ implements the Java Message Service (JMS) API and provides enterprise class features such as:

* Loosely coupled messaging  between enterprise system components
* Scalable distribution of message servers (broker clustering) 
* Integrated SOAP / HTTP messaging
* Scalable JCA 1.5 compliant Resource Adapter
* Enterprise Administration Features
* Extensive JMX support 

A more complete feature list is available <a href="features.html">here</a>. 

<p>&nbsp;
<hr width="40%" size="5" noshade="noshade" />
<h2>Why use a message queue?</h2>
<p>Message oriented integration has some advantages over direct integration between applications. Primarily, this allows the messaging infrastructure to operate as a buffer between your major system components. Additionally, messaging can provide a mechanism through which you can communicate with a disparate collection of system components, without necessarily knowing the details of those components. Since there's an integral buffer between the connected applications, each of these systems can operate at their own pace while the messaging infrastructure can absorb messages at the production rate, while they can be consumed at whatever rate the consumer is able to work through these messages.
<p>Messaging is fundamentally different from an API call because your applications cannot know when the application at the other end of the communications link is going to receive the message and perform whatever actions are implied by that message. It is inherently asynchronous. Applications which require tight coupling may not be the best choice for a MOM intermediary. All actions between your systems are abstracted to a collection of messages which are exchanged via the JMS server, operating as an intermediary between your application end-points. Just because an application is asynchronous, does not mean that it cannot have high-throughput. In fact, many applications have been implemented which perform at very high message exchange rates. 
<p>What MQ can ensure is that the messages will be operated in the proper order and, if there are any delivery problems, the infrastructure can know with certainty what the final delivery disposition was (success or failure). 
<h2>Open MQ Installation</h2>
<p>There are a few options available to you. The simplest is to download the GUI installer packages and follow the instructions from the installation application. If you want to download a smaller package, you can download the package archive -- without the installer. We call this a &quot;file based&quot; install. This will simply unpack the product where-ever you choose. You can then run the setup script to configure Open MQ for the most general types of use. 
<p>If you want to start from the source code, you'll need a copy of NetBeans (or a sufficiently equipped IDE). We provide instructions for building and then running a simple test application to verify that what you have built, is built properly. See the <a href="./downloads.html#source">downloads page</a> for directions about how to obtain this package
<h2>Open MQ Architecture and System Components </h2>
<p>Here's a block diagram to orient you to the major system components of Open MQ
<p align="center"><img src="to_MQService.jpg" alt="Open MQ Block Diagram" width="424" height="446" />
<h2> <a name="broker" id="broker"></a>Open MQ Message Brokers</h2>
<p>Message Brokers are the fundamental work-horses of Open MQ. These server side applications manage all message exchanges between the clients who are producing and consuming messages. A message broker is responsible for managing a collection of message destinations. Clients can produce messages to, or consume messages from a message destination. A message destination can be configured as a message queue, or as a topic destination. Queues are used when you want messages to be handled with precise ordering and, when you need to ensure that delivery is guaranteed. Topic destinations are useful in &quot;Publish and Subscribe&quot; messaging. Pub-Sub messaging is useful for distributing messages to more than one consumer (though there's no reason you can't have only one consumer). Whereas, message &quot;queues&quot; are used when you need to ensure that messages are handled in an exact order, and with specific recovery and retry capabilities.
<p>Brokers can be &quot;clustered&quot; for service and data redundancy. Messages Broker Clusters manage internal book-keeping between the &quot;Cluster Nodes&quot; to ensure that the rules for delivery are followed. 
<h2><a name="admin" id="admin"></a>Open MQ Administration  </h2>
<p>Open MQ provides a simple, built-in administration GUI. This user interface provides for common tasks such as starting and stopping a message queue broker, creating destinations, and simple tasks. For more complex applications, we provide a rich Java Management Extensions (JMX) API. Through the JMX API, you can monitor and manage nearly any aspect of your Open MQ system. Further, you can develop your own management controls via this API. If your project includes Java Enterprise System, then the Java Enterprise System Monitoring Framework is available for use with Open MQ. Likewise, Java Composite Application Suite -- and the GlassFish Server application server also use the JMX API for controlling and managing Open MQ operations. 
<h2><a name="stores" id="stores"></a>Open MQ Message Storage Options</h2>
<p>Open MQ supports two basic types of message storage. You can use the embedded &quot;File-Store,&quot; or you can use a JDBC data-source. The embedded file-store is used if you make no changes to the default setting. This is optimized for use with open MQ and, in most applications will provide you with the highest system performance. Each Open MQ Broker manages its own file-store. There are facilities for message distribution when using the embedded file-store, however, these do not provide continuous availability, in the event of a broker failure.
<p>Many sites prefer using a common data storage layer, based on SQL. For these sites, Open MQ supports databases via JDBC. JDBC does not provide as high a level of optimization for Messaging use. Additionally, all the optimizations which are available must be implemented in the database, as part of that products' administration utilities. In general, you can expect higher initial throughput when using the embedded file-store.
<p>For high reliability and high-availability use, you must use a high-availability JDBC database. Open MQ uses the jdbc database to store data and it implements a low-volume broker-to-broker communication protocol to resolve any run-time operational issues which are not appropriate within the database. Open MQ can be configured for HA use with any JDBC database, but for maximum availability, you will want to deploy with a database that is resilient to failure, and provides complete internal data consistency and redundancy. Example HA databases include: HADB -- the JDBC data store which is included in Oracle GlassFish  Server (AKA Sun Java System Application Server 9.1); mySQL Cluster edition (with ndb storage); and Oracle. 
<h2><a name="javaclient"></a>Open MQ Java Client</h2>
<p>Open MQ provides a rich client API for developing your Java applications. If you are writing a stand-alone application you can integrate directly with Open MQ via the Java client API. The API is much too rich to detail here. When you install the product, it includes complete javadoc. And, you can read the extensive manual in the MQ <a href="http://download.oracle.com/docs/cd/E19798-01/">document collection</a>. For an index of all the documents, you can also browse over to the Document section of the <a href="./downloads.htm#docs">Downloads Page</a>. 
<h2><a name="cclient"></a>Open MQ C-Client</h2>
<p>Many projects want to interface with a C based client. There is no standard for the C language, for JMS. This has been discussed as a possible extension, but so far, there's been no firm action taken on creating a C standard. So, every product has a slightly unique c-api which they use to <em>simulate</em> the Java JMS standard. Again, it's too rich to document here. You should read the programmers guide to the c-api which is available in the MQ <a href="http://download.oracle.com/docs/cd/E19798-01/821-1795/index.html">document collection</a>, or via the link at the Open MQ <a href="./downloads.htm#docs">Downloads Page</a>. 
<h2><a name="http" id="http"></a>Open MQ JMS over HTTP</h2>
<p>You can work directly with JMS over HTTP using Open MQ. This allows you to use remote clients that span across traditional fire-walls. This is detailed in the<a href="http://download.oracle.com/docs/cd/E19798-01/821-1796/aeqex/index.html"> Java Client Developers Guide</a>, Chapter 5 &quot;Working With SOAP Messages&quot; 
<h2>Product Manuals</h2>
<p>We have extensive manuals which describe these concepts and more. Begining with MQ 4.4 update 2, all MQ documentation is now contained in the related GlassFish Server documentation. GlassFish 3.0.1 documentation is available <a href="http://download.oracle.com/docs/cd/E19798-01/">here</a>. Legacy documentation, for all prior releases can be found at <a href="http://www.oracle.com/technetwork/indexes/documentation/legacy-glassfish-message-queue-306290.html">this link</a>. 
<p> If you're new to JMS Messaging, you might want to read the Overview Guide to become oriented to the product. You can get your hands dirty with the Admin. overview and Quick Start tutorial, in the <a href="http://download.oracle.com/docs/cd/E19798-01/821-1794/gcrlv/index.html">Administration Guide, Part 1, Introduction to MQ Administration</a> -- which will guide you through the basic administration function, followed by a tutorial. Following that, you might find the <a href="http://download.oracle.com/docs/cd/E19798-01/821-1798/index.html">MQ Technical Overview</a> useful for planning and organizing how you want to approach your JMS project. 
<p>There are also many tips and hints at the Open MQ <a href="https://glassfish.java.net/wiki-archive/OpenMessageQueue.html">Wiki</a> and <a href="https://glassfish.java.net/wiki-archive/OpenMQFAQ.html">FAQ</a>. 
<hr width="40%" size="5" noshade="noshade" />

<p>Open Message Queue is integrated into several community and commercial class products. It is the Java EE Java Message Service (JMS) provider for Project GlassFish, and it is the reference implementation JMS Provider for Java Platform, Enterprise Edition.
<p>If you are interested in using a supported version of Open Message Queue, you should look over at the <a href="http://www.oracle.com/technetwork/middleware/glassfish/overview/index.html">Oracle GlassFish Server product pages</a>. Trial versions are available for download and service contracts are available from Sun Microsystems. Please remember, features in the commercial class products may vary from Open MQ. 
<table border="0" cellspacing="0" cellpadding="0" width="95%" class="generic1">
  <tbody>
    <tr>
      <td><div><a href="https://glassfish.java.net/">GlassFish Community</a> </div></td>
    </tr>
    <tr>
      <td><div><a href="https://open-esb.java.net/">Project Open Enterprise Service Bus </a> </div></td>
    </tr>
    <tr>
      <td><div><a href="http://www.oracle.com/technetwork/middleware/glassfish/overview/index.html">Oracle GlassFish Server</a> -- Includes the commercial version of Open Message Queue for which you may purchase a support contract </div></td>
    </tr>
    <tr>
      <td><div><a href="http://netbeans.org">NetBeans</a></div></td>
    </tr>
    <tr>
      <td><div><a href="http://www.oracle.com/technetwork/java/javaee/overview/index.html">Java Enterprise Edition at OTN</a></div></td>
    </tr>
    <tr>
      <td><div><a href="http://docs.oracle.com/javase/7/docs/technotes/tools/">Java Tools</a></div></td>
    </tr>
    <tr>
      <td><div><a href="http://www.oracle.com/us/products/servers-storage/solaris/index.html">Solaris Operating Environment</a></div></td>
    </tr>
  </tbody>
</table>
<p>&nbsp;
</div></td>
</tr>
<tr class="white"><td><div class="min1">&nbsp;</div></td></tr>
</table>
</div>
</div>
</div>

