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

public class Tenant {

    private final Long id;
    private final String name;
    private final String schemaName;
    private final String schemaServer;
    private final String schemaServerPort;
    private final String schemaUsername;
    private final String schemaPassword;
    private final String timezoneId;

    public Tenant(final Long id, final String name, final String schemaName, final String schemaServer, final String schemaServerPort,
            final String schemaUsername, final String schemaPassword, final String timezoneId) {
        this.id = id;
        this.name = name;
        this.schemaName = schemaName;
        this.schemaServer = schemaServer;
        this.schemaServerPort = schemaServerPort;
        this.schemaUsername = schemaUsername;
        this.schemaPassword = schemaPassword;
        this.timezoneId = timezoneId;

    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public String getSchemaServer() {
        return this.schemaServer;
    }

    public String getSchemaServerPort() {
        return this.schemaServerPort;
    }

    public String getSchemaUsername() {
        return this.schemaUsername;
    }

    public String getSchemaPassword() {
        return this.schemaPassword;
    }

    public String getTimezoneId() {
        return this.timezoneId;
    }

}