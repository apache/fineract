/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.data;

public class SMTPCredentialsData {

    private final String username;
    private final String password;
    private final String host;
    private final String port;
    private final boolean useTLS;

    public SMTPCredentialsData(final String username, final String password, final String host, final String port, final boolean useTLS) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.useTLS = useTLS;
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

}
