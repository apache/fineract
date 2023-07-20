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
package org.apache.fineract.infrastructure.core.domain;

import java.io.Serializable;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;

/**
 * Holds Tenant's DB server connection connection details.
 */
@Getter
@Builder
@AllArgsConstructor
@Jacksonized
public class FineractPlatformTenantConnection implements Serializable {

    private final Long connectionId;
    private final String schemaServer;
    private final String schemaServerPort;
    private final String schemaConnectionParameters;
    private final String schemaUsername;
    private final String schemaPassword;
    private final String schemaName;
    private final String readOnlySchemaServer;
    private final String readOnlySchemaServerPort;
    private final String readOnlySchemaName;
    private final String readOnlySchemaUsername;
    private final String readOnlySchemaPassword;
    private final String readOnlySchemaConnectionParameters;
    private final boolean autoUpdateEnabled;
    private final int initialSize;
    private final long validationInterval;
    private final boolean removeAbandoned;
    private final int removeAbandonedTimeout;
    private final boolean logAbandoned;
    private final int abandonWhenPercentageFull;
    private final int maxActive;
    private final int minIdle;
    private final int maxIdle;
    private final int suspectTimeout;
    private final int timeBetweenEvictionRunsMillis;
    private final int minEvictableIdleTimeMillis;
    private final boolean testOnBorrow;

    private final String masterPasswordHash;

    public FineractPlatformTenantConnection(final Long connectionId, final String schemaName, String schemaServer,
            final String schemaServerPort, final String schemaConnectionParameters, final String schemaUsername,
            final String schemaPassword, final boolean autoUpdateEnabled, final int initialSize, final long validationInterval,
            final boolean removeAbandoned, final int removeAbandonedTimeout, final boolean logAbandoned,
            final int abandonWhenPercentageFull, final int maxActive, final int minIdle, final int maxIdle, final int suspectTimeout,
            final int timeBetweenEvictionRunsMillis, final int minEvictableIdleTimeMillis, final boolean tesOnBorrow,
            final String readOnlySchemaServer, final String readOnlySchemaServerPort, final String readOnlySchemaName,
            final String readOnlySchemaUsername, final String readOnlySchemaPassword, final String readOnlySchemaConnectionParameters,
            final String masterPasswordHash) {

        this.connectionId = connectionId;
        this.schemaName = schemaName;
        this.schemaServer = schemaServer;
        this.schemaServerPort = schemaServerPort;
        this.schemaConnectionParameters = schemaConnectionParameters;
        this.schemaUsername = schemaUsername;
        this.schemaPassword = schemaPassword;
        this.autoUpdateEnabled = autoUpdateEnabled;
        this.initialSize = initialSize;
        this.validationInterval = validationInterval;
        this.removeAbandoned = removeAbandoned;
        this.removeAbandonedTimeout = removeAbandonedTimeout;
        this.logAbandoned = logAbandoned;
        this.abandonWhenPercentageFull = abandonWhenPercentageFull;
        this.maxActive = maxActive;
        this.minIdle = minIdle;
        this.maxIdle = maxIdle;
        this.suspectTimeout = suspectTimeout;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        this.testOnBorrow = tesOnBorrow;
        this.readOnlySchemaServer = readOnlySchemaServer;
        this.readOnlySchemaServerPort = readOnlySchemaServerPort;
        this.readOnlySchemaName = readOnlySchemaName;
        this.readOnlySchemaUsername = readOnlySchemaUsername;
        this.readOnlySchemaPassword = readOnlySchemaPassword;
        this.readOnlySchemaConnectionParameters = readOnlySchemaConnectionParameters;
        this.masterPasswordHash = masterPasswordHash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.schemaName).append(":").append(this.schemaServer).append(":")
                .append(this.schemaServerPort);
        if (this.schemaConnectionParameters != null && !this.schemaConnectionParameters.isEmpty()) {
            sb.append('?').append(this.schemaConnectionParameters);
        }
        return sb.toString();
    }

    public static String toJdbcUrl(String protocol, String host, String port, String db, String parameters) {
        StringBuilder sb = new StringBuilder(protocol).append("://").append(host).append(":").append(port).append('/').append(db);

        if (!StringUtils.isEmpty(parameters)) {
            sb.append('?').append(parameters);
        }

        return sb.toString();
    }

    public static String toProtocol(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            return url.substring(0, url.indexOf("://"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
