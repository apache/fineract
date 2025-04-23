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
package org.apache.fineract.integrationtests.client;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.apache.fineract.client.models.StaffRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for /staff API.
 *
 * @author Michael Vorburger.ch
 */
public class StaffTest extends IntegrationTest {

    @Test
    @Order(1)
    void createOne() {
        assertThat(create()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    void retrieveAnyExisting() {
        assertThat(retrieveFirst()).isPresent();
    }

    // The following are not tests, but helpful utilities for other tests

    public Long getStaffId() {
        return retrieveFirst().orElseGet(this::create);
    }

    Long create() {
        return ok(fineractClient().staff.create3(new StaffRequest().officeId(1L).firstname(Utils.randomStringGenerator("StaffTest", 6))
                .lastname(Utils.randomStringGenerator("Staffer_", 6)).externalId(Utils.randomStringGenerator("", 12))
                .joiningDate(LocalDate.now(ZoneId.of("UTC")).toString()).dateFormat("yyyy-MM-dd").locale("en_US"))).getResourceId();
    }

    Optional<Long> retrieveFirst() {
        var staff = ok(fineractClient().staff.retrieveAll16(1L, true, false, "ACTIVE"));
        if (!staff.isEmpty()) {
            return Optional.of((long) staff.get(0).getId());
        }
        return Optional.empty();
    }
}
