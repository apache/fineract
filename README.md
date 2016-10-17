Apache Fineract: A Platform for Microfinance
============

The next evolution of fineract focussing on being faster, lighter and cheaper to change (than existing mifos) so that it is more responsive to the needs of MFI’s and Integrators

Requirements
============
. Java >= 1.8 (Oracle JVMS have been tested)
. gradle-wrapper.jar version 2.10
. MySQL 5.5

Instructions to build war file
============

1. Extract the archive file to your local directory
2. Download gradle-wrapper.jar version 2.10 and place it in fineract-provider/gradle/wrapper folder
3. Change current working directory to fineract-provider
4. Run './gradlew clean war' or './gradlew build' to build deployable war file which will be created at fineract-provider/build/libs directory. 


Instructions to execute Integration tests
============
1. Login to mysql DB using 'mysql -u root -pmysql'
2. Create mifosplatform-tenants database using create database `mifosplatform-tenants`;
3. Create default tenant database using create database `mifostenant-default`;
4. With fineract-provider as current working directory run below commands
4.a. ./gradlew migrateTenantListDB -PdbName=mifosplatform-tenants
4.b. ./gradlew migrateTenantDB -PdbName=mifostenant-default
5. Run './gradlew clean integrationTest'


More details of the project can be found at https://cwiki.apache.org/confluence/display/FINERACT