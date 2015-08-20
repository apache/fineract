/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.mifosplatform.infrastructure.core.domain;


/**
 * Holds DB server connection details.
 *
 */
public class MifosPlatformTenantConnection {
    
    private final Long connectionId;
    private final String schemaServer;
    private final String schemaServerPort;
    private final String schemaUsername;
    private final String schemaPassword;
    private final String schemaName;
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
    private final int maxRetriesOnDeadlock;
    private final int maxIntervalBetweenRetries;
    private final boolean testOnBorrow;
    
    public MifosPlatformTenantConnection(final Long connectionId,final String schemaName, String schemaServer,final String schemaServerPort,final String schemaUsername,final String schemaPassword,
            final boolean autoUpdateEnabled,final int initialSize,final long validationInterval,final boolean removeAbandoned,final int removeAbandonedTimeout,
            final boolean logAbandoned,final int abandonWhenPercentageFull,final int maxActive,final int minIdle,final int maxIdle,final int suspectTimeout,
            final int timeBetweenEvictionRunsMillis,final int minEvictableIdleTimeMillis,final int maxRetriesOnDeadlock,final int maxIntervalBetweenRetries,final boolean tesOnBorrow) {
       
        this.connectionId = connectionId;
        this.schemaName =schemaName;
        this.schemaServer = schemaServer;
        this.schemaServerPort = schemaServerPort;
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
        this.maxRetriesOnDeadlock = maxRetriesOnDeadlock;
        this.maxIntervalBetweenRetries = maxIntervalBetweenRetries;
        this.testOnBorrow=tesOnBorrow;
    }

    public String databaseURL() {
        final String url = new StringBuilder("jdbc:mysql://").append(this.schemaServer).append(':').append(this.schemaServerPort)
                .append('/').append(this.schemaName).toString();
        return url;
    }
    
    /**
     * @return the schemaServer
     */
    public String getSchemaServer() {
        return this.schemaServer;
    }

    
    /**
     * @return the schemaServerPort
     */
    public String getSchemaServerPort() {
        return this.schemaServerPort;
    }

    
    /**
     * @return the schemaUsername
     */
    public String getSchemaUsername() {
        return this.schemaUsername;
    }

    
    /**
     * @return the schemaPassword
     */
    public String getSchemaPassword() {
        return this.schemaPassword;
    }

    
    /**
     * @return the autoUpdateEnabled
     */
    public boolean isAutoUpdateEnabled() {
        return this.autoUpdateEnabled;
    }

    
    /**
     * @return the initialSize
     */
    public int getInitialSize() {
        return this.initialSize;
    }

    
    /**
     * @return the validationInterval
     */
    public long getValidationInterval() {
        return this.validationInterval;
    }

    
    /**
     * @return the removeAbandoned
     */
    public boolean isRemoveAbandoned() {
        return this.removeAbandoned;
    }

    
    /**
     * @return the removeAbandonedTimeout
     */
    public int getRemoveAbandonedTimeout() {
        return this.removeAbandonedTimeout;
    }

    
    /**
     * @return the logAbandoned
     */
    public boolean isLogAbandoned() {
        return this.logAbandoned;
    }

    
    /**
     * @return the abandonWhenPercentageFull
     */
    public int getAbandonWhenPercentageFull() {
        return this.abandonWhenPercentageFull;
    }

    
    /**
     * @return the maxActive
     */
    public int getMaxActive() {
        return this.maxActive;
    }

    
    /**
     * @return the minIdle
     */
    public int getMinIdle() {
        return this.minIdle;
    }

    
    /**
     * @return the maxIdle
     */
    public int getMaxIdle() {
        return this.maxIdle;
    }

    
    /**
     * @return the suspectTimeout
     */
    public int getSuspectTimeout() {
        return this.suspectTimeout;
    }

    
    /**
     * @return the timeBetweenEvictionRunsMillis
     */
    public int getTimeBetweenEvictionRunsMillis() {
        return this.timeBetweenEvictionRunsMillis;
    }

    
    /**
     * @return the minEvictableIdleTimeMillis
     */
    public int getMinEvictableIdleTimeMillis() {
        return this.minEvictableIdleTimeMillis;
    }

    
    /**
     * @return the maxRetriesOnDeadlock
     */
    public int getMaxRetriesOnDeadlock() {
        return this.maxRetriesOnDeadlock;
    }

    
    /**
     * @return the maxIntervalBetweenRetries
     */
    public int getMaxIntervalBetweenRetries() {
        return this.maxIntervalBetweenRetries;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public Long getConnectionId() {
        return connectionId;
    }

    public String getSchemaName() {
        return schemaName;
    }
    @Override
    public String toString() {
        return this.schemaName+":"+this.schemaServer+":"+this.schemaServerPort;
    }
}
