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
package org.apache.fineract.infrastructure.configuration.data;

public class SMTPCredentialsData {

    private final String username;
    private final String password;
    private final String host;
    private final String port;
    private final boolean useTLS;
    private final String fromEmail;
    private final String fromName;

    public SMTPCredentialsData(final String username, final String password, final String host, final String port, final boolean useTLS,
            String fromEmail, String fromName) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.useTLS = useTLS;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public boolean isUseTLS() {
        return useTLS;
    }

    public String getFromEmail() {
        return fromEmail != null ? fromEmail : username;
    }

    public String getFromName() {
        return fromName != null ? fromName : username;
    }
}
