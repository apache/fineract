# Installation Mifos Platform

This file describes how to install mifos platform for use in a production or test environment.

## Release Artifact Structure

The release artifact (.zip file) has the following structure:

```
  + mifosplatform-X.Y.Z.RELEASE  ... parent folder which will have the release number on it
  |
  + api-docs ... folder contains html based documentation on the platform API for this release
  + database ... folder contains database setup and upgrade scripts along with sample data required for installation
  + pentahoReports ... folder contains any out-of-the-box reports provided through pentaho
  + apps ... folder contains apps like community app that speak to the platform
  + runmifosx.bat ... launch script for Windows 
  + runmifosx.sh ... launch script for Mac OS X and Linux
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

The three ways to get up and running with mifos platform is:

1. The first (and the easiest) method is to just fire up the launch script provided in the release artifact and Mifos takes care of all the database, server-side and client-side setup for you. 
2. Use Amazon AWS and mifosplatform public AMI to spin up a new instance in the cloud
3. Manually install the prerequisite software on your own machine, follow setup instructions and use release artifacts to get platform running yourself.

## 1. Launch script automatic out-of-the box setup

Important : Automatic setup is currently broken, please follow the instructions for **Manual Installation** instead. For details of the issue, refer comments at https://mifosforge.jira.com/browse/MIFOSX-1756

The Mifos release artifact contains two launch scripts with names starting with "**runmifosx**". One of them (the one with the *.bat* file extension) is used to launch the platform on a Windows machine while the other (which has the *.sh* file extension) is used to launch the platform on Mac OS X and Linux. Just double-click on the respective script (depending on your OS) and voila! you have your own Mifos X platform running locally on your computer. 

**Note**: *In case your script does not launch by double clicking on Linux or Mac OS X, here's what you can do. Head over to bash and type the following:*

```
$ cd /path/where/runmifosx.sh/is/located
$ chmod 0755 ./runmifosx.sh
$ ./runmifosx.sh
```

## 2. Amazon Public AMI

  Use AWS Wizard to launch instance by using this link: <a target="_blank" href="https://console.aws.amazon.com/ec2/home?region=ap-southeast-1#launchAmi=ami-909bb3c2" title="Latest Mifos Platform Public AMI">Mifos Platform AMI (ami-909bb3c2)</a>

  *Note:* Read through the following as you step through the AWS Wizard
  
  - You are automatically brought to Step 2 on the wizard, by default a 'micro' instance is selected, you may need to select a 'General Purpose' m1.small as the t1.micro's memory is right on the edge of whats needed to support MySQL, Tomcat 7 + Mifos Platform
  - Use default settings for steps 3 to 5 until you reach step 6 configure security group.
  - If your first time using AWS you should use the create new security group option and add the following 'rules' to the security group:
    - SSH (PORT 22), MYSQL (3306), HTTPS (443), Custom TCP Rule (8443) all with a 'Source' value of 'Anywhere'
  - Click 'Review and Launch'
  - You will be asked to use a 'keypair' which you will need to SSH onto the new instance, if this is your first time create a new keypair, be srue to download it and store in place as you will need it later, otherwise use an existing keypair.
  - When the instance starts, the following should be available at:
    - Platform application should be available @ https://[public DNS]:8443/mifosng-provider/api/v1/offices?tenantIdentifier=default&pretty=true
    - Community app should be available @ https://[public DNS]:8443/community-app
    - API docs should be available @ https://[public DNS]:8443/api-docs/apiLive.htm

  *Name:* Mifos Platform 1.26.0.RELEASE Public AMI
  
 - AMI ID: ami-909bb3c2
 - Kernel ID: aki-503e7402
 - Name: Mifos Platform 1.26.0.RELEASE Public AMI
 - Owner: 239215483039
 - Source: 239215483039/Mifos X-1.26.0 release
 - Architecture: Ubuntu12.04 LTS x86_64
 - Built starting from Ubuntu AMI ami-35acbb41
 - Java 1.7.0_51 64 bit JVM
 - Tomcat 7.0.39 (with SSL configured for self-signed certificate)
 - MySql 5.5.31

## 3. Manual Installation

### 3.1 Prerequisite Software

  Before running mifos platform you must have the following software installed:
  - Oracle Java - JDK 7 (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  - Oracle MySQL - (http://dev.mysql.com/downloads/)
  - Apache Tomcat 7 - (http://tomcat.apache.org/download-70.cgi)

### 3.2 MySQL Setup

  Assumption: MySQL server is now installed.
  
  You should know your password for MySQL root user. If you wish you can change it by doing:
  ```
  mysqladmin -u root -p 'oldpassword' password newpassword
  ```
  The mifosplatform settings default to a mysql root user with username: root, password: mysql
  
  If you have never installed mifosplatform before follow section 2.2.1 for first time setup of database, otherwise read section 2.2.2 on database upgrades.
  
#### 3.2.1 First Time Database setup

  Mifos platform has support for hosting multiple tenants so we use two database schemas:
   - *mifosplatform-tenants*: which is responsible for persisting the tenant information which is used when deciding what schema each incoming request in the platform should route to. It acts as a registry which contains the details of all the tenant databases, and their connection information, which is used by MifosX to connect to the appropriate tenant.
   - *mifostenant-default*: All tenant specific schemas follow the pattern mifostenant-xxxx, out of the box the default schema is used.
   
  Step one: create mifosplatform-tenants database
  ```
  mysql -uroot -pmysql
  create database `mifosplatform-tenants`;
  exit
  ```

  Step two: create mifostenant-default database
  ```
  mysql -uroot -pmysql
  create database `mifostenant-default`;
  exit
  ```
  The tables and lookup data for both databases are created on application startup.

  Step three (optional): to be followed only if your mysql credentials are other than root/mysql
  
  Manually restore the contents of *mifosplatform-tenants* database

  ```
  mysql -uroot -pmysql mifosplatform-tenants < database/mifospltaform-tenants-first-time-install.sql
  ```
 Next, update the default credentials for connecting to *mifoplatform-default* database stored in the *tenants* table of *mifosplatform-tenants* with your credentials

  ```
  UPDATE tenants SET schema_username = "your_username", schema_password = "your_password" WHERE identifier = "default";
  ```

#### 3.2.2 Upgrade existing database(s)

  The list database *mifosplatform-tenants* is upgraded when the application starts. Any *tenant* databases will also be upgraded automatically when the application starts if the *auto_update* field of the *tenants* database table is enabled(=1). This is the default setting.
  
  Upgrading your database in this way is the recommended way as it will upgrade any *tenants* setup in the *mifosplatform-tenants* database but can be disabled by setting the *auto_update* field of the tenant to zero.
  
##### 3.2.2.1 Special Instructions for those upgrading from version 1.24.* or lower

Starting from version 1.25.* , updates to *mifosplatform-tenants* database are managed by Flyway (http://flywaydb.org/). To ensure that flyway works correctly, verify that table *schema_version* is present along with two entries in *mifosplatform-tenants* database.

You can do so as follows

````
mysql -uroot -pmysql
 
use `mifosplatform-tenants`;
select * from schema_version;
```

If the table does not exist, create the same using the script at https://gist.github.com/vishwasbabu/dc105b6a9450cff8ff1f

Next, check if the patch for "externalizing mysql connection properties" has been run (i.e if you are updating from 1.21 or a higher version of Mifos X)

````
mysql -uroot -pmysql
 
use `mifosplatform-tenants`;
select pool_initial_size from tenants;
```

If the above query does **not** throw an error (Unknown column 'pool_initial_size'), *schema_version* needs to be updated with the details of this patch by executing the scripts below

```
mysql -uroot -pmysql
 
use `mifosplatform-tenants`;
INSERT INTO `schema_version` (`version_rank`, `installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES
  (2, 2, '2', 'externalize-connection-properties', 'SQL', 'V2__externalize-connection-properties.sql', 210473669, 'root', '2014-10-12 22:13:51', 661, 1);
```

#### 3.2.3 Load *mifostenant-default* schema with sample data (optional)
  Every release ships with sample data (offices, users, customers, loan products, savings products and a chart of accounts). The same can be restored by running the following command
  
   ```
  mysql -uroot -pmysql mifostenant-default < database/migrations/sample_data/load_sample_data.sql
  ```

  
### 3.3 Tomcat 7 Setup

  Assumption: You have downloaded and installed Tomcat 7 correctly for your operation system: see http://tomcat.apache.org/tomcat-7.0-doc/setup.html
  
#### 3.3.1 Environment Variables Check

  Check that the following environment variables exist:
  - JAVA_HOME ... should point to directory where a 1.7 JDK or JRE is on machine
  - CATALINA_HOME ... should point to a directory where a Tomcat 7 instance is installed

  Check that the following is on your path:
  - %JAVA_HOME%\bin;

#### 3.3.2 Logging

  - In the [TOMCAT_HOME]/logs create a file called ```mifos-platform.log```

#### 3.3.3 Libraries

  Ensure the following libraries are in the [TOMCAT_HOME]/lib folder
  - tomcat-jdbc.jar (You can download the same from http://central.maven.org/maven2/org/apache/tomcat/tomcat-jdbc/7.0.57/tomcat-jdbc-7.0.57.jar)
  - mysql-connector-java-5.1.22 (You will need to download the latest MySQL Connector/J Jar file from http://dev.mysql.com/downloads/connector/j/)

#### 3.3.4 Configure for SSL

  Generate a new keystore using java keytool (if you havent already done this):
  - Follow docs to create keystore.(http://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html#Configuration)
   
  - Update server.xml as in docs. (Ther server.xml configuration given in the configure tomcat 7 below is what is need.)

  ```sudo keytool -genkey -alias mifostom -keyalg RSA -keystore /home/ubuntu/.keystore```

#### 3.3.5 Update tomcat configuration files for SSL, compression and JNDI connection to MySql

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

#### 3.3.6 Drop application into tomcat webapps folder

  Drop the following from the release artifact into the [TOMCAT_HOME]/webapps folder:
  - mifosng-provider.war
  
  Drop the following from the release artifact into [TOMCAT_HOME]/webapps/ROOT folder:
  - The entire ```apps/community-app``` folder. 
  - The entire api-docs folder

#### 3.3.7 Startup tomcat
  Startup tomcat:
  - Platform application should be available @ https://[server ip address]:8443/mifosng-provider/api/v1/offices?tenantIdentifier=default&pretty=true
  - Community application should be available @ https://[server ip address]:8443/community-app?baseApiUrl=https://[server ip address]:8443/mifosng-provider/api/v1/
  - API docs should be available @ https://[server ip address]:8443/api-docs/apiLive.htm
  
  *where [server ip address] is the hostname or IP address of your computer. For instance, if you've installed Mifos on your local machine then [server ip address] is localhost*

## How to integrate the web front-end UI with the back-end

To be able to use the front-end UI along with the back-end, you simply need to copy the webapp's source code files to the ```apps``` folder. For instance, you could copy the source code files of the community app to ```apps/community-app```. This would enable you to access the commuity app from the following URL:
*https://[server ip address]:8443/mifosng-provider/apps/community-app?baseApiUrl=https://[server ip address]:8443*
