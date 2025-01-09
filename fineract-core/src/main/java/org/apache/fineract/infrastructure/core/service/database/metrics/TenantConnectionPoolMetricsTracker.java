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

package org.apache.fineract.infrastructure.core.service.database.metrics;

import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.PoolStats;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;

public class TenantConnectionPoolMetricsTracker implements IMetricsTracker {

    public static final String HIKARI_METRIC_NAME_PREFIX = ".hikaricp";

    private static final String METRIC_CATEGORY = "pool";
    private static final String METRIC_NAME_WAIT = HIKARI_METRIC_NAME_PREFIX + ".connections.acquire";
    private static final String METRIC_NAME_USAGE = HIKARI_METRIC_NAME_PREFIX + ".connections.usage";
    private static final String METRIC_NAME_CONNECT = HIKARI_METRIC_NAME_PREFIX + ".connections.creation";

    private static final String METRIC_NAME_TIMEOUT_RATE = HIKARI_METRIC_NAME_PREFIX + ".connections.timeout";
    private static final String METRIC_NAME_TOTAL_CONNECTIONS = HIKARI_METRIC_NAME_PREFIX + ".connections";
    private static final String METRIC_NAME_IDLE_CONNECTIONS = HIKARI_METRIC_NAME_PREFIX + ".connections.idle";
    private static final String METRIC_NAME_ACTIVE_CONNECTIONS = HIKARI_METRIC_NAME_PREFIX + ".connections.active";
    private static final String METRIC_NAME_PENDING_CONNECTIONS = HIKARI_METRIC_NAME_PREFIX + ".connections.pending";
    private static final String METRIC_NAME_MAX_CONNECTIONS = HIKARI_METRIC_NAME_PREFIX + ".connections.max";
    private static final String METRIC_NAME_MIN_CONNECTIONS = HIKARI_METRIC_NAME_PREFIX + ".connections.min";

    private final Timer connectionObtainTimer;
    private final Counter connectionTimeoutCounter;
    private final Timer connectionUsage;
    private final Timer connectionCreation;
    private final Gauge totalConnectionGauge;
    private final Gauge idleConnectionGauge;
    private final Gauge activeConnectionGauge;
    private final Gauge pendingConnectionGauge;
    private final Gauge maxConnectionGauge;
    private final Gauge minConnectionGauge;
    private final MeterRegistry meterRegistry;
    private final PoolStats poolStats;

    public TenantConnectionPoolMetricsTracker(String tenantIdentifier, String poolName, PoolStats poolStats, MeterRegistry meterRegistry) {
        // poolStats must be held with a 'strong reference' even though it is never referenced within this class
        this.poolStats = poolStats; // DO NOT REMOVE

        this.meterRegistry = meterRegistry;

        String metricPrefix = "fineract.tenants." + tenantIdentifier;

        this.connectionObtainTimer = Timer.builder(metricPrefix + METRIC_NAME_WAIT).description("Connection acquire time") //
                .tags(METRIC_CATEGORY, poolName) //
                .register(meterRegistry);

        this.connectionCreation = Timer.builder(metricPrefix + METRIC_NAME_CONNECT).description("Connection creation time") //
                .tags(METRIC_CATEGORY, poolName) //
                .register(meterRegistry);

        this.connectionUsage = Timer.builder(metricPrefix + METRIC_NAME_USAGE).description("Connection usage time") //
                .tags(METRIC_CATEGORY, poolName) //
                .register(meterRegistry);

        this.connectionTimeoutCounter = Counter.builder(metricPrefix + METRIC_NAME_TIMEOUT_RATE)
                .description("Connection timeout total count") //
                .tags(METRIC_CATEGORY, poolName) //
                .register(meterRegistry);

        this.totalConnectionGauge = Gauge.builder(metricPrefix + METRIC_NAME_TOTAL_CONNECTIONS, poolStats, PoolStats::getTotalConnections)
                .description("Total connections") //
                .tags(METRIC_CATEGORY, poolName) //
                .register(meterRegistry);

        this.idleConnectionGauge = Gauge.builder(metricPrefix + METRIC_NAME_IDLE_CONNECTIONS, poolStats, PoolStats::getIdleConnections)
                .description("Idle connections") //
                .tags(METRIC_CATEGORY, poolName) //
                .register(meterRegistry);

        this.activeConnectionGauge = Gauge
                .builder(metricPrefix + METRIC_NAME_ACTIVE_CONNECTIONS, poolStats, PoolStats::getActiveConnections)
                .description("Active connections") //
                .tags(METRIC_CATEGORY, poolName) //
                .register(meterRegistry);

        this.pendingConnectionGauge = Gauge.builder(metricPrefix + METRIC_NAME_PENDING_CONNECTIONS, poolStats, PoolStats::getPendingThreads)
                .description("Pending threads") //
                .tags(METRIC_CATEGORY, poolName) //
                .register(meterRegistry);

        this.maxConnectionGauge = Gauge.builder(metricPrefix + METRIC_NAME_MAX_CONNECTIONS, poolStats, PoolStats::getMaxConnections)
                .description("Max connections") //
                .tags(METRIC_CATEGORY, poolName) //
                .register(meterRegistry);

        this.minConnectionGauge = Gauge.builder(metricPrefix + METRIC_NAME_MIN_CONNECTIONS, poolStats, PoolStats::getMinConnections)
                .description("Min connections") //
                .tags(METRIC_CATEGORY, poolName) //
                .register(meterRegistry);

    }

    @Override
    public void recordConnectionAcquiredNanos(final long elapsedAcquiredNanos) {
        connectionObtainTimer.record(elapsedAcquiredNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void recordConnectionUsageMillis(final long elapsedBorrowedMillis) {
        connectionUsage.record(elapsedBorrowedMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordConnectionTimeout() {
        connectionTimeoutCounter.increment();
    }

    @Override
    public void recordConnectionCreatedMillis(long connectionCreatedMillis) {
        connectionCreation.record(connectionCreatedMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        meterRegistry.remove(connectionObtainTimer);
        meterRegistry.remove(connectionTimeoutCounter);
        meterRegistry.remove(connectionUsage);
        meterRegistry.remove(connectionCreation);
        meterRegistry.remove(totalConnectionGauge);
        meterRegistry.remove(idleConnectionGauge);
        meterRegistry.remove(activeConnectionGauge);
        meterRegistry.remove(pendingConnectionGauge);
        meterRegistry.remove(maxConnectionGauge);
        meterRegistry.remove(minConnectionGauge);
    }
}
