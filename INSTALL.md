# Installation Mifos Platform

This file describes how to install mifos platform for use in a production or test environment.

## Release Artifact Structure

The release artifact (.zip file) has the following structure:

```
  + mifosplatform-X.Y.Z.RELEASE  ... parent folder which will have the release number on it
  |
  + api-docs ... folder contains html based documentation on the platform API for this release
  + database ... folder contains database setup and upgrade scripts required for installation
  + pentahoReports ... folder contains any out-of-the-box reports provided through pentaho
  + apps ... folder contains apps like reference app that speak to the platform
  |
  -- CHANGELOG.MD ... file documents changelog of project up to this release
  -- CONTRIBUTORS.MD ... file provides details on contributors to project
  -- INSTALL.MD ... file provides details on installation instructions
  -- LICENSE.MD ... file provides details on the open source license used by the project
  -- mifosng-provider.war ... the platform WAR file to be dropped into Tomcat 7
  -- README.MD ... initial readme file for the project
  -- VERSIONING.MD ... file provides details on approach to release versioning 
```
  
## How to Install

The two ways to get up and running with mifos platform is:

1. Use Amazon AWS and mifosplatform public AMI to spin up a new instance in the cloud
2. Manually install the prerequisite software on your own machine, follow setup instructions and use release artifacts to get platform running yourself.

## 1. Amazon Public AMI

  Use AWS Wizard to launch instance by using this link: <a target="_blank" href="https://console.aws.amazon.com/ec2/home?region=eu-west-1#launchAmi=ami-948b6be3" title="Mifos Platform Public AMI 1.13.0.RELEASE">Mifos Platform AMI (ami-948b6be3)</a>

  *Note:* Read through the following as you step through the AWS Wizard
  
  - You are automatically brought to Step 2 on the wizard, by default a 'micro' instance is selected, you may need to select a 'General Purpose' m1.small as the t1.micro's memory is right on the edge of whats needed to support MySQL, Tomcat 7 + Mifos Platform
  - Use default settings for steps 3 to 5 until you reach step 6 configure security group.
  - If your first time using AWS you should use the create new security group option and add the following 'rules' to the security group:
    - SSH (PORT 22), MYSQL (3306), HTTPS (443), Custom TCP Rule (8443) all with a 'Source' value of 'Anywhere'
  - Click 'Review and Launch'
  - You will be asked to use a 'keypair' which you will need to SSH onto the new instance, if this is your first time create a new keypair, be srue to download it and store in place as you will need it later, otherwise use an existing keypair.
  - When the instance starts, the following should be available at:
    - Platform application should be available @ https://[public DNS]:8443/mifosng-provider/api/v1/offices?tenantIdentifier=default&pretty=true
    - Reference application should be available @ https://[public DNS]:8443/IndividualLendingGeneralJavaScript/IndivLendHome.html?baseApiUrl=https://[server ip address]:8443/mifosng-provider/api/v1/
    - API docs should be available @ https://[public DNS]:8443/api-docs/apiLive.htm

  *Name:* Mifos Platform 1.13.0.RELEASE Public AMI
  
 - AMI ID: ami-948b6be3
 - Kernel ID: aki-71665e05
 - Name: Mifos Platform 1.13.0.RELEASE Public AMI
 - Owner: 476083131096
 - Source: 476083131096/Mifos Platform 1.13.0.RELEASE Public AMI
 - Architecture: Ubuntu12.04 LTS x86_64
 - Built starting from Ubuntu AMI ami-35acbb41
 - Java 1.6_45 32 bit JVM
 - Tomcat 7.0.39 (with SSL configured for self-signed certificate)
 - MySql 5.5.31
 - Mifos Platform 1.13.0.RELEASE
 - Mifos Reference App 1.13.0.RELEASE

## 2. Manual Installation

### 2.1 Prerequisite Software

  Before running mifos platform you must have the following software installed:
  - Oracle Java - JDK 7 (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  - Oracle MySQL - (http://dev.mysql.com/downloads/)
  - Apache Tomcat 7 - (http://tomcat.apache.org/download-70.cgi)

### 2.2 MySQL Setup

  Assumption: MySQL server is now installed.
  
  You should know your password for MySQL root user. If you wish you can change it by doing:
  ```
  mysqladmin -u root -p 'oldpassword' password newpassword
  ```
  The mifosplatform settings default to a mysql root user with username: root, password: mysql
  
  If you have never installed mifosplatform before follow section 2.2.1 for first time setup of database, otherwise read section 2.2.2 on database upgrades.
  
#### 2.2.1 First Time Database setup

  Mifos platform has support for hosting multiple tenants so we use two database schemas:
   - *mifosplatform-tenants*: which is responsible for persisting the tenant information which is used when deciding what schema each incoming request in the platform should route to. It acts as a registry which contains the details of all the tenant databases, and their connection information, which is used by MifosX to connect to the appropriate tenant.
   - *mifostenant-default*: All tenant specific schemas follow the pattern mifostenant-xxxx, out of the box the default schema is used.
   
  Step one: create mifosplatform-tenants database
  ```
  mysql -uroot -pmysql
  create database `mifosplatform-tenants`;
  exit
  ```

  Step two: populate mifosplatform-tenants database using ```database/mifospltaform-tenants-first-time-install.sql```
  ```
  mysql -uroot -pmysql mifosplatform-tenants < database/mifospltaform-tenants-first-time-install.sql
  ```
  
  Step three: create mifostenant-default database
  ```
  mysql -uroot -pmysql
  create database `mifostenant-default`;
  exit
  ```

#### 2.2.2 Upgrade existing database(s)

  Any *tenant* databases will be upgraded automatically when the application starts if the *auto_update* field of the *tenants* database table is enabled(=1). This is the default setting.
  
  Upgrading your database in this way is the recomended way as it will upgrade any *tenants* setup in the *mifosplatform-tenants* database but can be disabled by setting the *auto_update* field of the tenant to zero.
  
### 2.3 Tomcat 7 Setup

  Assumption: You have downloaded and installed Tomcat 7 correctly for your operation system: see http://tomcat.apache.org/tomcat-7.0-doc/setup.html
  
#### 2.3.1 Environment Variables Check

  Check that the following environment variables exist:
  - JAVA_HOME ... should point to directory where a 1.6 JDK or JRE is on machine
  - CATALINA_HOME ... should point to a directory where a Tomcat 7 instance is installed

  Check that the following is on your path:
  - %JAVA_HOME%\bin;

#### 2.3.2 Logging

  - In the [TOMCAT_HOME]/logs create a file called ```mifos-platform.log```

#### 2.3.3 Libraries

  Ensure the following libraries are in the [TOMCAT_HOME]/lib folder
  - tomcat-jdbc.jar
  - mysql-connector-java-5.1.22 (You will need to download latest MySQL Connector/J Jar file from http://dev.mysql.com/downloads/connector/j/)

#### 2.3.4 Configure for SSL

  Generate a new keystore using java keytool (if you havent already done this):
  - Follow docs to create keystore.(http://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html#Configuration)
  - Update server.xml as in docs. (Ther server.xml configuration given in the configure tomcat 7 below is what is need.)

  ```sudo keytool -genkey -alias mifostom -keyalg RSA -keystore /home/ubuntu/.keystore```

#### 2.3.5 Update tomcat configuration files for SSL, compression and JNDI connection to MySql

```
<?xml version='1.0' encoding='utf-8'?>
<Server port="8005" shutdown="SHUTDOWN">
<Listener className="org.apache.catalina.core.JasperListener" />
<Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
<Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
<Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />
 
  <GlobalNamingResources>
       <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml"
       />
 
       <Resource type="javax.sql.DataSource"
            name="jdbc/mifosplatform-tenants"
            factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
            driverClassName="com.mysql.jdbc.Driver"
            url="jdbc:mysql://localhost:3306/mifosplatform-tenants"
            username="root"
            password="[root mysql password]"
            initialSize="3"
            maxActive="10"
            maxIdle="6"
            minIdle="3"
            validationQuery="SELECT 1"
            testOnBorrow="true"
            testOnReturn="true"
            testWhileIdle="true"
            timeBetweenEvictionRunsMillis="30000"
            minEvictableIdleTimeMillis="60000"
            logAbandoned="true"
            suspectTimeout="60"
       />
  </GlobalNamingResources>
 
  <Service name="Catalina">

<Connector protocol="org.apache.coyote.http11.Http11Protocol"
           port="8443" maxThreads="200" scheme="https"
           secure="true" SSLEnabled="true"
           keystoreFile="/home/ubuntu/.keystore"
           keystorePass="testmifos"
           clientAuth="false" sslProtocol="TLS"
           URIEncoding="UTF-8"
           compression="force"
           compressableMimeType="text/html,text/xml,text/plain,text/javascript,text/css"/>
 
      <Engine name="Catalina" defaultHost="localhost">
 
      <Realm className="org.apache.catalina.realm.LockOutRealm">
        <Realm className="org.apache.catalina.realm.UserDatabaseRealm" resourceName="UserDatabase"/>
      </Realm>
 
      <Host name="localhost"  appBase="webapps" unpackWARs="true" autoDeploy="true">
 
          <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="localhost_access_log." suffix=".log"
               pattern="%h %l %u %t &quot;%r&quot; %s %b" />
      </Host>
    </Engine>
  </Service>
</Server>
```

#### 2.3.6 Drop application into tomcat webapps folder

  Drop the following from the release artifact into the [TOMCAT_HOME]/webapps folder:
  - mifosng-provider.war
  
  Drop the following from the release artifact into [TOMCAT_HOME]/webapps/ROOT folder:
  - The entire ```apps/IndividualLendingGeneralJavaScript``` folder
  - The entire api-docs folder

#### 2.3.7 Startup tomcat
  Startup tomcat:
  - Platform application should be available @ https://[server ip address]:8443/mifosng-provider/api/v1/offices?tenantIdentifier=default&pretty=true
  - Reference application should be available @ https://[server ip address]:8443/IndividualLendingGeneralJavaScript/IndivLendHome.html?baseApiUrl=https://[server ip address]:8443/mifosng-provider/api/v1/
  - API docs should be available @ https://[server ip address]:8443/api-docs/apiLive.htm
