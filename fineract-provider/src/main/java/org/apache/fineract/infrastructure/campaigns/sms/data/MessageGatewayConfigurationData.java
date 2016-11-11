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
package org.apache.fineract.infrastructure.campaigns.sms.data;

public class MessageGatewayConfigurationData {

    private final Long id;
    private final String connectionName;
    private final String hostName;
    private final int portNumber;
    private final String endPoint;
    private final String userName;
    private final String password;
    private final boolean sslEnabled;
    private final String tenantAppKey;

    public MessageGatewayConfigurationData(final Long id, final String connectionName, final String hostName, final int portNumber,
            final String endPoint, final String userName, final String password, final boolean sslEnabled, final String tenantAppKey) {
        this.id = id;
        this.connectionName = connectionName;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.endPoint = endPoint;
        this.userName = userName;
        this.password = password;
        this.sslEnabled = sslEnabled;
        this.tenantAppKey = tenantAppKey;
    }

    public Long getId() {
        return this.id;
    }

    public String getConnectionName() {
        return this.connectionName;
    }

    public String getHostName() {
        return this.hostName;
    }

    public int getPortNumber() {
        return this.portNumber;
    }

    public String getEndPoint() {
        return this.endPoint;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isSslEnabled() {
        return this.sslEnabled;
    }

    public String getTenantAppKey() {
        return this.tenantAppKey;
    }
}
