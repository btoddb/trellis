Trellis
-------

The following instructions have been tested on OS X

Assume ${trellis-home} is the location of Trellis source code
Assume ${trellis-install-home} is the destination install directory for Trellis (not source code)

Patching Cassandra
------------------

cassandra-1.1.4-notify.patch works against (as you might guess) Apache Cassandra 1.1.4,
but should work on other releases with a bit of coercing.

Download Apache Cassandra 1.1.4 source code from here and untar into clean directory:

   http://cassandra.apache.org/download/
   
change to cassandra source directory:

   patch -p1 ${trellis-home}/cassandra-1.1.4-notify.patch
   ant clean mvn-install


Building Trellis
----------------

After patching Cassandra and building, the result is that maven has put the Cassandra
artifacts in your local maven repository.  For the following to work, the proper version
must be set in Trellis' pom.xml.  For example, in the previous section the proper build is
"1.1.4-notify-SNAPSHOT".  So edit Trellis' pom.xml and modify the property, cassandra.version,
and use the proper version.

Edit cassandra version property, cassandra.version:

   vi ${trellis-home}/pom.xml
   
Build:

   cd ${trellis-home}
   mvn clean package -P assemble-artifacts

The following artifacts are produced:

   ${trellis-home}/trellis-server/target/trellis-server-0.1.5-SNAPSHOT-server.tar.gz
   ${trellis-home}/trellis-actor-example/target/trellis-actor-example-0.1.5-SNAPSHOT-actor-providers.jar


Deploying Trellis
-----------------

Assume deploying patched cassandra 1.1.4

Change to install directory:

   cd ${trellis-install-home}

Install Trellis and patched Cassandra (make sure to untar cassandra first!):

   tar xvfz ${wherever-it-is}/apache-cassandra-1.1.4-notify-SNAPSHOT-bin.tar.gz
   tar xvfz ${trellis-home}/trellis-server/target/trellis-server-0.1.5-SNAPSHOT-server.tar.gz
   

Deploy Actors
-------------

All actor jars must be put in ${trellis-install-home}/trellis/provider-libs.  Trellis must be
restarted to find new/changed actors.

If you would like the example actor:

   cp ${trellis-home}/trellis-actor-example/target/trellis-actor-example-0.1.5-SNAPSHOT-actor-providers.jar \
         ${trellis-install-home}/provider-libs/.


Starting/Stopping Trellis
-------------------------

${trellis-install-home}/bin/start.sh
${trellis-install-home}/bin/stop.sh


Creating an Actor
-----------------

Best to use the trellis-actor-example as a blueprint for creating an actor.  However, a little explanation is
needed to show how actors are discovered.  If you notice in the example there is a META-INF/services directory
in the example actor jar file.  This directory defines the "services" a jar file supports as defined by Java's
ServiceLoader interface (http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html).

Create a file named "com.btoddb.trellis.actor.TrellisActorLoader" in your jar's META-INF/services directory,
which should have one line for each actor.  Each line is simply the complete path to a class implementing,
com.btoddb.trellis.actor.TrellisActorLoader.

