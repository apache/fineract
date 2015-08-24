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
    private final String timezoneId;
    private final MifosPlatformTenantConnection connection;

    public MifosPlatformTenant(final Long id, final String tenantIdentifier, final String name,
            final String timezoneId, final MifosPlatformTenantConnection connection) {
        this.id = id;
        this.tenantIdentifier = tenantIdentifier;
        this.name = name;
        this.timezoneId = timezoneId;
        this.connection = connection;
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

    public String getTimezoneId() {
        return this.timezoneId;
    }

    public MifosPlatformTenantConnection getConnection() {
        return connection;
    }

}