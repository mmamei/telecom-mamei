
***** Instructions to configure Tomcat properly *******

1. create a key for the SSL algorithms.
From command prompt run:

keytool -genkey -alias tomcat -keyalg RSA

This creates a file ".keystore" in the user main dir.
Move the file in tomcat conf directory and rename as tomcat.keystore


2. go to Tomcat server.xsl and uncomment the HTTPS connector, changing as follows:


<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
        maxThreads="150" SSLEnabled="true" scheme="https" secure="true"
	keystoreFile="conf/tomcat.keystore" keystorePass="1976jotequfine"
        clientAuth="false" sslProtocol="TLS" />


3. change the tomcat-users.xml file in conf


4. go to conf/catalina.properties
Add bin and lib/*.jar of the Eclipse project to common.loader

common.loader=${catalina.base}/lib,${catalina.base}/lib/*.jar,${catalina.home}/lib,${catalina.home}/lib/*.jar,C:/Users/Marco/Google Drive/Code/TELECOM/Telecom2/bin,C:/Users/Marco/Google Drive/Code/TELECOM/Telecom2/lib/*.jar



5. increase jvm size

in catalina.bat file:
set JAVA_OPTS= -Xms3000m -Xmx3000m

or configure via the GUI in  

tomcat8w.exe




6. Change the webapp directory of this application- by adding the <context> element to the <host> element in server.xml

<Host name="localhost"  appBase="webapps" unpackWARs="true" autoDeploy="true">
<Context path="/pls" docBase="C:/Users/Marco/Google Drive/Code/TELECOM/Telecom2/web" debug="0" reloadable="true" crossContext="true"></Context>
