package org.mifosplatform.infrastructure.core.domain;

public class MifosPlatformTenant {

    private final Long id;
    private final String name;
    private final String schemaName;
    private final String schemaServer;
    private final String schemaServerPort;
    private final String schemaUsername;
    private final String schemaPassword;
    private final String timezoneId;
    

    public MifosPlatformTenant(final Long id, final String name, final String schemaName, final String schemaServer,
            final String schemaServerPort, final String schemaUsername, final String schemaPassword, String timezoneId) {
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
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getSchemaServer() {
        return schemaServer;
    }

    public String getSchemaServerPort() {
        return schemaServerPort;
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
      
}