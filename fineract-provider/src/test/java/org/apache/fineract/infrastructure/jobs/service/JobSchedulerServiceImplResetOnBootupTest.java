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
package org.apache.fineract.infrastructure.jobs.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.junit.jupiter.api.Test;

class JobSchedulerServiceImplResetOnBootupTest {

    @Test
    void propertyDefaultsToTrueWhenNotConfigured() {
        assertThat(new FineractProperties.FineractJobProperties().isResetSchedulerOnBootupEnabled()).isTrue();
    }

    @Test
    void bootUnsuspendsTheSchedulerWhenTheColumnIsSetAndThePropertyIsOn() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(false)) {
            fixture.properties.getJob().setResetSchedulerOnBootupEnabled(true);
            fixture.schedulerDetail.setSuspended(true);
            fixture.schedulerDetail.setResetSchedulerOnBootup(true);

            fixture.boot();

            assertThat(fixture.schedulerDetail.isSuspended()).isFalse();
        }
    }

    @Test
    void bootKeepsTheSchedulerSuspendedWhenThePropertyIsOffEvenIfTheColumnIsSet() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(false)) {
            fixture.properties.getJob().setResetSchedulerOnBootupEnabled(false);
            fixture.schedulerDetail.setSuspended(true);
            fixture.schedulerDetail.setResetSchedulerOnBootup(true);

            fixture.boot();

            assertThat(fixture.schedulerDetail.isSuspended()).isTrue();
        }
    }
}
