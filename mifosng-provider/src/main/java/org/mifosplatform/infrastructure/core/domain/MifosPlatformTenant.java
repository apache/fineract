/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.domain;

public class MifosPlatformTenant {

    private final Long id;
    private final String tenantIdentifier;
    private final String name;
    private final String schemaName;
    private final String schemaServer;
    private final String schemaServerPort;
    private final String schemaUsername;
    private final String schemaPassword;
    private final String timezoneId;
    private final boolean autoUpdateEnabled;

    public MifosPlatformTenant(final Long id, final String tenantIdentifier, final String name, final String schemaName,
            final String schemaServer, final String schemaServerPort, final String schemaUsername, final String schemaPassword,
            final String timezoneId, final boolean autoUpdateEnabled) {
        this.id = id;
        this.tenantIdentifier = tenantIdentifier;
        this.name = name;
        this.schemaName = schemaName;
        this.schemaServer = schemaServer;
        this.schemaServerPort = schemaServerPort;
        this.schemaUsername = schemaUsername;
        this.schemaPassword = schemaPassword;
        this.timezoneId = timezoneId;
        this.autoUpdateEnabled = autoUpdateEnabled;
    }

    public String databaseURL() {
        final String url = new StringBuilder("jdbc:mysql://").append(this.schemaServer).append(':').append(this.schemaServerPort)
                .append('/').append(this.schemaName).toString();
        return url;
    }

    public Long getId() {
        return this.id;
    }

    public String getTenantIdentifier() {
        return this.tenantIdentifier;
    }

    public String getName() {
        return this.name;
    }

    public String getSchemaName() {
        return this.schemaName;
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

    public boolean isAutoUpdateEnabled() {
        return this.autoUpdateEnabled;
    }

}