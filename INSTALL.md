# Installation Mifos Platform

This file describes how to install mifos platform for use in a production or test environment.

The two ways to get up and running with mifos platform is:

1. Use Amazon AWS and mifosplatform public AMI to spin up a new instance in the cloud
2. Manually install the prerequisite software on your own machine, follow setup instructions and use release artifacts to get platform running yourself.

## 1. Amazon Public AMI


Launch instance <a target="_blank" href="https://console.aws.amazon.com/ec2/home?region=eu-west-1#launchAmi=ami-65e6e011" title="Mifos X Public AMI 1">ami-65e6e011</a>

 - Mifos X Public AMI 1
 - Ubuntu11.10, tomcat7.0.21 (with SSL),MySql 5.1.62, Java 1.6_30, Mifos X Platform

## 2. Manual Installation

### 2.1 Prerequisite Software

  Before running mifos platform you must have the following software installed:
  - Oracle Java - JDK/JRE 1.6 (NOT 1.7) (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
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

### 2.4 Release Artifacts
  
  
  
  
