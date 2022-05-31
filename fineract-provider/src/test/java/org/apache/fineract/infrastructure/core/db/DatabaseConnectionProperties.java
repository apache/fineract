/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fineract.infrastructure.core.db;

public class DatabaseConnectionProperties {

    private String driverClassName;
    private String jdbcUrl;
    private String connectionTestQuery;

    private String schemaServer;
    private String schemaName;
    private String schemaPort;
    private String schemaUsername;
    private String schemaPassword;

    protected DatabaseConnectionProperties(final String driverClassName, final String jdbcUrl, final String schemaPort,
            final String schemaPassword) {
        this.driverClassName = driverClassName;
        this.jdbcUrl = jdbcUrl;
        this.schemaPort = schemaPort;
        this.schemaPassword = schemaPassword;

        this.schemaServer = "localhost";
        this.schemaUsername = "root";
        this.schemaName = "fineract_tenants";
        this.connectionTestQuery = "SELECT 1";
    }

    public static DatabaseConnectionProperties instance(final String dbType) {
        return new DatabaseConnectionProperties(getDbDriverClassName(dbType), buildJdbcUrl(dbType), getDbPort(dbType),
                getDbPassword(dbType));
    }

    private static String getDbDriverClassName(final String dbType) {
        if (dbType.equals("mariadb")) {
            return "org.mariadb.jdbc.Driver";
        } else if (dbType.equals("mysql")) {
            return "com.mysql.cj.jdbc.Driver";
        } else {
            return "org.postgresql.Driver";
        }
    }

    private static String buildJdbcUrl(final String dbType) {
        return "jdbc:" + dbType + "://localhost:" + getDbPort(dbType) + "/fineract_tenants";
    }

    private static String getDbPort(final String dbType) {
        if (dbType.equals("postgresql")) {
            return "5432";
        }
        return "3306";
    }

    private static String getDbPassword(final String dbType) {
        if (dbType.equals("postgresql")) {
            return "postgres";
        }
        return "mysql";
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getConnectionTestQuery() {
        return connectionTestQuery;
    }

    public String getSchemaServer() {
        return schemaServer;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getSchemaPort() {
        return schemaPort;
    }

    public String getSchemaUsername() {
        return schemaUsername;
    }

    public String getSchemaPassword() {
        return schemaPassword;
    }
}
