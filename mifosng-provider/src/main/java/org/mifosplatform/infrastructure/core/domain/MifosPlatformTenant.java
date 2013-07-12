/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;


@Entity
@Table(name = "tenants")
public class MifosPlatformTenant extends AbstractPersistable<Long>{

    @Column(name="identifier")
    private String tenantIdentifier;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "schema_name")
    private String schemaName;
    
    @Column(name = "schema_server")
    private String schemaServer;
    
    @Column(name = "schema_server_port")
    private String schemaServerPort;
    
    @Column(name = "schema_username")
    private String schemaUsername;
    
    @Column(name = "schema_password")
    private String schemaPassword;
    
    @Column(name = "timezone_id")
    private String timezoneId;
    
    @Column(name = "auto_update")
    private boolean autoUpdateEnabled;
    
    
    public MifosPlatformTenant() {
        
    }

    public MifosPlatformTenant(final Long id,final String tenantIdentifier, final String name, final String schemaName, final String schemaServer,
            final String schemaServerPort, final String schemaUsername, final String schemaPassword, String timezoneId,
            final boolean autoUpdateEnabled) {
        setId(id);
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
        String url = new StringBuilder("jdbc:mysql://").append(schemaServer).append(':').append(schemaServerPort).append('/')
                .append(schemaName).toString();
        return url;
    }

     
    public String getTenantIdentifier() {
        return this.tenantIdentifier;
    }

    public String getName() {
        return name;
    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public String getSchemaUsername() {
        return schemaUsername;
    }

    public String getSchemaPassword() {
        return schemaPassword;
    }

    public String getTimezoneId() {
        return timezoneId;
    }

    public boolean isAutoUpdateEnabled() {
        return this.autoUpdateEnabled;
    }

}