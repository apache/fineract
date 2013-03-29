package org.mifosplatform.database;
import com.googlecode.flyway.core.Flyway;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;

/**
 * Use Flyway library to migrate DB schema. Searches for .sql files in the migrate folder.
 * For more details on this refer to:
 * http://flywaydb.org/documentation/migration/sql.html
 */
public class DBMigrationHelper {

//    private static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/mifostenant-default";
//    private static final String DB_USER = "root";
//    private static final String DB_PASSWORD = "mysql";

    protected final Log logger = LogFactory.getLog(getClass());

    public void migrateDB(DataSource tenantDataSource){

       logger.info("Checking for DB Schema Migration. Tenant: "+ tenantDataSource);

       Flyway flyway = new Flyway();
       flyway.setDataSource(tenantDataSource);
       flyway.migrate();
   }
}
